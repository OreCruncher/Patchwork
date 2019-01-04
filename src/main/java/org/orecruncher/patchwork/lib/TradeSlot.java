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

import org.orecruncher.patchwork.ModInfo;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TradeSlot extends SlotPhantom {

	private static final ResourceLocation RESOURCE_AVAILABLE = new ResourceLocation(ModInfo.MOD_ID,
			"textures/gui/slot_resource_available.png");
	private static final ResourceLocation RESOURCE_NOT_AVAILABLE = new ResourceLocation(ModInfo.MOD_ID,
			"textures/gui/slot_resource_not_available.png");

	protected IResourceAvailableCheck available = null;
	protected boolean isInfinite;
	protected int stackLimit;

	public TradeSlot(final IInventory inventory, final int slotIndex, final int xPos, final int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		this.stackLimit = -1;
	}

	public TradeSlot setInfinite() {
		this.isInfinite = true;
		return this;
	}

	public TradeSlot setResourceCheck(final IResourceAvailableCheck check) {
		this.available = check;
		return this;
	}

	public TradeSlot setStackLimit(final int limit) {
		this.stackLimit = limit;
		return this;
	}

	public boolean isAvailable() {
		return this.available == null || this.available.isAvailable(this);
	}

	@Override
	public int getSlotStackLimit() {
		if (this.stackLimit < 0) {
			return super.getSlotStackLimit();
		} else {
			return this.stackLimit;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Nonnull
	public ResourceLocation getBackgroundLocation() {
		return isAvailable() ? RESOURCE_AVAILABLE : RESOURCE_NOT_AVAILABLE;
	}

	@Override
	public ItemStack decrStackSize(final int i) {
		if (!this.isInfinite) {
			return super.decrStackSize(i);
		}

		ItemStack stack = this.inventory.getStackInSlot(getSlotIndex());
		if (!stack.isEmpty()) {
			stack = stack.copy();
			stack.shrink(i);
		}
		return stack;
	}
	
	public static interface IResourceAvailableCheck {
		boolean isAvailable(final Slot slot);
	}

}
