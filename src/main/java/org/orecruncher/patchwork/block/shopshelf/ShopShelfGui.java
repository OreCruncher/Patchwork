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

import org.orecruncher.lib.Localization;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.lib.GuiContainerBase;
import org.orecruncher.patchwork.lib.TradeSlot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public final class ShopShelfGui extends GuiContainerBase {

	private static final ResourceLocation BACKGROUND = new ResourceLocation(ModInfo.MOD_ID,
			"textures/gui/shopshelf.png");
	private static final ResourceLocation RESOURCE_AVAILABLE = new ResourceLocation(ModInfo.MOD_ID,
			"textures/gui/slot_resource_available.png");
	private static final ResourceLocation RESOURCE_NOT_AVAILABLE = new ResourceLocation(ModInfo.MOD_ID,
			"textures/gui/slot_resource_not_available.png");

	protected final TileEntityShopShelf tile;
	
	public ShopShelfGui(final TileEntityShopShelf tile, final EntityPlayer player) {
		super(BACKGROUND, new ShopShelfContainer(tile, player));

		this.tile = tile;
		this.xSize = 175;
		this.ySize = 165;
	}

	@Override
	public String getTitle() {
		return Localization.loadString(tile.getName());
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float partialTick, final int mouseX, final int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		// Loop through the slots looking for TradeSlots so we can
		// render the resource available background colors
		for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); i1++) {
			final Slot slot = this.inventorySlots.inventorySlots.get(i1);
			if (slot instanceof TradeSlot && slot.getHasStack()) {
				final TradeSlot ts = (TradeSlot) slot;
				final int x = this.guiLeft + ts.xPos;
				final int y = this.guiTop + ts.yPos;
				this.mc.renderEngine.bindTexture(ts.isAvailable() ? RESOURCE_AVAILABLE : RESOURCE_NOT_AVAILABLE);
				this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
			}
		}
	}
}
