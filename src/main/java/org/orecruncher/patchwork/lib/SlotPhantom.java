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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPhantom extends Slot {

	protected int slotIndex = 0;
	protected boolean locked = true;
	protected boolean canShift = false;

	public SlotPhantom(@Nonnull final IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.slotIndex = index;
	}

	@Override
	public boolean canTakeStack(@Nonnull final EntityPlayer player) {
		return false;
	}

	@Override
	public boolean isItemValid(@Nonnull final ItemStack stack) {
		return true;
	}

	public boolean isLocked() {
		return this.locked;
	}

	public boolean canShift() {
		return this.canShift;
	}

	@SuppressWarnings("unchecked")
	public <T extends SlotPhantom> T setLocked(final boolean f) {
		this.locked = f;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends SlotPhantom> T setCanShift(final boolean f) {
		this.canShift = f;
		return (T) this;
	}

}