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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModInfo;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public class ItemRegistrationHandler {

	// Items are added whenever ItemBase gets constructed
	private static Set<ItemBase> items = new HashSet<>();

	@Nonnull
	public static ItemBase add(@Nonnull final ItemBase item) {
		items.add(item);
		return item;
	}

	@SubscribeEvent
	public static void registerItems(@Nonnull final RegistryEvent.Register<Item> evt) {

		// Initialize any items before we go through and do registration
		ModItems.initialize();

		final IForgeRegistry<Item> r = evt.getRegistry();

		for (final ItemBase item : items) {
			r.registerAll(item);
		}
	}

	@SubscribeEvent
	public static void registerModels(@Nonnull final ModelRegistryEvent event) {
		for (final ItemBase item : items) {
			item.registerItemModel();
		}
	}

	@SubscribeEvent
	public static void registerColorizers(@Nonnull final ColorHandlerEvent.Item event) {
		for (final ItemBase item : items) {
			if (item instanceof IColorizer) {
				final IItemColor c = (@Nonnull final ItemStack stack,
						final int tintIndex) -> ((IColorizer) (stack.getItem())).getColor(stack);
				event.getItemColors().registerItemColorHandler(c, item);
			}
		}
	}

}
