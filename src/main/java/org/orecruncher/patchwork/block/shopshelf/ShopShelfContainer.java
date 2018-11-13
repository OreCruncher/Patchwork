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
import org.orecruncher.patchwork.lib.InventoryUtils;
import org.orecruncher.patchwork.lib.SlotLocked;
import org.orecruncher.patchwork.lib.SlotPhantom;
import org.orecruncher.patchwork.lib.StackHandlerBase;
import org.orecruncher.patchwork.lib.TradeSlot;
import org.orecruncher.patchwork.lib.TradeSlot.IResourceAvailableCheck;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ShopShelfContainer extends ContainerBase<TileEntityShopShelf> implements IResourceAvailableCheck {

	protected static final int CONFIG_SLOT_START = ShopShelfStackHandler.CONFIG_SLOT_START;
	protected static final int CONFIG_SLOT_END = ShopShelfStackHandler.CONFIG_SLOT_END;
	protected static final int INVENTORY_SLOT_START = ShopShelfStackHandler.INVENTORY_SLOT_START;
	protected static final int INVENTORY_SLOT_END = ShopShelfStackHandler.INVENTORY_SLOT_END;
	protected static final int TOTAL_SLOTS = ShopShelfStackHandler.TOTAL_SLOTS;

	protected final boolean asOwner;

	public ShopShelfContainer(@Nonnull final TileEntityShopShelf tile, @Nonnull final EntityPlayer player) {
		this(tile, player, false);
	}

	public ShopShelfContainer(@Nonnull final TileEntityShopShelf tile, @Nonnull final EntityPlayer player,
			final boolean asOwner) {
		super(tile);

		this.asOwner = asOwner;

		// Add configuration slots
		for (int i = 0; i < 6; i++) {

			final int slotBase = CONFIG_SLOT_START + (i * 3);
			final int x = (i < 3) ? 17 : 97;
			final int y = (i < 3) ? i * GUI_INVENTORY_CELL_SIZE + 17 : (i - 3) * GUI_INVENTORY_CELL_SIZE + 17;

			if (asOwner) {
				SlotPhantom slot = new SlotPhantom(tile, slotBase, x, y);
				slot.setLocked(false).setCanShift(true);
				addSlotToContainer(slot);

				slot = new SlotPhantom(tile, slotBase + 1, x + GUI_INVENTORY_CELL_SIZE, y);
				slot.setLocked(false).setCanShift(true);
				addSlotToContainer(slot);

				final TradeSlot ts = new TradeSlot(tile, slotBase + 2, x + GUI_INVENTORY_CELL_SIZE * 2 + 9, y);
				ts.setResourceCheck(this).setLocked(false).setCanShift(true);
				addSlotToContainer(ts);
			} else {
				Slot slot = new SlotLocked(tile, slotBase, x, y);
				addSlotToContainer(slot);

				slot = new SlotLocked(tile, slotBase + 1, x + GUI_INVENTORY_CELL_SIZE, y);
				addSlotToContainer(slot);

				final TradeSlot ms = new TradeSlot(tile, slotBase + 2, x + GUI_INVENTORY_CELL_SIZE * 2 + 9, y);
				ms.setInfinite().setResourceCheck(this);
				addSlotToContainer(ms);
			}
		}

		if (asOwner) {
			final int yOffset = 84;
			// Add vending machine inventory
			for (int i = 0; i < PLAYER_CHEST_SIZE; ++i) {
				final int slotBase = INVENTORY_SLOT_START + i;
				// The constants are offsets from the left and top edge
				// of the GUI
				final int h = (i % PLAYER_HOTBAR_SIZE) * GUI_INVENTORY_CELL_SIZE + 8;
				final int v = (i / PLAYER_HOTBAR_SIZE) * GUI_INVENTORY_CELL_SIZE + yOffset;
				addSlotToContainer(new Slot(tile, slotBase, h, v));
			}
		}

		// Add the player inventory
		addPlayerInventory(player.inventory, asOwner ? 232 : 166);
	}

	@Override
	@Nonnull
	public ItemStack slotClick(final int slotIndex, final int dragType, @Nonnull final ClickType clickTypeIn,
			@Nonnull final EntityPlayer player) {
		if (!this.asOwner) {
			final Slot slot = slotIndex < 0 ? null : this.inventorySlots.get(slotIndex);
			if (slot instanceof TradeSlot) {
				final TradeSlot ts = (TradeSlot) slot;
				return doTrade(ts, player);
			}
		}
		return super.slotClick(slotIndex, dragType, clickTypeIn, player);
	}

	@Nonnull
	protected ItemStack doTrade(@Nonnull final TradeSlot slot, @Nonnull final EntityPlayer player) {

		// If there is nothing available then what is there to do
		if (!slot.isAvailable())
			return ItemStack.EMPTY;

		final NonNullList<ItemStack> playerInv = player.inventory.mainInventory;
		final StackHandlerBase chest = this.tile.getInventory();

		// Get the result of the potential trade. If there is nothing,
		// or the player inventory cannot hold the stack then return.
		final ItemStack result = slot.getStack();
		if (result.isEmpty() || !InventoryUtils.canAccept(playerInv, result, ItemStack.EMPTY, 0, playerInv.size() - 1))
			return ItemStack.EMPTY;

		// Capture whether the shelf is running in normal or admin mode
		// makes a difference as to whether items are placed/removed
		// from the internal chest since admin shops have infinite
		// product.
		final boolean normalMode = !this.tile.isAdminShop();

		// Get the input slots. We do some math on the slot index to
		// figure out the right input slots.
		final int index = (slot.slotNumber / 3) * 3 + CONFIG_SLOT_START;
		final ItemStack input1 = getSlot(index).getStack();
		final ItemStack input2 = getSlot(index + 1).getStack();

		// If both are empty stacks then its free
		if (!input1.isEmpty() || !input2.isEmpty()) {
			// See if the vending inventory can accept the required items
			if (normalMode && !chest.canAccept(input1, input2, INVENTORY_SLOT_START, INVENTORY_SLOT_END))
				return ItemStack.EMPTY;

			// See if the player can provide the payment
			if (!InventoryUtils.contains(playerInv, input1, input2, 0, playerInv.size() - 1))
				return ItemStack.EMPTY;

			// OK - things should work. Do the transaction.
			if (!input1.isEmpty()) {
				if (normalMode) {
					chest.addItemStackToInventory(input1.copy(), INVENTORY_SLOT_START, INVENTORY_SLOT_END);
				}
				// Use a copy because we want the original intact
				InventoryUtils.removeItemStackFromInventory(playerInv, input1.copy(), 0, playerInv.size() - 1);
			}
			if (!input2.isEmpty()) {
				if (normalMode) {
					chest.addItemStackToInventory(input2.copy(), INVENTORY_SLOT_START, INVENTORY_SLOT_END);
				}
				// Use a copy because we want the original intact
				InventoryUtils.removeItemStackFromInventory(playerInv, input2.copy(), 0, playerInv.size() - 1);
			}
		}

		if (normalMode) {
			// Use a copy because we want the original intact
			chest.removeItemStackFromInventory(result.copy(), INVENTORY_SLOT_START, INVENTORY_SLOT_END);
		}

		player.inventory.addItemStackToInventory(result.copy());
		player.onUpdate();

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int slotIndex) {

		// Shift+click mode not available when it isn't being configured
		if (!this.asOwner)
			return ItemStack.EMPTY;

		ItemStack stack = null;
		final Slot slot = this.inventorySlots.get(slotIndex);

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slot != null && slot.getHasStack()) {

			final ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();

			// If it is one of the general container slots, move to player
			// inventory.
			if (slotIndex >= INVENTORY_SLOT_START && slotIndex < ShopShelfStackHandler.TOTAL_SLOTS) {
				if (!mergeToPlayerInventory(stackInSlot)) {
					return null;
				}
			} else if (slotIndex >= ShopShelfStackHandler.TOTAL_SLOTS) {
				// Try moving to the input slot
				if (!mergeItemStack(stackInSlot, INVENTORY_SLOT_START, TOTAL_SLOTS, false)) {
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

	@Override
	public boolean isAvailable(@Nonnull final Slot slot) {
		if (this.tile.isAdminShop())
			return true;
		final ItemStack stack = slot.getStack();
		return this.tile.getInventory().contains(stack, ItemStack.EMPTY, INVENTORY_SLOT_START, INVENTORY_SLOT_END);
	}

}
