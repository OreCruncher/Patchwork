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

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.item.ItemMaterial;
import org.orecruncher.patchwork.item.ModItems;

import com.google.gson.JsonObject;

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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class ToolRepairRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe {

	private static final ItemStack REPAIR_PASTE = new ItemStack(ModItems.MATERIAL, 1,
			ItemMaterial.Type.REPAIR_PASTE.getSubTypeId());
	private static final int REPAIR_AMOUNT = 50;

	private static boolean isTypeAcceptable(@Nonnull final Item item) {
		return item instanceof ItemArmor || item instanceof ItemTool || item instanceof ItemFishingRod
				|| item instanceof ItemHoe || item instanceof ItemShield || item instanceof ItemSword
				|| item instanceof ItemShears;
	}

	private static boolean isBrokenItem(final ItemStack stack) {
		return stack.getItem().isDamageable() && stack.isItemDamaged() && isTypeAcceptable(stack.getItem());
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
				foundPaste = true;
			} else if (isBrokenItem(ingredient)) {
				if (foundBrokenItem)
					return false;
				foundBrokenItem = true;
			}
		}
		return foundPaste && foundBrokenItem;
	}

	@Override
	@Nonnull
	public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
		// Just need to locate the broken item
		final int invSize = inv.getSizeInventory();
		ItemStack result = null;
		int pasteCount = 0;
		for (int i = 0; i < invSize; i++) {
			final ItemStack ingredient = inv.getStackInSlot(i);
			if (ingredient.isEmpty())
				continue;
			if (isBrokenItem(ingredient)) {
				result = ingredient.copy();
			} else if (ItemStack.areItemsEqual(REPAIR_PASTE, ingredient)) {
				pasteCount++;
			}
		}
		result.setItemDamage(Math.max(0, result.getItemDamage() - REPAIR_AMOUNT * pasteCount));
		return result;
	}

	@Override
	public boolean canFit(final int width, final int height) {
		return width * height > 1;
	}

	@Override
	@Nonnull
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	public static class Factory implements IRecipeFactory {
		@Override
		@Nonnull
		public IRecipe parse(@Nonnull final JsonContext context, @Nonnull final JsonObject json) {
			return new ToolRepairRecipe();
		}
	}

}
