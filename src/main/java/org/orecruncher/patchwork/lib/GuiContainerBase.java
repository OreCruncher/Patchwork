package org.orecruncher.patchwork.lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
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
	}

	public void bindTexture(@Nonnull final ResourceLocation texture) {
		this.mc.renderEngine.bindTexture(texture);
	}
}
