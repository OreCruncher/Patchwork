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

package org.orecruncher.patchwork.entity;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMagic extends Render<EntityMagic> {

	private final float scale;
	private boolean blend = false;

	public RenderMagic(@Nonnull final RenderManager renderManager, final float scale, final boolean doBlending) {
		super(renderManager);
		this.scale = scale;
		this.blend = doBlending;
		
		// No shadows
		this.shadowSize = 0F;
	}

	@Override
	public void doRender(@Nonnull final EntityMagic entity, final double par2, final double par4, final double par6, final float par8, final float par9) {

		GlStateManager.pushMatrix();
		bindTexture(entity.getTexture());
		GlStateManager.translate((float) par2, (float) par4, (float) par6);
		GlStateManager.enableRescaleNormal();
		if (this.blend) {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		final float f2 = this.scale;
		GlStateManager.scale(f2 / 1.0F, f2 / 1.0F, f2 / 1.0F);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();

		final float f3 = 0.0f;
		final float f4 = 1.0f;
		final float f5 = 0.0f;
		final float f6 = 1.0f;
		final float f7 = 1.0F;
		final float f8 = 0.5F;
		final float f9 = 0.25F;

		final float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? this.renderManager.playerViewX
				: -this.renderManager.playerViewX;
		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0.0F - f8, 0.0F - f9, 0.0D).tex(f3, f6).endVertex();
		buffer.pos(f7 - f8, 0.0F - f9, 0.0D).tex(f4, f6).endVertex();
		buffer.pos(f7 - f8, 1.0F - f9, 0.0D).tex(f4, f5).endVertex();
		buffer.pos(0.0F - f8, 1.0F - f9, 0.0D).tex(f3, f5).endVertex();
		tessellator.draw();

		GlStateManager.disableRescaleNormal();
		if (this.blend) {
			GlStateManager.disableBlend();
		}
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	@Nonnull
	protected ResourceLocation getEntityTexture(@Nonnull final EntityMagic entity) {
		return entity.getTexture();
	}
}