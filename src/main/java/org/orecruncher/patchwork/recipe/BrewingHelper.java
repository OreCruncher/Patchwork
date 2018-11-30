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
package org.orecruncher.patchwork.recipe;

import org.orecruncher.patchwork.ModOptions;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;

public class BrewingHelper {

	// For making the Flight Essence material
	@ItemStackHolder(value = "minecraft:splash_potion", nbt = "{Potion:\"minecraft:thick\"}")
	public static final ItemStack THICK_SPLASH_POTION = null;
	@ItemStackHolder("minecraft:ghast_tear")
	public static final ItemStack GHAST_TEAR = null;
	@ItemStackHolder("patchwork:flight_essence")
	public static final ItemStack FLIGHT_ESSENCE = null;

	public static void initialize() {
		if (ModOptions.items.enableRingOfFlight)
			BrewingRecipeRegistry.addRecipe(THICK_SPLASH_POTION, GHAST_TEAR, FLIGHT_ESSENCE);
	}

}
