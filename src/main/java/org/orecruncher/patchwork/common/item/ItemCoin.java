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

package org.orecruncher.patchwork.common.item;

import java.util.Comparator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.orecruncher.lib.IVariant;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.client.ModCreativeTab;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemCoin extends ItemBase implements IColorizer {

	public ItemCoin() {
		super("coin");
		setCreativeTab(ModCreativeTab.tab);
		setHasSubtypes(true);
	}

	@Override
	public void registerItemModel() {
		for (final Type bt : Type.values()) {
			ModBase.proxy().registerItemRenderer(this, bt.getSubTypeId(),
					new ModelResourceLocation(ModInfo.MOD_ID + ":coin", "inventory"));
		}
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			for (final Type t : Type.values())
				items.add(new ItemStack(this, 1, t.getSubTypeId()));
		}
	}

	@Override
	@Nonnull
	public String getTranslationKey(@Nonnull final ItemStack stack) {
		return Type.bySubTypeId(stack.getMetadata()).getUnlocalizedName();
	}

	@Override
	public int getColor(@Nonnull final ItemStack stack) {
		return Type.bySubTypeId(stack.getMetadata()).getColor();
	}

	public static enum Type implements IVariant {

		// #b87333
		COPPER(0, "copper", 12088115),
		// #cd7f32
		BRONZE(1, "bronze", 13467442),
		// #c0c0c0
		SILVER(2, "silver", 12632256),
		// #ffd700
		GOLD(3, "gold", 16766720),
		// #e5e4e2
		PLATINUM(4, "platinum", 5066338);

		private static final Type[] SUBTYPE_LOOKUP = Stream.of(values())
				.sorted(Comparator.comparing(Type::getSubTypeId)).toArray(Type[]::new);

		private final int color;
		private final String name;
		private final String unlocalizedName;
		private final int subTypeId;

		private Type(final int subTypeId, @Nonnull final String name, final int color) {
			this.subTypeId = subTypeId;
			this.name = name;
			this.color = color;
			this.unlocalizedName = "item." + ModInfo.MOD_ID + ".coin_" + this.name;
		}

		@Override
		@Nonnull
		public String getName() {
			return this.name;
		}

		@Nonnull
		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}

		@Override
		public int getSubTypeId() {
			return this.subTypeId;
		}

		public int getColor() {
			return this.color;
		}

		@Nonnull
		public static Type bySubTypeId(int subTypeId) {
			return SUBTYPE_LOOKUP[MathStuff.clamp(subTypeId, 0, SUBTYPE_LOOKUP.length - 1)];
		}

	}
}
