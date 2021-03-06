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

package org.orecruncher.patchwork.item;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBase extends Item {

	protected final String name;
	protected boolean hasEffect;

	public ItemBase(@Nonnull final String name) {
		this.name = name;
		setRegistryName(this.name);
		setTranslationKey(ModInfo.MOD_ID + "." + this.name);

		// Let the registration handler know about this
		// new item.
		ItemRegistrationHandler.add(this);
	}

	public void registerItemModel() {
		ModBase.proxy().registerItemRenderer(this, 0,
				new ModelResourceLocation(ModInfo.MOD_ID + ":" + this.name, "inventory"));
	}

	@Override
	@Nonnull
	public ItemBase setCreativeTab(@Nonnull final CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}
	
	@Nonnull
	public ItemBase setHasEffect(final boolean flag) {
		this.hasEffect = flag;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(@Nonnull final ItemStack stack) {
		return this.hasEffect || super.hasEffect(stack);
	}

}
