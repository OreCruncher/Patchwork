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

import java.util.AbstractMap.SimpleEntry;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.common.item.ItemMaterial;
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

//https://github.com/Xalcon/ChocoCraft3/tree/master/src/main/java/net/xalcon/chococraft/common/crafting
//https://github.com/Choonster-Minecraft-Mods/TestMod3/tree/d064915183a4a3b803d779576f982279268b1ca3/src/main/java/choonster/testmod3/crafting/recipe
//https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/d064915183a4a3b803d779576f982279268b1ca3/src/main/resources/assets/testmod3/recipes/_factories.json

public final class RepairPasteRecipe extends ShapelessRecipes {

	private static final ItemStack REPAIR_PASTE = new ItemStack(ModItems.MATERIAL, 1,
			ItemMaterial.Type.REPAIR_PASTE.getMeta());
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
	@Nullable
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

	private static boolean isTypeAcceptable(@Nonnull final Item item) {
		return item instanceof ItemArmor || item instanceof ItemTool || item instanceof ItemFishingRod
				|| item instanceof ItemHoe || item instanceof ItemShield || item instanceof ItemSword
				|| item instanceof ItemShears;
	}

	// Scans the item registry looking for items that repair paste can be applied
	// to. Limited to tools, armor, etc. that can be damaged.
	public static void register() {

		StreamSupport.stream(Item.REGISTRY.spliterator(), false).filter(i -> i.isDamageable() && isTypeAcceptable(i))
				.map(i -> new ItemStack(i))
				.map(is -> new SimpleEntry<>(new ItemStack(is.getItem(), 1, is.getMaxDamage() / 2), is)).forEach(e -> {
					// key is the input stack, value is the output stack
					final ResourceLocation location = RecipeHelper.getNameForRecipe(e.getValue());
					final RepairPasteRecipe recipe = new RepairPasteRecipe(location.getResourceDomain(), e.getValue(),
							RecipeHelper.buildInput(new Object[] { e.getKey(), REPAIR_PASTE }));
					recipe.setRegistryName(location);
					GameData.register_impl(recipe);
				});

	}
}
