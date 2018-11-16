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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBase<T extends TileEntityContainerBase> extends Container {

	protected static final int PLAYER_HOTBAR_SIZE = 9;
	protected static final int PLAYER_CHEST_SIZE = 27;
	protected static final int PLAYER_INVENTORY_SIZE = PLAYER_HOTBAR_SIZE + PLAYER_CHEST_SIZE;
	protected static final int GUI_INVENTORY_CELL_SIZE = 18;

	protected final T tile;

	public ContainerBase(final T tile) {
		this.tile = tile;
	}

	@Override
	public boolean canInteractWith(@Nonnull final EntityPlayer player) {
		return this.tile.isUsableByPlayer(player);
	}

	protected void addPlayerInventory(final InventoryPlayer inv, final int guiHeight) {

		int yOffset = guiHeight - 82;

		// Add the player inventory
		for (int i = 0; i < PLAYER_CHEST_SIZE; ++i) {

			// The constants are offsets from the left and top edge
			// of the GUI
			final int h = (i % PLAYER_HOTBAR_SIZE) * GUI_INVENTORY_CELL_SIZE + 8;
			final int v = (i / PLAYER_HOTBAR_SIZE) * GUI_INVENTORY_CELL_SIZE + yOffset;

			// We offset by 9 to skip the hotbar slots - they
			// come next
			addSlotToContainer(new Slot(inv, i + 9, h, v));
		}

		yOffset = guiHeight - 24;
		// Add the hotbar
		for (int i = 0; i < PLAYER_HOTBAR_SIZE; ++i) {
			addSlotToContainer(new Slot(inv, i, 8 + i * GUI_INVENTORY_CELL_SIZE, yOffset));
		}
	}

	public boolean mergeToPlayerInventory(@Nonnull final ItemStack stack) {
		final int sizeInventory = this.tile.getSizeInventory();
		return mergeItemStack(stack, sizeInventory, sizeInventory + PLAYER_INVENTORY_SIZE, false);
	}

	@Override
	public ItemStack slotClick(final int slotIndex, final int button, final ClickType modifier,
			final EntityPlayer player) {
		final Slot slot = slotIndex < 0 ? null : this.inventorySlots.get(slotIndex);
		if (slot instanceof SlotPhantom) {
			final SlotPhantom ts = (SlotPhantom) slot;
			return slotClickMultiSlot(ts, button, modifier, player);
		}
		return super.slotClick(slotIndex, button, modifier, player);
	}

	private ItemStack slotClickMultiSlot(final SlotPhantom slot, final int mouseButton, final ClickType modifier,
			final EntityPlayer player) {
		ItemStack stack = ItemStack.EMPTY;

		final ItemStack stackSlot = slot.getStack();
		if (!stackSlot.isEmpty()) {
			stack = stackSlot.copy();
		}

		if (mouseButton == 2) {
			fillPhantomSlot(slot, ItemStack.EMPTY, mouseButton, modifier);
		} else if (mouseButton == 0 || mouseButton == 1) {
			final InventoryPlayer playerInv = player.inventory;

			final ItemStack stackHeld = playerInv.getItemStack();

			if (stackSlot.isEmpty()) {
				if (!stackHeld.isEmpty() && slot.isItemValid(stackHeld)) {
					fillPhantomSlot(slot, stackHeld, mouseButton, modifier);
				}
			} else if (stackHeld.isEmpty()) {
				adjustPhantomSlot(slot, mouseButton, modifier);
			} else if (slot.isItemValid(stackHeld)) {
				if (ItemStack.areItemStacksEqual(stackSlot, stackHeld)) {
					adjustPhantomSlot(slot, mouseButton, modifier);
				} else {
					fillPhantomSlot(slot, stackHeld, mouseButton, modifier);
				}
			}
		} else if (mouseButton == 5) {
			final InventoryPlayer playerInv = player.inventory;
			final ItemStack stackHeld = playerInv.getItemStack();
			if (!slot.getHasStack()) {
				fillPhantomSlot(slot, stackHeld, mouseButton, modifier);
			}
		}
		return stack;
	}

	protected void adjustPhantomSlot(final SlotPhantom slot, final int mouseButton, final ClickType modifier) {
		if (slot.isLocked()) {
			return;
		}
		
		ItemStack stackSlot = slot.getStack();
		int stackSize;
		// TODO: Used to be "1" - need to figure out the mousing. Maybe it means shift
		// is being held?
		if (modifier == ClickType.QUICK_MOVE) {
			stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
		} else {
			stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
		}

		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}

		if (stackSize <= 0)
			stackSlot = ItemStack.EMPTY;
		else
			stackSlot.setCount(stackSize);

		slot.putStack(stackSlot);
	}

	protected void fillPhantomSlot(@Nonnull final SlotPhantom slot, @Nonnull final ItemStack stackHeld,
			final int mouseButton, final ClickType modifier) {
		if (slot.isLocked()) {
			return;
		}

		if (stackHeld.isEmpty()) {
			slot.putStack(ItemStack.EMPTY);
			return;
		}

		int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}
		final ItemStack phantomStack = stackHeld.copy();
		phantomStack.setCount(stackSize);

		slot.putStack(phantomStack);
	}

}
