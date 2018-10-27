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

package org.blockartistry.doodads.common.recipe;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.ModItems;
import org.blockartistry.doodads.util.RecipeHelper;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.GameData;

public final class RepairPasteRecipe extends ShapelessRecipes {

	private static final ItemStack REPAIR_PASTE = new ItemStack(ModItems.REPAIR_PASTE);
	private static final int REPAIR_AMOUNT = 50;

	public RepairPasteRecipe(@Nonnull final String group, @Nonnull final ItemStack output,
			@Nonnull final NonNullList<Ingredient> ingredients) {
		super(group, output, ingredients);
	}

	private boolean isBrokenItem(final ItemStack stack) {
		return stack.getItem().isDamageable() && stack.isItemDamaged();
	}

	@Override
	public boolean matches(@Nonnull final InventoryCrafting inv, @Nonnull final World worldIn) {
		boolean foundPaste = false;
		boolean foundBrokenItem = false;
		final int invSize = inv.getSizeInventory();
		for (int i = 0; i < invSize; i++) {
			final ItemStack ingredient = inv.getStackInSlot(i);
			if (ingredient.isEmpty())
				continue;
			if (ItemStack.areItemsEqual(REPAIR_PASTE, ingredient)) {
				if (foundPaste)
					return false;
				foundPaste = true;
			} else if (isBrokenItem(ingredient)) {
				if (foundBrokenItem)
					return false;
				foundBrokenItem = true;
			}
		}
		return foundPaste && foundBrokenItem;
	}

	// Looks like codewise a match has already been performed
	// and the ingredients are present in the grid. Theoretically
	// we do not have to validate it.
	@Override
	public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
		// Just need to locate the broken item
		final int invSize = inv.getSizeInventory();
		for (int i = 0; i < invSize; i++) {
			final ItemStack ingredient = inv.getStackInSlot(i);
			if (isBrokenItem(ingredient)) {
				final ItemStack result = ingredient.copy();
				result.setItemDamage(Math.max(0, result.getItemDamage() - REPAIR_AMOUNT));
				return result;
			}
		}
		return null;
	}

	// Scans the item registry looking for items that repair paste can be applied
	// to. Limited
	// to tools, armor, etc. that can be damaged.
	public static void register() {
		for (final ResourceLocation r : Item.REGISTRY.getKeys()) {
			final Item item = Item.REGISTRY.getObject(r);
			if (item.isDamageable()) {
				if (item instanceof ItemArmor || item instanceof ItemTool || item instanceof ItemFishingRod
						|| item instanceof ItemHoe || item instanceof ItemShield || item instanceof ItemSword
						|| item instanceof ItemShears) {
					final ItemStack input = new ItemStack(item);
					final ItemStack output = input.copy();
					final ResourceLocation location = RecipeHelper.getNameForRecipe(output);
					final RepairPasteRecipe recipe = new RepairPasteRecipe(location.getResourceDomain(), output,
							RecipeHelper.buildInput(new Object[] { input, REPAIR_PASTE }));
					recipe.setRegistryName(location);
					GameData.register_impl(recipe);
				}
			}
		}

	}
}
