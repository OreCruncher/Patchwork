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
import org.orecruncher.patchwork.item.ItemRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.CapabilityRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.IRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.IRingOfFlightSettable;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class FlightRingRefuelRecipe extends RepairRecipeBase {

	public FlightRingRefuelRecipe() {
		super(() -> BrewingHelper.FLIGHT_ESSENCE, () -> ModOptions.ringOfFlight.refuelAmount);
	}

	@Override
	protected boolean isDamagedItem(@Nonnull final ItemStack stack) {
		if (stack.getItem() instanceof ItemRingOfFlight) {
			final IRingOfFlight cap = CapabilityRingOfFlight.getCapability(stack);
			final ItemRingOfFlight.Variant v = cap.getVariant();
			return cap.getRemainingDurability() < v.getMaxDurability();
		}
		return false;
	}

	@Override
	protected int getItemDamage(@Nonnull final ItemStack stack) {
		final IRingOfFlightSettable cap = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
		final ItemRingOfFlight.Variant v = cap.getVariant();
		return v.getMaxDurability() - cap.getRemainingDurability();
	}

	// It's operating on a copy
	@Override
	protected ItemStack fixItemDamage(@Nonnull final ItemStack stack, final int repairAmount) {
		final IRingOfFlightSettable cap = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
		final ItemRingOfFlight.Variant v = cap.getVariant();
		cap.setDurability(Math.min(v.getMaxDurability(), cap.getRemainingDurability() + repairAmount));
		return stack;
	}

	public static class Factory implements IRecipeFactory {
		@Override
		@Nonnull
		public IRecipe parse(@Nonnull final JsonContext context, @Nonnull final JsonObject json) {
			return new FlightRingRefuelRecipe();
		}
	}

}
