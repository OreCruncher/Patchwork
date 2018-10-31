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
import javax.annotation.Nullable;

import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.common.item.ItemMagicDevice.Type;
import org.blockartistry.doodads.common.item.ModItems;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDeviceSettable;
import org.blockartistry.doodads.util.RecipeHelper;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.GameData;

public class MagicDeviceSkin extends ShapelessRecipes {

	public MagicDeviceSkin(String group, ItemStack output, NonNullList<Ingredient> ingredients) {
		super(group, output, ingredients);
	}

	private boolean isProperIngredient(final ItemStack stack) {
		return stack.getItem() instanceof ItemMagicDevice
				&& ((ItemMagicDevice) stack.getItem()).getDeviceType(stack).getVariants() > 1;
	}

	@Override
	public boolean matches(@Nonnull final InventoryCrafting inv, @Nonnull final World worldIn) {
		boolean found = false;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			if (isProperIngredient(inv.getStackInSlot(i))) {
				if (found)
					return false;
				found = true;
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

	public static void register() {
		for (final Type t : ItemMagicDevice.Type.values()) {
			if (t.getVariants() > 1) {
				final ItemStack input = new ItemStack(ModItems.MAGIC_DEVICE, 1, t.getSubTypeId());
				final ItemStack output = input.copy();
				final IMagicDeviceSettable caps = (IMagicDeviceSettable) ItemMagicDevice.getCapability(output);
				caps.setVariant(caps.getVariant() + 1);

				final ResourceLocation location = RecipeHelper.getNameForRecipe(output);
				final MagicDeviceSkin recipe = new MagicDeviceSkin(location.getResourceDomain(), output,
						RecipeHelper.buildInput(new Object[] { input }));
				recipe.setRegistryName(location);
				GameData.register_impl(recipe);

			}
		}
	}
}
