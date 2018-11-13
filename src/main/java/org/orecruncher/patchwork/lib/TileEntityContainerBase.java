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

package org.orecruncher.patchwork.lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockable;

public abstract class TileEntityContainerBase extends TileEntityLockable {

	protected abstract StackHandlerBase getInventory();

	@Override
	public int getSizeInventory() {
		return getInventory().size();
	}

	@Override
	public boolean isEmpty() {
		return getInventory().isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(final int index) {
		return getInventory().getStackInSlot(index);
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(final int index, final int count) {
		return getInventory().getAndSplit(index, count);
	}

	@Override
	@Nonnull
	public ItemStack removeStackFromSlot(final int index) {
		return getInventory().getAndRemove(index);
	}

	@Override
	public void setInventorySlotContents(final int index, @Nonnull final ItemStack stack) {
		getInventory().setStackInSlot(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull final EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
					this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void openInventory(@Nonnull final EntityPlayer player) {
		// Override to provide functionality
	}

	@Override
	public void closeInventory(@Nonnull final EntityPlayer player) {
		// Override to provide functionality
	}

	@Override
	public void clear() {
		getInventory().clear();
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getField(final int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// Ignore
	}

	@Override
	public int getFieldCount() {
		// Ignore
		return 0;
	}

	@Nonnull
	protected IBlockState getState() {
		return this.world.getBlockState(this.pos);
	}

	protected void sendUpdates(final boolean dirty) {
		sendUpdates(dirty, false);
	}

	protected void sendUpdates(final boolean dirty, final boolean forced) {
		if (!this.world.isRemote) {
			if (forced || getInventory().isDirty()) {
				this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
				this.world.notifyBlockUpdate(this.pos, getState(), getState(), 3);
				this.world.scheduleBlockUpdate(this.pos, getBlockType(), 0, 0);
				getInventory().clearDirty();
			}
			if (forced || dirty)
				markDirty();
		}
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		getInventory().deserializeNBT(nbt.getCompoundTag(NBT.INVENTORY));
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setTag(NBT.INVENTORY, getInventory().serializeNBT());
		return nbt;
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

	private static class NBT {
		public static final String INVENTORY = "_inv_";
	}
}
