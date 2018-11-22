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
package org.orecruncher.patchwork.item.ringofflight;

import java.util.Map;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.item.ItemRingOfFlight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerWings implements LayerRenderer<AbstractClientPlayer> {

	private static final float FLAP_RANGE_IDLE = 2F;
	private static final float FLAP_RANGE_FLYING = 20F;
	private static final float FLAP_SPEED_IDLE = 4F;
	private static final float FLAP_SPEED_FLYING = 2F;

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
		//@formatter:off
		null,
		new ResourceLocation(ModInfo.MOD_ID, "textures/items/ringofflight/wing_feather.png"),
		new ResourceLocation(ModInfo.MOD_ID, "textures/items/ringofflight/wing_sturdy.png"),
		new ResourceLocation(ModInfo.MOD_ID, "textures/items/ringofflight/wing_obsidian.png"),
		new ResourceLocation(ModInfo.MOD_ID, "textures/items/ringofflight/wing_speed.png")
		//@formatter:on
	};

	private final RenderPlayer renderPlayer;
	private int displayList = 0;

	public LayerWings(@Nonnull final RenderPlayer renderPlayer) {
		this.renderPlayer = renderPlayer;
	}

	@Override
	public void doRenderLayer(@Nonnull AbstractClientPlayer player, float p_177141_2_, float p_177141_3_,
			float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {

		final int textureId = getWingTexture(player);
		if (textureId == 0)
			return;

		GlStateManager.color(1, 1, 1, 1);

		GlStateManager.pushMatrix();
		final ModelRenderer bipedBody = this.renderPlayer.getMainModel().bipedBody;
		bipedBody.postRender(0.0625F);
		final float v = (bipedBody.cubeList.get(0).posZ2 - bipedBody.cubeList.get(0).posZ1) / 2;
		GlStateManager.translate(0.0F, player.isSneaking() ? 0.125F : 0, 0.0625F * v);

		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURES[textureId]);

		// Calculate the flap rotation based on existence, partial ticks, and some
		// trigonometry.
		final float age = player.ticksExisted + partialTicks;
		final boolean isFlying = player.capabilities.isFlying;
		final float flapRange = isFlying ? FLAP_RANGE_FLYING : FLAP_RANGE_IDLE;
		final float flapSpeed = isFlying ? FLAP_SPEED_FLYING : FLAP_SPEED_IDLE;
		final float a = (1 + MathStuff.cos(age / flapSpeed)) * flapRange + 25F;

		if (this.displayList == 0)
			this.displayList = generateDisplayList();

		// One wing...
		GlStateManager.pushMatrix();
		GlStateManager.rotate(-a, 0, 1, 0);
		GlStateManager.callList(this.displayList);
		GlStateManager.popMatrix();

		// ...and the other...
		GlStateManager.pushMatrix();
		GlStateManager.rotate(a, 0, 1, 0);
		GlStateManager.callList(this.displayList + 1);
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

	private static int generateDisplayList() {
		int list = 0;
		final Tessellator instance = Tessellator.getInstance();
		final BufferBuilder b = instance.getBuffer();
		list = GLAllocation.generateDisplayLists(2);
		GL11.glNewList(list, GL11.GL_COMPILE);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glTranslatef(0.0F, -0.25F - 0.0625F, 0);
		b.begin(7, DefaultVertexFormats.POSITION_TEX);
		b.pos(0, 0, 0).tex(0, 0).endVertex();
		b.pos(0, 1, 0).tex(0, 1).endVertex();
		b.pos(1, 1, 0).tex(1, 1).endVertex();
		b.pos(1, 0, 0).tex(1, 0).endVertex();
		instance.draw();
		GL11.glEndList();

		GL11.glNewList(list + 1, GL11.GL_COMPILE);
		b.begin(7, DefaultVertexFormats.POSITION_TEX);
		GL11.glTranslatef(0.0F, -0.25F - 0.0625F, 0);
		b.pos(0, 0, 0).tex(0, 0).endVertex();
		b.pos(0, 1, 0).tex(0, 1).endVertex();
		b.pos(-1, 1, 0).tex(1, 1).endVertex();
		b.pos(-1, 0, 0).tex(1, 0).endVertex();
		instance.draw();
		GL11.glEndList();
		return list;
	}

	private int getWingTexture(@Nonnull final EntityPlayer player) {
		final ItemStack stack = ItemRingOfFlight.getActiveRing(player);
		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		if (caps == null)
			return 0;
		return caps.getVariant().getSubTypeId();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

	public static void initialize() {
		final Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		addLayer(skinMap.get("default"));
		addLayer(skinMap.get("slim"));
	}

	private static void addLayer(@Nonnull final RenderPlayer rp) {
		rp.addLayer(new LayerWings(rp));
	}
}