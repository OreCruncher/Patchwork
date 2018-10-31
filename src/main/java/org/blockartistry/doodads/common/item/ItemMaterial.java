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

import java.util.Comparator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.client.DoodadsCreativeTab;
import org.blockartistry.doodads.util.IVariant;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemMaterial extends ItemBase {

	public ItemMaterial() {
		super("material");
		setCreativeTab(DoodadsCreativeTab.tab);
	}

	@Override
	public void registerItemModel() {
		for (final Type bt : Type.values()) {
			Doodads.proxy().registerItemRenderer(this, bt.getMeta(),
					new ModelResourceLocation(ModInfo.MOD_ID + ":material", "type=" + bt.getName()));
		}
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			for (final Type t : Type.values())
				items.add(new ItemStack(this, 1, t.getMeta()));
		}
	}

	@Override
	@Nonnull
	public String getUnlocalizedName(@Nonnull final ItemStack stack) {
		return Type.byMetadata(stack.getMetadata()).getUnlocalizedName();
	}

	public static enum Type implements IVariant {

		REPAIR_PASTE(0, "repairpaste"), MAGICAL_AMALGAM(1, "magicalamalgam");

		private static final Type[] META_LOOKUP = Stream.of(values()).sorted(Comparator.comparing(Type::getMeta))
				.toArray(Type[]::new);

		private final String name;
		private final String unlocalizedName;
		private final int meta;

		private Type(final int meta, @Nonnull final String name) {
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = "item." + ModInfo.MOD_ID + ".material_" + this.name;
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
		public int getMeta() {
			return this.meta;
		}

		@Nonnull
		public static Type byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

	}

}
