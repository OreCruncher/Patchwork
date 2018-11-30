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

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;

public abstract class RepairRecipeBase extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe {

	@ItemStackHolder(value = "patchwork:tools")
	public static final ItemStack TOOLS = ItemStack.EMPTY;

	protected final Supplier<Integer> repairAmount;
	protected final Supplier<ItemStack> repairMaterial;

	public RepairRecipeBase(@Nonnull final Supplier<ItemStack> repairMaterial,
			@Nonnull final Supplier<Integer> repairAmount) {
		this.repairMaterial = repairMaterial;
		this.repairAmount = repairAmount;
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
			if (this.repairMaterial.get().getItem() == ingredient.getItem()) {
				foundPaste = true;
			} else if (TOOLS.getItem() == ingredient.getItem()) {
				if (foundTools)
					return false;
				foundTools = true;
			} else if (isDamagedItem(ingredient)) {
				if (foundBrokenItem)
					return false;
				foundBrokenItem = true;
			}
		}
		return foundPaste && foundBrokenItem && foundTools;
	}

	// Need to provide method to identify the damaged item in the crafting grid
	protected abstract boolean isDamagedItem(@Nonnull final ItemStack stack);

	@Nonnull
	protected ItemStack findDamagedItem(@Nonnull final InventoryCrafting inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (TOOLS.getItem() != stack.getItem() && isDamagedItem(stack))
				return stack;
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	protected ItemStack findTools(@Nonnull final InventoryCrafting inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (TOOLS.getItem() == stack.getItem())
				return stack;
		}
		return ItemStack.EMPTY;
	}

	protected int countMaterial(@Nonnull final InventoryCrafting inv) {
		int result = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (this.repairMaterial.get().getItem() == stack.getItem())
				result += stack.getCount();
		}
		return result;
	}

	protected int getItemDamage(@Nonnull final ItemStack stack) {
		return stack.getItemDamage();
	}

	// It's operating on a copy
	protected ItemStack fixItemDamage(@Nonnull final ItemStack stack, final int repairAmount) {
		stack.setItemDamage(Math.max(0, getItemDamage(stack) - repairAmount));
		return stack;
	}

	@Override
	@Nonnull
	public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
		// Just need to locate the damaged item
		final ItemStack broken = findDamagedItem(inv).copy();
		final ItemStack tools = findTools(inv);
		final int materialAmount = countMaterial(inv);

		final int REPAIR_AMOUNT = this.repairAmount.get();

		// How much material is needed to fully repair item
		final int materialNeeded = (getItemDamage(broken) / REPAIR_AMOUNT) + 1;
		// How much can be applied with the current tool
		final int canBeApplied = tools.getMaxDamage() - tools.getItemDamage();
		// Figure out how much material can be applied
		final int materialToApply = Math.min(Math.min(materialNeeded, canBeApplied), materialAmount);

		fixItemDamage(broken, REPAIR_AMOUNT * materialToApply);
		return broken;
	}

	@Override
	@Nonnull
	public NonNullList<ItemStack> getRemainingItems(@Nonnull final InventoryCrafting inv) {
		final ItemStack broken = findDamagedItem(inv).copy();
		final ItemStack tools = findTools(inv).copy();
		final int materialAmount = countMaterial(inv);

		final int REPAIR_AMOUNT = this.repairAmount.get();

		// How much material is needed to fully repair item
		final int materialNeeded = (getItemDamage(broken) / REPAIR_AMOUNT) + 1;
		// How much can be applied with the current tool
		final int canBeApplied = tools.getMaxDamage() - tools.getItemDamage();
		// Figure out how much material can be applied
		final int materialToApply = Math.min(Math.min(materialNeeded, canBeApplied), materialAmount);

		final NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		int materialRemaining = materialToApply;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (TOOLS.getItem() == stack.getItem()) {
				tools.setItemDamage(tools.getItemDamage() + materialToApply);
				if (tools.getItemDamage() < tools.getMaxDamage())
					ret.set(i, tools);
			} else if (this.repairMaterial.get().getItem() == stack.getItem()) {
				inv.setInventorySlotContents(i, ItemStack.EMPTY);
				if (materialRemaining < stack.getCount()) {
					final ItemStack t = stack.copy();
					t.shrink(materialRemaining);
					ret.set(i, t);
					materialRemaining = 0;
				} else {
					materialRemaining -= stack.getCount();
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

}
