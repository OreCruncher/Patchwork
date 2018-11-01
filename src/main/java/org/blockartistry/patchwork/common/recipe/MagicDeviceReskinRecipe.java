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

package org.blockartistry.patchwork.common.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.patchwork.common.item.ItemMagicDevice;
import org.blockartistry.patchwork.common.item.magic.capability.IMagicDeviceSettable;

import com.google.gson.JsonObject;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class MagicDeviceReskinRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe {

	private boolean isProperIngredient(final ItemStack stack) {
		return stack.getItem() instanceof ItemMagicDevice;
	}

	@Override
	public boolean matches(@Nonnull final InventoryCrafting inv, @Nonnull final World worldIn) {
		boolean found = false;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			if (isProperIngredient(stack)) {
				if (found)
					return false;
				found = true;
			} else {
				// Came across something that shouldn't be there
				return false;
			}
		}
		return found;
	}

	// Looks like codewise a match has already been performed
	// and the ingredients are present in the grid. Theoretically
	// we do not have to validate it.
	@Override
	@Nullable
	public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (isProperIngredient(stack)) {
				final ItemStack result = stack.copy();
				final IMagicDeviceSettable caps = (IMagicDeviceSettable) ItemMagicDevice.getCapability(result);
				if (caps != null) {
					int newVariant = caps.getVariant() + 1;
					if (newVariant >= ((ItemMagicDevice) result.getItem()).getDeviceType(result).getVariants())
						newVariant = 0;
					caps.setVariant(newVariant);
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public boolean canFit(final int width, final int height) {
		return width * height > 0;
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
			return new MagicDeviceReskinRecipe();
		}
	}

}
