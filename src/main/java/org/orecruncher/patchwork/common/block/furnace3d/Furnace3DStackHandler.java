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

package org.orecruncher.patchwork.common.block.furnace3d;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class Furnace3DStackHandler extends ItemStackHandler {

	public static final int INPUT_SLOT = 0;
	public static final int FUEL_SLOT = 1;
	public static final int OUTPUT_SLOT = 2;
	public static final int TOTAL_SLOTS = 3;

	private boolean isDirty;

	public Furnace3DStackHandler() {
		super(TOTAL_SLOTS);
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public void clearDirty() {
		this.isDirty = false;
	}

	public ItemStack getInputStack() {
		return getStackInSlot(INPUT_SLOT);
	}

	public ItemStack getOutputStack() {
		return getStackInSlot(OUTPUT_SLOT);
	}

	public ItemStack getFuelStack() {
		return getStackInSlot(FUEL_SLOT);
	}

	public void setInputStack(@Nonnull final ItemStack stack) {
		setStackInSlot(INPUT_SLOT, stack);
	}

	public void setOutputStack(@Nonnull final ItemStack stack) {
		setStackInSlot(OUTPUT_SLOT, stack);
	}

	public void setFuelStack(@Nonnull final ItemStack stack) {
		setStackInSlot(FUEL_SLOT, stack);
	}

	public int size() {
		return this.stacks.size();
	}

	public boolean isEmpty() {
		for (final ItemStack itemstack : this.stacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public void clear() {
		for (int i = 0; i < size(); i++)
			setStackInSlot(i, ItemStack.EMPTY);
	}

	public ItemStack getAndSplit(int index, int amount) {
		return index >= 0 && index < this.stacks.size() && !this.stacks.get(index).isEmpty() && amount > 0
				? this.stacks.get(index).splitStack(amount)
				: ItemStack.EMPTY;
	}

	public ItemStack getAndRemove(int index) {
		return index >= 0 && index < this.stacks.size() ? this.stacks.set(index, ItemStack.EMPTY) : ItemStack.EMPTY;
	}

	@Override
	protected void onContentsChanged(final int slot) {
		this.isDirty = true;
	}

}
