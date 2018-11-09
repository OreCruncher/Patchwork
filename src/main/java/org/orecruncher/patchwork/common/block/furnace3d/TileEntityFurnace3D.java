/*
 * Licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Portions copyright Shadows-of-fire.  Essentially used his optimizations for the furnace code
// https://github.com/Shadows-of-Fire/FastFurnace/blob/master/src/main/java/shadows/fastfurnace/block/TileFastFurnace.java

package org.orecruncher.patchwork.common.block.furnace3d;

import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityFurnace3D extends TileEntityLockable implements ITickable, ISidedInventory {

	private static final int[] SLOTS_TOP = new int[] { Furnace3DStackHandler.INPUT_SLOT };
	private static final int[] SLOTS_BOTTOM = new int[] { Furnace3DStackHandler.OUTPUT_SLOT,
			Furnace3DStackHandler.FUEL_SLOT };
	private static final int[] SLOTS_SIDES = new int[] { Furnace3DStackHandler.FUEL_SLOT };

	@ItemStackHolder(value = "minecraft:sponge", meta = 1)
	public static final ItemStack WET_SPONGE = ItemStack.EMPTY;

	private final IItemHandler handlerTop = new SidedInvWrapper(this, EnumFacing.UP);
	private final IItemHandler handlerBottom = new SidedInvWrapper(this, EnumFacing.DOWN);
	private final IItemHandler handlerSide = new SidedInvWrapper(this, EnumFacing.WEST);

	protected ItemStack recipeKey = ItemStack.EMPTY;
	protected ItemStack recipeOutput = ItemStack.EMPTY;
	protected ItemStack failedMatch = ItemStack.EMPTY;

	/**
	 * Our inventory for the furnace
	 */
	private final Furnace3DStackHandler inventory = new Furnace3DStackHandler();

	/** The number of ticks that the furnace will keep burning */
	private int furnaceBurnTime;

	/**
	 * The number of ticks that a fresh copy of the currently-burning item would
	 * keep the furnace burning for
	 */
	private int currentItemBurnTime;
	private int cookTime;
	private int totalCookTime;

	private String customName;

	public TileEntityFurnace3D() {
		this.totalCookTime = 200;
	}

	@Override
	public String getName() {
		return hasCustomName() ? this.customName : "container.furnace";
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && !this.customName.isEmpty();
	}

	public void setCustomInventoryName(@Nonnull final String name) {
		this.customName = name;
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	@Nonnull
	public ItemStack getStackInSlot(final int index) {
		return this.inventory.getStackInSlot(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	@Override
	@Nonnull
	public ItemStack decrStackSize(final int index, final int count) {
		return this.inventory.getAndSplit(index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return this.inventory.getAndRemove(index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		final ItemStack itemstack = this.inventory.getStackInSlot(index);
		final boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack)
				&& ItemStack.areItemStackTagsEqual(stack, itemstack);
		this.inventory.setStackInSlot(index, stack);

		if (stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}

		if (index == 0 && !flag) {
			this.totalCookTime = getCookTime(stack);
			this.cookTime = 0;
			sendUpdates(true);
		}
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.inventory.deserializeNBT(nbt.getCompoundTag(NBT.INVENTORY));
		this.furnaceBurnTime = nbt.getInteger(NBT.BURN_TIME);
		this.cookTime = nbt.getInteger(NBT.COOK_TIME);
		this.totalCookTime = nbt.getInteger(NBT.COOK_TIME_TOTAL);
		this.currentItemBurnTime = getItemBurnTime(this.inventory.getFuelStack());
		if (nbt.hasKey(NBT.CUSTOM_NAME, 8))
			this.customName = nbt.getString(NBT.CUSTOM_NAME);
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger(NBT.BURN_TIME, (short) this.furnaceBurnTime);
		nbt.setInteger(NBT.COOK_TIME, (short) this.cookTime);
		nbt.setInteger(NBT.COOK_TIME_TOTAL, (short) this.totalCookTime);
		nbt.setTag(NBT.INVENTORY, this.inventory.serializeNBT());

		if (hasCustomName())
			nbt.setString(NBT.CUSTOM_NAME, this.customName);

		return nbt;
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
	 * possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Helper method for the TESR
	 */
	@Nonnull
	public EnumFacing getFacing() {
		return getState().getValue(BlockFurnace3D.FACING);
	}

	/**
	 * Furnace isBurning
	 */
	private boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		if (this.world.isRemote && isBurning()) {
			this.furnaceBurnTime--;
			return;
		} else if (this.world.isRemote)
			return;

		ItemStack fuel = ItemStack.EMPTY;
		final boolean canSmelt = canSmelt();

		if (!isBurning() && canSmelt && !(fuel = this.inventory.getFuelStack()).isEmpty())
			burnFuel(fuel, false);

		final boolean wasBurning = isBurning();

		if (isBurning()) {
			this.furnaceBurnTime--;
			if (canSmelt)
				smelt();
			else
				this.cookTime = 0;
		}

		if (!isBurning() && canSmelt() && !(fuel = this.inventory.getFuelStack()).isEmpty())
			burnFuel(fuel, wasBurning);

		sendUpdates(wasBurning && !isBurning());
	}

	private void updateBlockState() {
		final IBlockState state = getState();
		if (state.getValue(BlockFurnace3D.ACTIVE) != isBurning()) {
			this.world.setBlockState(this.pos, state.withProperty(BlockFurnace3D.ACTIVE, isBurning()));
		}
	}

	@Override
	public boolean shouldRefresh(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState oldState, @Nonnull final IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	private int getCookTime(@Nonnull final ItemStack stack) {
		return 200;
	}

	/**
	 * Returns true if the furnace can smelt an item, i.e. has a source item,
	 * destination stack isn't full, etc.
	 */
	private boolean canSmelt() {
		final ItemStack input = this.inventory.getInputStack();
		final ItemStack output = this.inventory.getOutputStack();
		if (input.isEmpty() || input == this.failedMatch)
			return false;

		if (this.recipeKey.isEmpty() || !OreDictionary.itemMatches(this.recipeKey, input, false)) {
			boolean matched = false;
			for (final Entry<ItemStack, ItemStack> e : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
				if (OreDictionary.itemMatches(e.getKey(), input, false)) {
					this.recipeKey = e.getKey();
					this.recipeOutput = e.getValue();
					matched = true;
					this.failedMatch = ItemStack.EMPTY;
					break;
				}
			}
			if (!matched) {
				this.recipeKey = ItemStack.EMPTY;
				this.recipeOutput = ItemStack.EMPTY;
				this.failedMatch = input;
				return false;
			}
		}

		return !this.recipeOutput.isEmpty()
				&& (output.isEmpty() || (ItemHandlerHelper.canItemStacksStack(this.recipeOutput, output)
						&& (this.recipeOutput.getCount() + output.getCount() <= output.getMaxStackSize())));

	}

	private void smelt() {
		this.cookTime++;
		if (this.cookTime == this.totalCookTime) {
			this.cookTime = 0;
			this.totalCookTime = getCookTime(this.inventory.getInputStack());
			smeltItem();
		}
	}

	private void burnFuel(ItemStack fuel, boolean burnedThisTick) {
		this.currentItemBurnTime = (this.furnaceBurnTime = getItemBurnTime(fuel));
		if (isBurning()) {
			// Have to take into account things like lava buckets
            final Item item = fuel.getItem();
			fuel.shrink(1);
			if (fuel.isEmpty())
				fuel = item.getContainerItem(fuel);
			this.inventory.setFuelStack(fuel);
			sendUpdates(!burnedThisTick);
		}
	}

	/**
	 * Turn one item from the furnace source stack into the appropriate smelted item
	 * in the furnace result stack
	 */
	private void smeltItem() {
		final ItemStack input = this.inventory.getInputStack();
		final ItemStack recipeOutput = FurnaceRecipes.instance().getSmeltingList().get(this.recipeKey);
		final ItemStack output = this.inventory.getOutputStack();

		if (output.isEmpty()) {
			this.inventory.setOutputStack(recipeOutput.copy());
		} else if (ItemHandlerHelper.canItemStacksStack(output, recipeOutput)) {
			output.grow(recipeOutput.getCount());
			this.inventory.setOutputStack(output);
		}

		if (input.isItemEqual(WET_SPONGE) && this.inventory.getFuelStack().getItem() == Items.BUCKET)
			this.inventory.setFuelStack(new ItemStack(Items.WATER_BUCKET));

		input.shrink(1);
		this.inventory.setInputStack(input);
	}

	/**
	 * Returns the number of ticks that the supplied fuel item will keep the furnace
	 * burning, or 0 if the item isn't fuel
	 */
	private static int getItemBurnTime(@Nonnull final ItemStack stack) {
		return TileEntityFurnace.getItemBurnTime(stack);
	}

	public static boolean isItemFuel(@Nonnull final ItemStack stack) {
		return getItemBurnTime(stack) > 0;
	}

	/**
	 * Don't rename this method to canInteractWith due to conflicts with Container
	 */
	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
					this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void openInventory(@Nonnull final EntityPlayer player) {
		// Intentionally blank
	}

	@Override
	public void closeInventory(@Nonnull final EntityPlayer player) {
		// Intentionally blank
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot. For guis use Slot.isItemValid
	 */
	@Override
	public boolean isItemValidForSlot(final int index, @Nonnull final ItemStack stack) {
		if (index == Furnace3DStackHandler.OUTPUT_SLOT) {
			return false;
		} else if (index != Furnace3DStackHandler.FUEL_SLOT) {
			return true;
		} else {
			final ItemStack fuelStack = this.inventory.getFuelStack();
			return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && fuelStack.getItem() != Items.BUCKET;
		}
	}

	@Override
	@Nonnull
	public int[] getSlotsForFace(@Nonnull final EnumFacing side) {
		if (side == EnumFacing.DOWN) {
			return SLOTS_BOTTOM;
		} else {
			return side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES;
		}
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from
	 * the given side.
	 */
	@Override
	public boolean canInsertItem(final int index, @Nonnull final ItemStack itemStackIn,
			@Nullable EnumFacing direction) {
		return isItemValidForSlot(index, itemStackIn);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from
	 * the given side.
	 */
	@Override
	public boolean canExtractItem(final int index, @Nonnull final ItemStack stack,
			@Nonnull final EnumFacing direction) {
		if (direction == EnumFacing.DOWN && index == 1) {
			final Item item = stack.getItem();

			if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
				return false;
			}
		}

		return true;
	}

	@Override
	@Nonnull
	public String getGuiID() {
		return "minecraft:furnace";
	}

	@Override
	@Nonnull
	public Container createContainer(@Nonnull final InventoryPlayer playerInventory,
			@Nonnull final EntityPlayer playerIn) {
		return new ContainerFurnace3D(playerInventory, this);
	}

	@Override
	public int getField(final int id) {
		switch (id) {
		case 0:
			return this.furnaceBurnTime;
		case 1:
			return this.currentItemBurnTime;
		case 2:
			return this.cookTime;
		case 3:
			return this.totalCookTime;
		default:
			return 0;
		}
	}

	@Override
	public void setField(final int id, final int value) {
		switch (id) {
		case 0:
			this.furnaceBurnTime = value;
			break;
		case 1:
			this.currentItemBurnTime = value;
			break;
		case 2:
			this.cookTime = value;
			break;
		case 3:
			this.totalCookTime = value;
			break;
		default:
			// fall thru
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	@Override
	public void clear() {
		this.inventory.clear();
	}

	@Nonnull
	private IBlockState getState() {
		return this.world.getBlockState(this.pos);
	}

	private void sendUpdates(final boolean dirty) {
		updateBlockState();
		if (this.inventory.isDirty()) {
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			this.world.notifyBlockUpdate(this.pos, getState(), getState(), 3);
			this.world.scheduleBlockUpdate(this.pos, getBlockType(), 0, 0);
			this.inventory.clearDirty();
		}
		if (dirty)
			markDirty();
	}

	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(@Nonnull final NetworkManager net, @Nonnull final SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			if (facing == EnumFacing.DOWN)
				return (T) this.handlerBottom;
			else if (facing == EnumFacing.UP)
				return (T) this.handlerTop;
			else
				return (T) this.handlerSide;
		return super.getCapability(capability, facing);
	}

	private static class NBT {
		public static final String INVENTORY = "i";
		public static final String BURN_TIME = "bt";
		public static final String COOK_TIME = "ct";
		public static final String COOK_TIME_TOTAL = "ctt";
		public static final String CUSTOM_NAME = "cn";
	}

}