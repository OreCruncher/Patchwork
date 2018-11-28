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

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.orecruncher.lib.Localization;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.lib.GuiContainerBase;
import org.orecruncher.patchwork.lib.SlotPhantom;
import org.orecruncher.patchwork.lib.TradeSlot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ShopShelfOwnerGui extends GuiContainerBase {

	private static final ResourceLocation BACKGROUND = new ResourceLocation(ModInfo.MOD_ID,
			"textures/gui/shopshelf_owner.png");

	protected final TileEntityShopShelf tile;

	public ShopShelfOwnerGui(final TileEntityShopShelf tile, final EntityPlayer player) {
		super(BACKGROUND, new ShopShelfContainer(tile, player, true));

		this.tile = tile;
		this.xSize = 175;
		this.ySize = 231;
	}

	@Override
	public String getTitle() {
		return Localization.loadString(this.tile.getName());
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		// note to self(signum returns +1 for positive numbers and -1 for negative
		// numbers, 0 for 0..)
		final int amt = Integer.signum(Mouse.getEventDWheel());
		if (amt != 0) {
			// The player scrolled the wheel
			final Slot slot = getSlotUnderMouse();
			if (slot instanceof SlotPhantom || slot instanceof TradeSlot) {
				// Don't want to drop below 1 or go above the stack amount
				final ItemStack stack = slot.getStack();
				if (amt < 0 && stack.getCount() < 2)
					return;
				if (amt > 0 && stack.getCount() == stack.getMaxStackSize())
					return;
				// Simulate a mouse click in the slot
				final int mouseButton = amt < 0 ? 0 : 1;

				handleMouseClick(slot, slot.slotNumber, mouseButton, ClickType.PICKUP);
			}
		}
	}
}
