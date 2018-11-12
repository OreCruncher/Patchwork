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

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryUtils {

	private static final Consumer<Integer> NO_CALLBACK = (@Nonnull final Integer t) -> {
	};

	/*
	 * Determines if the two ItemStacks can be combined. It does NOT take into
	 * account max stack sides and the like.
	 */
	public static boolean canCombine(@Nonnull final ItemStack stack1, @Nonnull final ItemStack stack2) {
		return (!stack1.isEmpty() && stack1.getItem() == stack2.getItem()
				&& (!stack2.getHasSubtypes() || stack2.getMetadata() == stack1.getMetadata())
				&& ItemStack.areItemStackTagsEqual(stack2, stack1));
	}

	/*
	 * Determines if the two ItemStacks in question can fit into the specified list.
	 * ItemStack.EMPTY can be provided if there is no appropriate stack.
	 */
	public static boolean canAccept(@Nonnull final NonNullList<ItemStack> list, @Nonnull final ItemStack stack1,
			@Nonnull final ItemStack stack2, final int slotStart, final int slotEnd) {

		if (stack1.isEmpty() && stack2.isEmpty())
			return true;

		if (canCombine(stack1, stack2)) {
			int count = stack1.getCount() + stack2.getCount();
			for (int i = slotStart; i <= slotEnd && count > 0; i++) {
				final ItemStack item = list.get(i);
				if (item.isEmpty()) {
					count -= stack1.getMaxStackSize();
				} else if (canCombine(stack1, item)) {
					count -= item.getMaxStackSize() - item.getCount();
				}
			}
			return count < 1;
		} else {
			// Two different types of stacks.
			int count1 = !stack1.isEmpty() ? stack1.getCount() : 0;
			int count2 = !stack2.isEmpty() ? stack2.getCount() : 0;
			for (int i = slotStart; i <= slotEnd && (count1 > 0 || count2 > 0); i++) {
				final ItemStack item = list.get(i);
				if (item.isEmpty()) {
					if (count1 > 0) {
						count1 -= stack1.getMaxStackSize();
					} else if (count2 > 0) {
						count2 -= stack2.getMaxStackSize();
					}
				} else if (canCombine(item, stack1)) {
					count1 -= item.getMaxStackSize() - item.getCount();
				} else if (canCombine(item, stack2)) {
					count2 -= item.getMaxStackSize() - item.getCount();
				}
			}
			return count1 < 1 && count2 < 1;
		}
	}

	public static void compact(@Nonnull final NonNullList<ItemStack> list, final int startSlot, final int endSlot) {
		compact(list, startSlot, endSlot, NO_CALLBACK);
	}

	public static void compact(@Nonnull final NonNullList<ItemStack> list, final int startSlot, final int endSlot,
			@Nonnull final Consumer<Integer> callback) {
		for (int i = startSlot + 1; i <= endSlot; i++) {
			final ItemStack stack = list.get(i);
			if (stack.isEmpty())
				continue;
			for (int j = startSlot; j < i; j++) {
				final ItemStack target = list.get(j);
				if (target.isEmpty()) {
					list.set(j, stack);
					callback.accept(j);
					list.set(i, ItemStack.EMPTY);
					callback.accept(i);
					break;
				} else if (InventoryUtils.canCombine(stack, target)) {
					final int hold = target.getMaxStackSize() - target.getCount();
					if (hold >= stack.getCount()) {
						target.grow(stack.getCount());
						callback.accept(j);
						list.set(i, ItemStack.EMPTY);
						callback.accept(i);
						break;
					} else if (hold != 0) {
						stack.shrink(hold);
						callback.accept(i);
						target.grow(hold);
						callback.accept(j);
					}
				}
			}
		}
	}

	/*
	 * Determines if both of the ItemStacks in question can be found in the
	 * specified list. ItemStack.EMPTY can be provided if there is no appropriate
	 * stack.
	 */
	public static boolean contains(@Nonnull final NonNullList<ItemStack> list, @Nonnull final ItemStack stack1,
			@Nonnull final ItemStack stack2, final int slotStart, final int slotEnd) {
		if (stack1.isEmpty() && stack2.isEmpty())
			return true;

		if (canCombine(stack1, stack2)) {
			int count = stack1.getCount() + stack2.getCount();
			for (int i = slotStart; i <= slotEnd && count > 0; i++) {
				final ItemStack item = list.get(i);
				if (canCombine(stack1, item)) {
					count -= item.getCount();
				}
			}
			return count < 1;
		} else {
			// Two different types of stacks. Have to handle nulls.
			int count1 = !stack1.isEmpty() ? stack1.getCount() : 0;
			int count2 = !stack2.isEmpty() ? stack2.getCount() : 0;
			for (int i = slotStart; i <= slotEnd && (count1 > 0 || count2 > 0); i++) {
				final ItemStack item = list.get(i);
				if (item.isEmpty())
					continue;
				if (ItemStack.areItemStacksEqual(item, stack1)) {
					count1 = 0;
				} else if (canCombine(item, stack1)) {
					count1 -= item.getCount();
				} else if (ItemStack.areItemStacksEqual(item, stack2)) {
					count2 = 0;
				} else if (canCombine(item, stack2)) {
					count2 -= item.getCount();
				}
			}
			return count1 < 1 && count2 < 1;
		}
	}

	/*
	 * Adds the specified ItemStack to the list, merging in as needed.
	 */
	public static boolean addItemStackToInventory(@Nonnull final NonNullList<ItemStack> list,
			@Nonnull final ItemStack stack, final int startSlot, final int endSlot) {
		return addItemStackToInventory(list, stack, startSlot, endSlot, NO_CALLBACK);
	}

	public static boolean addItemStackToInventory(@Nonnull final NonNullList<ItemStack> list,
			@Nonnull final ItemStack stack, final int startSlot, final int endSlot,
			@Nonnull final Consumer<Integer> callback) {
		if (stack.isEmpty())
			return true;
		for (int slot = startSlot; slot <= endSlot; slot++) {
			final ItemStack invStack = list.get(slot);
			// Quick and easy - if the slot is empty its the target
			if (invStack.isEmpty()) {
				list.set(slot, stack);
				callback.accept(slot);
				return true;
			}
			// If the stack can fit into this slot do the merge
			final int remainingSpace = invStack.getMaxStackSize() - invStack.getCount();
			if (remainingSpace > 0 && InventoryUtils.canCombine(stack, invStack)) {
				if (remainingSpace >= stack.getCount()) {
					invStack.grow(stack.getCount());
					callback.accept(slot);
					return true;
				}
				stack.shrink(remainingSpace);
				invStack.grow(remainingSpace);
				callback.accept(slot);
			}
		}
		return false;
	}

	/*
	 * Removes the specified ItemStack from the list. It will traverse the list
	 * until the quantity to be removed is satisfied.
	 */
	public static boolean removeItemStackFromInventory(@Nonnull final NonNullList<ItemStack> list,
			@Nonnull final ItemStack stack, final int startSlot, final int endSlot) {
		return removeItemStackFromInventory(list, stack, startSlot, endSlot, NO_CALLBACK);
	}

	public static boolean removeItemStackFromInventory(@Nonnull final NonNullList<ItemStack> list,
			@Nonnull final ItemStack stack, final int startSlot, final int endSlot,
			@Nonnull final Consumer<Integer> callback) {
		if (stack.isEmpty())
			return true;
		for (int slot = startSlot; slot <= endSlot && !stack.isEmpty(); slot++) {
			final ItemStack invStack = list.get(slot);
			if (!invStack.isEmpty()) {
				if (ItemStack.areItemStacksEqual(invStack, stack)) {
					list.set(slot, ItemStack.EMPTY);
					stack.setCount(0);
					callback.accept(slot);
				} else if (canCombine(invStack, stack)) {
					if (invStack.getCount() > stack.getCount()) {
						invStack.shrink(stack.getCount());
						stack.setCount(0);
					} else {
						stack.shrink(invStack.getCount());
						list.set(slot, ItemStack.EMPTY);
					}
					callback.accept(slot);
				}
			}
		}
		return stack.isEmpty();
	}

}
