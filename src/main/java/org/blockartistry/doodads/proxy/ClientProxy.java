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

package org.blockartistry.doodads.proxy;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.common.item.ItemBase;
import org.blockartistry.doodads.common.loot.LootRegistrationHandler;
import org.blockartistry.doodads.common.recipe.RepairPasteRecipe;
import org.blockartistry.doodads.util.Localization;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		Localization.initialize(Side.CLIENT, Doodads.MOD_ID);
	}
	
	@Override
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		RepairPasteRecipe.register();
		LootRegistrationHandler.initialize();
	}

	@Override
	public void registerItemRenderer(@Nonnull final ItemBase item, final int meta, @Nonnull final String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta,
				new ModelResourceLocation(Doodads.MOD_ID + ":" + id, "inventory"));
	}
}
