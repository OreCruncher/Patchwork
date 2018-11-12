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
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class GuiContainerBase extends GuiContainer {

	protected final ResourceLocation texture;

	public GuiContainerBase(@Nonnull final ResourceLocation texture, @Nonnull final Container container) {
		super(container);

		this.texture = texture;
	}

	@Nullable
	public String getTitle() {
		return null;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		final String title = getTitle();
		if (!StringUtils.isEmpty(title)) {
			this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6,
					4210752);
			// this.fontRenderer.drawString(this.title, 8, this.ySize - 96 + 2, 4210752);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, final int mouseX, final int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		bindTexture(this.texture);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		
		// Loop through the slots looking for TradeSlots so we can
		// render the resource available background colors
		for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); i1++) {
			final Slot slot = this.inventorySlots.inventorySlots.get(i1);
			if (slot instanceof TradeSlot && slot.getHasStack()) {
				final TradeSlot ts = (TradeSlot) slot;
				final int x = this.guiLeft + ts.xPos;
				final int y = this.guiTop + ts.yPos;
				this.mc.renderEngine.bindTexture(ts.getBackgroundLocation());
				this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
			}
		}
	}

	public void bindTexture(@Nonnull final ResourceLocation texture) {
		this.mc.renderEngine.bindTexture(texture);
	}
}
