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

package org.orecruncher.patchwork.block.shopshelf;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.lib.Localization;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.lib.InventoryUtils;
import org.orecruncher.patchwork.lib.StackHandlerBase;
import org.orecruncher.patchwork.lib.TileEntityContainerBase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityShopShelf extends TileEntityContainerBase {

	@ItemStackHolder(value = "minecraft:planks", meta = 0)
	public static final ItemStack DEFAULT_SKIN = ItemStack.EMPTY;
	protected static final UUID UNOWNED = new UUID(0, 0);
	protected static final int PLANKS_ID = OreDictionary.getOreID("plankWood");

	protected UUID owner = UNOWNED;
	protected String ownerName = "";
	protected ItemStack mimic = DEFAULT_SKIN;
	protected boolean adminShop = false;
	protected ShopShelfStackHandler inventory = new ShopShelfStackHandler();

	@Override
	@Nonnull
	protected StackHandlerBase getInventory() {
		return this.inventory;
	}

	@Override
	public String getName() {
		if (isAdminShop()) {
			final String name = Localization.loadString("tile.patchwork.shopshelf.admin.name");
			return Localization.format("tile.patchwork.shopshelf.name.format", name);
		} else if (isOwned()) {
			return Localization.format("tile.patchwork.shopshelf.name.format", this.ownerName);
		}
		return "tile.patchwork.shopshelf.name";
	}

	/**
	 * Helper method for the TESR
	 */
	@Nonnull
	public EnumFacing getFacing() {
		return getState().getValue(BlockShopShelf.FACING);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Container createContainer(@Nonnull final InventoryPlayer playerInventory,
			@Nonnull final EntityPlayer playerIn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGuiID() {
		return ModInfo.MOD_ID + ".shopshelf";
	}

	@SideOnly(Side.CLIENT)
	public ItemStack getMimic() {
		return this.mimic;
	}

	public boolean isValidMimic(@Nonnull final ItemStack stack) {
		return stack.getItem() instanceof ItemBlock;
		// final int[] dicts = OreDictionary.getOreIDs(stack);
		// return dicts != null && IntStream.of(dicts).anyMatch(x -> x == PLANKS_ID);
	}

	public void setMimic(@Nonnull final ItemStack stack) {
		setMimic(stack, false);
	}

	protected void setMimic(@Nonnull final ItemStack stack, final boolean loading) {
		if (!InventoryUtils.canCombine(this.mimic, stack) && isValidMimic(stack)) {
			this.mimic = stack.copy();
			this.mimic.setCount(1);
			if (this.world.isRemote)
				this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			else if (!loading)
				sendUpdates(false);
		}
	}

	public boolean okToBreak(@Nonnull final EntityPlayer player) {
		return player.capabilities.isCreativeMode || isOwner(player) || !(isAdminShop() || isOwned());
	}

	public boolean canConfigure(@Nonnull final EntityPlayer player) {
		return player.capabilities.isCreativeMode || isOwner(player);
	}

	public boolean isAdminShop() {
		return this.adminShop;
	}

	public void setAdminShop() {
		this.adminShop = true;
		this.owner = UNOWNED;
		this.ownerName = "";
		sendUpdates(true);
	}

	public boolean isOwned() {
		return !UNOWNED.equals(this.owner);
	}

	public boolean isOwner(@Nullable final EntityPlayer player) {
		if (player == null)
			return false;
		return this.owner.equals(player.getPersistentID()) || (this.adminShop && player.capabilities.isCreativeMode);
	}

	public void setOwner(@Nullable final EntityPlayer player) {
		if (player != null && !this.adminShop && !isOwner(player)) {
			this.owner = player.getPersistentID();
			this.ownerName = player.getName();
			sendUpdates(true);
		}
	}

	@Override
	public boolean shouldRefresh(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState oldState, @Nonnull final IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.owner = nbt.getUniqueId(NBT.OWNER);
		this.ownerName = nbt.getString(NBT.OWNER_NAME);
		this.adminShop = nbt.getBoolean(NBT.ADMIN_SHOP);
		ItemStack newMimic = new ItemStack(nbt.getCompoundTag(NBT.SKIN));
		if (newMimic == null || !isValidMimic(newMimic))
			newMimic = DEFAULT_SKIN;
		setMimic(newMimic, true);
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setUniqueId(NBT.OWNER, this.owner);
		nbt.setString(NBT.OWNER_NAME, this.ownerName);
		nbt.setBoolean(NBT.ADMIN_SHOP, this.adminShop);
		nbt.setTag(NBT.SKIN, this.mimic.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	private static class NBT {
		public static final String OWNER = "o";
		public static final String OWNER_NAME = "on";
		public static final String SKIN = "sk";
		public static final String ADMIN_SHOP = "as";
	}

}
