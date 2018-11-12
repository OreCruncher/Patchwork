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

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class StackHandlerBase extends ItemStackHandler {

	private boolean isDirty;

	public StackHandlerBase(final int totalSlots) {
		super(totalSlots);
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public void clearDirty() {
		this.isDirty = false;
	}

	public int size() {
		return this.stacks.size();
	}

	public boolean isEmpty() {
		for (final ItemStack itemstack : this.stacks)
			if (!itemstack.isEmpty())
				return false;
		return true;
	}

	public void clear() {
		for (int i = 0; i < size(); i++)
			setStackInSlot(i, ItemStack.EMPTY);
	}

	/**
	 * Checks the provided inventory and slot range as to whether there is enough
	 * material equivalent to the two sample ItemStacks. The range is [slotStart,
	 * slotEnd].
	 */
	public boolean contains(@Nonnull final ItemStack stack1, @Nonnull final ItemStack stack2, final int slotStart,
			final int slotEnd) {
		return InventoryUtils.contains(this.stacks, stack1, stack2, slotStart, slotEnd);
	}

	/**
	 * Checks whether the specified inventory has enough space to accept the two
	 * sample ItemStacks. The range is [slotStart, slotEnd].
	 */
	public boolean canAccept(@Nonnull final ItemStack stack1, @Nonnull final ItemStack stack2, final int slotStart,
			final int slotEnd) {
		return InventoryUtils.canAccept(this.stacks, stack1, stack2, slotStart, slotEnd);
	}

	/**
	 * Compacts the inventory list by consolidating stacks toward the beginning of
	 * the array. This operation occurs in place meaning the return array is the
	 * original one passed in. The range is [slotStart, slotEnd].
	 */
	public void compact(final int startSlot, final int endSlot) {
		InventoryUtils.compact(this.stacks, startSlot, endSlot,
				(@Nonnull final Integer t) -> StackHandlerBase.this.onContentsChanged(t));
	}

	public boolean addItemStackToInventory(final ItemStack stack, final int startSlot, final int endSlot) {
		return InventoryUtils.addItemStackToInventory(this.stacks, stack, startSlot, endSlot,
				(@Nonnull final Integer t) -> StackHandlerBase.this.onContentsChanged(t));
	}

	public boolean removeItemStackFromInventory(final ItemStack stack, final int startSlot, final int endSlot) {
		return InventoryUtils.removeItemStackFromInventory(this.stacks, stack, startSlot, endSlot,
				(@Nonnull final Integer t) -> StackHandlerBase.this.onContentsChanged(t));
	}

	@Nonnull
	public ItemStack getAndSplit(final int index, final int amount) {
		ItemStack result = ItemStack.EMPTY;
		if (amount > 0) {
			final ItemStack original = getStackInSlot(index);
			if (!original.isEmpty()) {
				result = original.splitStack(amount);
				onContentsChanged(index);
			}
		}
		return result;
	}

	@Nonnull
	public ItemStack getAndRemove(final int index) {
		final ItemStack result = getStackInSlot(index);
		if (!result.isEmpty())
			setStackInSlot(index, ItemStack.EMPTY);
		return result;
	}

	@Override
	protected void onContentsChanged(final int slot) {
		this.isDirty = true;
	}

}
