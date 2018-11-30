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

import org.orecruncher.patchwork.ModOptions;

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
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;

public class ToolRepairRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe {

	@ItemStackHolder(value = "patchwork:repairpaste")
	public static final ItemStack REPAIR_PASTE = ItemStack.EMPTY;
	@ItemStackHolder(value = "patchwork:tools")
	public static final ItemStack TOOLS = ItemStack.EMPTY;

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
		boolean foundTools = false;
		final int invSize = inv.getSizeInventory();
		for (int i = 0; i < invSize; i++) {
			final ItemStack ingredient = inv.getStackInSlot(i);
			if (ingredient.isEmpty())
				continue;
			if (REPAIR_PASTE.getItem() == ingredient.getItem()) {
				foundPaste = true;
			} else if (TOOLS.getItem() == ingredient.getItem()) {
				if (foundTools)
					return false;
				foundTools = true;
			} else if (isBrokenItem(ingredient)) {
				if (foundBrokenItem)
					return false;
				foundBrokenItem = true;
			}
		}
		return foundPaste && foundBrokenItem && foundTools;
	}

	private static ItemStack findBrokenTool(@Nonnull final InventoryCrafting inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (TOOLS.getItem() != stack.getItem() && isBrokenItem(stack))
				return stack;
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack findTools(@Nonnull final InventoryCrafting inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (TOOLS.getItem() == stack.getItem())
				return stack;
		}
		return ItemStack.EMPTY;
	}

	private static int countPaste(@Nonnull final InventoryCrafting inv) {
		int result = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (REPAIR_PASTE.getItem() == stack.getItem())
				result += stack.getCount();
		}
		return result;
	}

	@Override
	@Nonnull
	public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
		// Just need to locate the broken item
		final ItemStack broken = findBrokenTool(inv).copy();
		final ItemStack tools = findTools(inv);
		final int pasteAmount = countPaste(inv);

		final int REPAIR_AMOUNT = ModOptions.repairPaste.repairAmount;
		
		// How much paste is needed to fully repair item
		final int pasteNeeded = (broken.getItemDamage() / REPAIR_AMOUNT) + 1;
		// How much can be applied with the current tool
		final int canBeApplied = tools.getMaxDamage() - tools.getItemDamage();
		// Figure out how much paste can be applied
		final int pasteToApply = Math.min(Math.min(pasteNeeded, canBeApplied), pasteAmount);

		broken.setItemDamage(Math.max(0, broken.getItemDamage() - REPAIR_AMOUNT * pasteToApply));
		return broken;
	}

	@Override
	@Nonnull
	public NonNullList<ItemStack> getRemainingItems(@Nonnull final InventoryCrafting inv) {
		final ItemStack broken = findBrokenTool(inv).copy();
		final ItemStack tools = findTools(inv).copy();
		final int pasteAmount = countPaste(inv);

		final int REPAIR_AMOUNT = ModOptions.repairPaste.repairAmount;
		
		// How much paste is needed to fully repair item
		final int pasteNeeded = (broken.getItemDamage() / REPAIR_AMOUNT) + 1;
		// How much can be applied with the current tool
		final int canBeApplied = tools.getMaxDamage() - tools.getItemDamage();
		// Figure out how much paste can be applied
		final int pasteToApply = Math.min(Math.min(pasteNeeded, canBeApplied), pasteAmount);

		final NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		int pasteRemaining = pasteToApply;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (TOOLS.getItem() == stack.getItem()) {
				tools.setItemDamage(tools.getItemDamage() + pasteToApply);
				if (tools.getItemDamage() < tools.getMaxDamage())
					ret.set(i, tools);
			} else if (REPAIR_PASTE.getItem() == stack.getItem()) {
				inv.setInventorySlotContents(i, ItemStack.EMPTY);
				if (pasteRemaining < stack.getCount()) {
					final ItemStack t = stack.copy();
					t.shrink(pasteRemaining);
					ret.set(i, t);
					pasteRemaining = 0;
				} else {
					pasteRemaining -= stack.getCount();
				}
			} else {
				ret.set(i, ForgeHooks.getContainerItem(stack));
			}
		}

		return ret;
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
