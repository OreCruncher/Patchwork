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

package org.orecruncher.patchwork.common.block.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.patchwork.common.block.BlockFurnace3D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityFurnace3D extends TileEntityLockable implements ITickable, ISidedInventory {

	private static final int[] SLOTS_TOP = new int[] { 0 };
	private static final int[] SLOTS_BOTTOM = new int[] { 2, 1 };
	private static final int[] SLOTS_SIDES = new int[] { 1 };

	/** The ItemStacks that hold the items currently being used in the furnace */
	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);

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

	@Override
	public String getName() {
		return hasCustomName() ? this.customName : "container.furnace3d";
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
		return this.furnaceItemStacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (final ItemStack itemstack : this.furnaceItemStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getStackInSlot(int index) {
		return this.furnaceItemStacks.get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		final ItemStack itemstack = this.furnaceItemStacks.get(index);
		final boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack)
				&& ItemStack.areItemStackTagsEqual(stack, itemstack);
		this.furnaceItemStacks.set(index, stack);

		if (stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}

		if (index == 0 && !flag) {
			this.totalCookTime = getCookTime(stack);
			this.cookTime = 0;
			markDirty();
		}
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.furnaceItemStacks = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbt, this.furnaceItemStacks);
		this.furnaceBurnTime = nbt.getInteger(NBT.BURN_TIME);
		this.cookTime = nbt.getInteger(NBT.COOK_TIME);
		this.totalCookTime = nbt.getInteger(NBT.COOK_TIME_TOTAL);
		this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks.get(1));
		if (nbt.hasKey(NBT.CUSTOM_NAME, 8))
			this.customName = nbt.getString(NBT.CUSTOM_NAME);
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger(NBT.BURN_TIME, (short) this.furnaceBurnTime);
		nbt.setInteger(NBT.COOK_TIME, (short) this.cookTime);
		nbt.setInteger(NBT.COOK_TIME_TOTAL, (short) this.totalCookTime);
		ItemStackHelper.saveAllItems(nbt, this.furnaceItemStacks);

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
	 * Furnace isBurning
	 */
	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	@SideOnly(Side.CLIENT)
	public static boolean isBurning(@Nonnull final IInventory inventory) {
		return inventory.getField(0) > 0;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		final boolean currentBurnState = this.isBurning();
		boolean newBurnState = false;

		if (this.isBurning()) {
			--this.furnaceBurnTime;
		}

		if (!this.world.isRemote) {
			final ItemStack itemstack = this.furnaceItemStacks.get(1);

			if (this.isBurning() || !itemstack.isEmpty() && !this.furnaceItemStacks.get(0).isEmpty()) {
				if (!this.isBurning() && canSmelt()) {
					this.furnaceBurnTime = getItemBurnTime(itemstack);
					this.currentItemBurnTime = this.furnaceBurnTime;

					if (this.isBurning()) {
						newBurnState = true;

						if (!itemstack.isEmpty()) {
							final Item item = itemstack.getItem();
							itemstack.shrink(1);

							if (itemstack.isEmpty()) {
								final ItemStack item1 = item.getContainerItem(itemstack);
								this.furnaceItemStacks.set(1, item1);
							}
						}
					}
				}

				if (this.isBurning() && canSmelt()) {
					++this.cookTime;

					if (this.cookTime == this.totalCookTime) {
						this.cookTime = 0;
						this.totalCookTime = getCookTime(this.furnaceItemStacks.get(0));
						smeltItem();
						newBurnState = true;
					}
				} else {
					this.cookTime = 0;
				}
			} else if (!this.isBurning() && this.cookTime > 0) {
				this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
			}

			if (currentBurnState != this.isBurning()) {
				newBurnState = true;
				this.world.setBlockState(this.pos, getState().withProperty(BlockFurnace3D.ACTIVE, isBurning()));
			}
		}

		if (newBurnState) {
			sendUpdates();
		}
	}

	public int getCookTime(ItemStack stack) {
		return 200;
	}

	/**
	 * Returns true if the furnace can smelt an item, i.e. has a source item,
	 * destination stack isn't full, etc.
	 */
	private boolean canSmelt() {
		if (this.furnaceItemStacks.get(0).isEmpty()) {
			return false;
		} else {
			final ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks.get(0));

			if (itemstack.isEmpty()) {
				return false;
			} else {
				final ItemStack itemstack1 = this.furnaceItemStacks.get(2);

				if (itemstack1.isEmpty()) {
					return true;
				} else if (!itemstack1.isItemEqual(itemstack)) {
					return false;
				} else if (itemstack1.getCount() + itemstack.getCount() <= getInventoryStackLimit()
						&& itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
					return true;
				} else {
					return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize();
				}
			}
		}
	}

	/**
	 * Turn one item from the furnace source stack into the appropriate smelted item
	 * in the furnace result stack
	 */
	public void smeltItem() {
		if (canSmelt()) {
			final ItemStack itemstack = this.furnaceItemStacks.get(0);
			final ItemStack itemstack1 = FurnaceRecipes.instance().getSmeltingResult(itemstack);
			final ItemStack itemstack2 = this.furnaceItemStacks.get(2);

			if (itemstack2.isEmpty()) {
				this.furnaceItemStacks.set(2, itemstack1.copy());
			} else if (itemstack2.getItem() == itemstack1.getItem()) {
				itemstack2.grow(itemstack1.getCount());
			}

			if (itemstack.getItem() == Item.getItemFromBlock(Blocks.SPONGE) && itemstack.getMetadata() == 1
					&& !this.furnaceItemStacks.get(1).isEmpty()
					&& this.furnaceItemStacks.get(1).getItem() == Items.BUCKET) {
				this.furnaceItemStacks.set(1, new ItemStack(Items.WATER_BUCKET));
			}

			itemstack.shrink(1);
		}
	}

	/**
	 * Returns the number of ticks that the supplied fuel item will keep the furnace
	 * burning, or 0 if the item isn't fuel
	 */
	public static int getItemBurnTime(ItemStack stack) {
		return TileEntityFurnace.getItemBurnTime(stack);
	}

	public static boolean isItemFuel(ItemStack stack) {
		return TileEntityFurnace.getItemBurnTime(stack) > 0;
	}

	/**
	 * Don't rename this method to canInteractWith due to conflicts with Container
	 */
	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
					this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot. For guis use Slot.isItemValid
	 */
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index == 2) {
			return false;
		} else if (index != 1) {
			return true;
		} else {
			final ItemStack itemstack = this.furnaceItemStacks.get(1);
			return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && itemstack.getItem() != Items.BUCKET;
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
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
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return isItemValidForSlot(index, itemStackIn);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from
	 * the given side.
	 */
	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		if (direction == EnumFacing.DOWN && index == 1) {
			final Item item = stack.getItem();

			if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String getGuiID() {
		return "minecraft:furnace";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerFurnace3D(playerInventory, this);
	}

	@Override
	public int getField(int id) {
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
	public void setField(int id, int value) {
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
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	@Override
	public void clear() {
		this.furnaceItemStacks.clear();
	}

	private IBlockState getState() {
		return this.world.getBlockState(this.pos);
	}

	private void sendUpdates() {
		this.world.markBlockRangeForRenderUpdate(pos, pos);
		this.world.notifyBlockUpdate(pos, getState(), getState(), 3);
		this.world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
		markDirty();
	}

	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(@Nonnull final NetworkManager net, @Nonnull final SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}

	net.minecraftforge.items.IItemHandler handlerTop = new net.minecraftforge.items.wrapper.SidedInvWrapper(this,
			net.minecraft.util.EnumFacing.UP);
	net.minecraftforge.items.IItemHandler handlerBottom = new net.minecraftforge.items.wrapper.SidedInvWrapper(this,
			net.minecraft.util.EnumFacing.DOWN);
	net.minecraftforge.items.IItemHandler handlerSide = new net.minecraftforge.items.wrapper.SidedInvWrapper(this,
			net.minecraft.util.EnumFacing.WEST);

	@SuppressWarnings("unchecked")
	@Override
	@javax.annotation.Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			@javax.annotation.Nullable net.minecraft.util.EnumFacing facing) {
		if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			if (facing == EnumFacing.DOWN)
				return (T) this.handlerBottom;
			else if (facing == EnumFacing.UP)
				return (T) this.handlerTop;
			else
				return (T) this.handlerSide;
		return super.getCapability(capability, facing);
	}

	private static class NBT {
		public static final String BURN_TIME = "bt";
		public static final String COOK_TIME = "ct";
		public static final String COOK_TIME_TOTAL = "ctt";
		public static final String CUSTOM_NAME = "cn";
	}

}