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

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.lib.ContainerBase;
import org.orecruncher.patchwork.lib.TradeSlot;
import org.orecruncher.patchwork.lib.TradeSlot.IResourceAvailableCheck;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ShopShelfOwnerContainer extends ContainerBase<TileEntityShopShelf> implements IResourceAvailableCheck {

	public ShopShelfOwnerContainer(@Nonnull final TileEntityShopShelf tile, @Nonnull final EntityPlayer player) {
		super(tile);

		final int yOffset = 84;

		// Add configuration slots
		for (int i = 0; i < 6; i++) {

			final int slotBase = ShopShelfStackHandler.CONFIG_SLOT_OFFSET + i;
			final int x = (i < 3) ? 17 : 97;
			final int y = (i < 3) ? i * GUI_INVENTORY_CELL_SIZE + 17 : (i - 3) * GUI_INVENTORY_CELL_SIZE + 17;

			TradeSlot slot = new TradeSlot(tile, slotBase, x, y);
			slot.setPhantom().setCanAdjustPhantom(true).setCanShift(true);
			addSlotToContainer(slot);

			slot = new TradeSlot(tile, slotBase + 6, x + GUI_INVENTORY_CELL_SIZE, y);
			slot.setPhantom().setCanAdjustPhantom(true).setCanShift(true);
			addSlotToContainer(slot);

			slot = new TradeSlot(tile, slotBase + 12, x + GUI_INVENTORY_CELL_SIZE * 2 + 9, y);
			slot.setPhantom().setCanAdjustPhantom(true).setCanShift(true);
			addSlotToContainer(slot);
		}

		// Add vending machine inventory
		for (int i = 0; i < PLAYER_CHEST_SIZE; ++i) {
			final int slotBase = ShopShelfStackHandler.INVENTORY_SLOT_OFFSET + i;

			// The constants are offsets from the left and top edge
			// of the GUI
			final int h = (i % PLAYER_HOTBAR_SIZE) * GUI_INVENTORY_CELL_SIZE + 8;
			final int v = (i / PLAYER_HOTBAR_SIZE) * GUI_INVENTORY_CELL_SIZE + yOffset;

			addSlotToContainer(new Slot(tile, slotBase, h, v));
		}

		// Add the player inventory
		addPlayerInventory(player.inventory, 232);

	}

	@Override
	public boolean isAvailable(@Nonnull final Slot slot) {
		if (this.tile.isAdminShop())
			return true;
		final int endSlot = ShopShelfStackHandler.INVENTORY_SLOT_OFFSET + ShopShelfStackHandler.INVENTORY_SLOT_COUNT
				- 1;
		final ItemStack stack = slot.getStack();
		return this.tile.getInventory().contains(stack, ItemStack.EMPTY, ShopShelfStackHandler.INVENTORY_SLOT_OFFSET,
				endSlot);
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int slotIndex) {

		ItemStack stack = null;
		final Slot slot = this.inventorySlots.get(slotIndex);

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slot != null && slot.getHasStack()) {

			final ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();

			// If it is one of the general container slots, move to player
			// inventory.
			if (slotIndex >= ShopShelfStackHandler.INVENTORY_SLOT_OFFSET
					&& slotIndex < ShopShelfStackHandler.TOTAL_SLOTS) {
				if (!mergeToPlayerInventory(stackInSlot)) {
					return null;
				}
			} else if (slotIndex >= ShopShelfStackHandler.TOTAL_SLOTS) {
				// Try moving to the input slot
				if (!mergeItemStack(stackInSlot, ShopShelfStackHandler.INVENTORY_SLOT_OFFSET,
						ShopShelfStackHandler.TOTAL_SLOTS, false)) {
					return null;
				}
			}

			// Cleanup the stack
			if (stackInSlot.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			}

			// Nothing changed
			if (stackInSlot.getCount() == stack.getCount()) {
				return null;
			}
		}

		return stack;
	}

}