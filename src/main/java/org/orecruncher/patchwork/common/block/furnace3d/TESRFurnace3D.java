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

package org.orecruncher.patchwork.common.block.furnace3d;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class TESRFurnace3D extends TileEntitySpecialRenderer<TileEntityFurnace3D> {

	private static final float SCALE = 0.7F;

	private enum SlotHelper {
		//
		INPUT(Furnace3DStackHandler.INPUT_SLOT, 0.4F, 0.4F, 0.3F),
		//
		OUTPUT(Furnace3DStackHandler.OUTPUT_SLOT, 0.4F, 0.4F, 0.7F),
		//
		FUEL(Furnace3DStackHandler.FUEL_SLOT, 0.4F, -0.1F, 0.5F);

		private final int slot;
		private final float xOffset;
		private final float yOffset;
		private final float zOffset;

		private SlotHelper(final int slot, final float dX, final float dY, final float dZ) {
			this.slot = slot;
			this.xOffset = dX;
			this.yOffset = dY;
			this.zOffset = dZ;
		}

		public ItemStack getStack(@Nonnull final TileEntityFurnace3D te) {
			return te.getStackInSlot(this.slot);
		}

		public float offsetX() {
			return this.xOffset;
		}

		public float offsetY() {
			return this.yOffset;
		}

		public float offsetZ() {
			return this.zOffset;
		}
	}
	
	private final EntityItem mock;
	
	public TESRFurnace3D() {
		this.mock = new EntityItem(null);
		this.mock.hoverStart = 0;
	}

	@Override
	public void render(@Nonnull final TileEntityFurnace3D te, final double x, final double y, final double z,
			final float partialTicks, final int destroyStage, final float alpha) {

		if (te.isEmpty())
			return;
		
        final int i = te.getWorld().getCombinedLight(te.getPos().up(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536, i / 65536);

        for (final SlotHelper slot : SlotHelper.values()) {
			final ItemStack stack = slot.getStack(te);
			if (!stack.isEmpty()) {
				this.mock.setItem(stack);
				GlStateManager.pushMatrix();
	            GlStateManager.translate(x + slot.offsetX(), y + slot.offsetY(), z + slot.offsetZ());
	            GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
	            GlStateManager.scale(SCALE, SCALE, SCALE);
	            Minecraft.getMinecraft().getRenderManager().renderEntity(this.mock, 0, 0, 0, 0F, 0, false);
		        GlStateManager.popMatrix();
		 	}
		}
	}

}
