/*
 * This file is part of Doodads, licensed under the MIT License (MIT).
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

package org.blockartistry.doodads.common.item;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.client.DoodadsCreativeTab;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public class ItemCoin extends ItemBase implements IColorizer {

	public static enum Type {
		// #b87333
		COPPER(12088115),
		// #cd7f32
		BRONZE(13467442),
		// #c0c0c0
		SILVER(12632256),
		// #ffd700
		GOLD(16766720),
		// #e5e4e2
		PLATINUM(5066338);

		private final int color;

		private Type(@Nonnull final int color) {
			this.color = color;
		}

		public int getColor() {
			return this.color;
		}

		public static int getColor(final String id) {
			final Type t = Type.valueOf(id.toUpperCase());
			return t == null ? 0 : t.getColor();
		}

	};

	private final int color;

	public ItemCoin(@Nonnull final Type type) {
		super("coin_" + type.name().toLowerCase());
		setCreativeTab(DoodadsCreativeTab.tab);

		this.color = type.getColor();
	}

	@Override
	public void registerItemModel() {
		Doodads.proxy().registerItemRenderer(this, 0, new ModelResourceLocation(ModInfo.MOD_ID + ":coin", "inventory"));
	}

	@Override
	public int getColor() {
		return this.color;
	}

}
