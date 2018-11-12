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
		if (stack1.isEmpty() && stack2.isEmpty())
			return true;

		if (ItemStack.areItemStacksEqual(stack1, stack2)) {
			int count = stack1.getCount() + stack2.getCount();
			for (int i = slotStart; i <= slotEnd && count > 0; i++) {
				final ItemStack item = getStackInSlot(i);
				if (ItemStack.areItemStacksEqual(stack1, item)) {
					count -= item.getCount();
				}
			}
			return count < 1;
		} else {
			// Two different types of stacks. Have to handle nulls.
			int count1 = !stack1.isEmpty() ? stack1.getCount() : 0;
			int count2 = !stack2.isEmpty() ? stack2.getCount() : 0;
			for (int i = slotStart; i <= slotEnd && (count1 > 0 || count2 > 0); i++) {
				final ItemStack item = getStackInSlot(i);
				if (item.isEmpty())
					continue;
				if (ItemStack.areItemsEqual(item, stack1)) {
					count1 -= item.getCount();
				} else if (ItemStack.areItemsEqual(item, stack2)) {
					count2 -= item.getCount();
				}
			}
			return count1 < 1 && count2 < 1;
		}
	}

	/**
	 * Checks whether the specified inventory has enough space to accept the two
	 * sample ItemStacks. The range is [slotStart, slotEnd].
	 */
	public boolean canAccept(@Nonnull final ItemStack stack1, @Nonnull final ItemStack stack2, final int slotStart,
			final int slotEnd) {

		if (stack1.isEmpty() && stack2.isEmpty())
			return true;

		if (ItemStack.areItemsEqual(stack1, stack2)) {
			int count = stack1.getCount() + stack2.getCount();
			for (int i = slotStart; i <= slotEnd && count > 0; i++) {
				final ItemStack item = getStackInSlot(i);
				if (item.isEmpty()) {
					count -= stack1.getMaxStackSize();
				} else if (ItemStack.areItemsEqual(stack1, item)) {
					count -= item.getMaxStackSize() - item.getCount();
				}
			}
			return count < 1;
		} else {
			// Two different types of stacks.
			int count1 = !stack1.isEmpty() ? stack1.getCount() : 0;
			int count2 = !stack2.isEmpty() ? stack2.getCount() : 0;
			for (int i = slotStart; i <= slotEnd && (count1 > 0 || count2 > 0); i++) {
				final ItemStack item = getStackInSlot(i);
				if (item.isEmpty()) {
					if (count1 > 0) {
						count1 -= stack1.getMaxStackSize();
					} else if (count2 > 0) {
						count2 -= stack2.getMaxStackSize();
					}
				} else if (ItemStack.areItemsEqual(item, stack1)) {
					count1 -= item.getMaxStackSize() - item.getCount();
				} else if (ItemStack.areItemsEqual(item, stack2)) {
					count2 -= item.getMaxStackSize() - item.getCount();
				}
			}
			return count1 < 1 && count2 < 1;
		}
	}

	/**
	 * Compacts the inventory list by consolidating stacks toward the beginning of
	 * the array. This operation occurs in place meaning the return array is the
	 * original one passed in. The range is [slotStart, slotEnd].
	 */
	public void compact(final int startSlot, final int endSlot) {
		for (int i = startSlot + 1; i <= endSlot; i++) {
			final ItemStack stack = getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			for (int j = startSlot; j < i; j++) {
				final ItemStack target = getStackInSlot(j);
				if (target.isEmpty()) {
					setStackInSlot(j, stack);
					setStackInSlot(i, ItemStack.EMPTY);
					break;
				} else if (ItemStack.areItemsEqual(stack, target)) {
					final int hold = target.getMaxStackSize() - target.getCount();
					if (hold >= stack.getCount()) {
						target.grow(stack.getCount());
						onContentsChanged(j);
						setStackInSlot(i, ItemStack.EMPTY);
						break;
					} else if (hold != 0) {
						stack.shrink(hold);
						onContentsChanged(i);
						target.grow(hold);
						onContentsChanged(j);
					}
				}
			}
		}
	}

	public boolean addItemStackToInventory(final ItemStack stack, final int startSlot, final int endSlot) {
		if (stack.isEmpty())
			return true;
		for (int slot = startSlot; slot <= endSlot; slot++) {
			final ItemStack invStack = getStackInSlot(slot);
			// Quick and easy - if the slot is empty its the target
			if (invStack.isEmpty()) {
				setStackInSlot(slot, stack);
				return true;
			}
			// If the stack can fit into this slot do the merge
			final int remainingSpace = invStack.getMaxStackSize() - invStack.getCount();
			if (remainingSpace > 0 && ItemStack.areItemsEqual(stack, invStack)) {
				if (remainingSpace >= stack.getCount()) {
					invStack.grow(stack.getCount());
					onContentsChanged(slot);
					return true;
				}
				stack.shrink(remainingSpace);
				invStack.grow(remainingSpace);
				onContentsChanged(slot);
			}
		}
		return false;
	}

	public boolean removeItemStackFromInventory(final ItemStack stack, final int startSlot, final int endSlot) {
		if (stack.isEmpty())
			return true;
		for (int slot = startSlot; slot <= endSlot && !stack.isEmpty(); slot++) {
			final ItemStack invStack = getStackInSlot(slot);
			if (!invStack.isEmpty() && ItemStack.areItemsEqual(invStack, stack)) {
				if (invStack.getCount() > stack.getCount()) {
					invStack.shrink(stack.getCount());
					onContentsChanged(slot);
					stack.setCount(0);
				} else {
					stack.shrink(invStack.getCount());
					setStackInSlot(slot, ItemStack.EMPTY);
				}
			}
		}
		return stack.isEmpty();
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
