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
import org.orecruncher.patchwork.lib.StackHandlerBase;
import org.orecruncher.patchwork.lib.TileEntityContainerBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;

public class TileEntityShopShelf extends TileEntityContainerBase {

	@ItemStackHolder(value = "minecraft:oak_planks", meta = 0)
	public static final ItemStack DEFAULT_SKIN = ItemStack.EMPTY;
	protected static final UUID UNOWNED = new UUID(0, 0);

	protected UUID owner = UNOWNED;
	protected String ownerName = "";
	protected ItemStack skin = DEFAULT_SKIN;
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
		sendUpdates(true, true);
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
			sendUpdates(true, true);
		}
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.owner = nbt.getUniqueId(NBT.OWNER);
		this.ownerName = nbt.getString(NBT.OWNER_NAME);
		this.adminShop = nbt.getBoolean(NBT.ADMIN_SHOP);
		this.skin = new ItemStack(nbt.getCompoundTag(NBT.SKIN));
		if (this.skin.isEmpty())
			this.skin = DEFAULT_SKIN;
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setUniqueId(NBT.OWNER, this.owner);
		nbt.setString(NBT.OWNER_NAME, this.ownerName);
		nbt.setBoolean(NBT.ADMIN_SHOP, this.adminShop);
		nbt.setTag(NBT.SKIN, this.skin.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	private static class NBT {
		public static final String OWNER = "o";
		public static final String OWNER_NAME = "on";
		public static final String SKIN = "sk";
		public static final String ADMIN_SHOP = "as";
	}

}
