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

import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModOptions;

import com.google.gson.JsonObject;

import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class EnableDisableCondition {

	private static final BooleanSupplier TRUE = () -> true;

	private static final BooleanSupplier FALSE = () -> false;

	public static class Factory implements IConditionFactory {
		@Override
		@Nonnull
		public BooleanSupplier parse(@Nonnull final JsonContext context, @Nonnull final JsonObject json) {
			if (json.has("option")) {
				final String option = json.get("option").getAsString();
				switch (option) {
				case "coins":
					return ModOptions.items.enableCoins ? TRUE : FALSE;
				case "mobnet":
					return ModOptions.items.enableMobnet ? TRUE : FALSE;
				case "furnace":
					return ModOptions.items.enableFurnace ? TRUE : FALSE;
				case "shopshelf":
					return ModOptions.items.enableShopShelf ? TRUE : FALSE;
				case "tools":
					return ModOptions.items.enableTools ? TRUE : FALSE;
				case "toolrepair":
					return (ModOptions.items.enableToolRepair && ModOptions.items.enableTools) ? TRUE : FALSE;
				case "ringofflight":
					return ModOptions.items.enableRingOfFlight ? TRUE : FALSE;
				case "refuelring":
					return (ModOptions.items.enableRingOfFlight && ModOptions.items.enableTools) ? TRUE : FALSE;
				case "wood_pile":
					return ModOptions.items.enableWoodPile ? TRUE : FALSE;
				default:
					ModBase.log().warn("Unknown recipe option [%s]", option);
					break;
				}
			}
			ModBase.log().warn("Recipe has a condition but no recognized 'option' tag!");
			return TRUE;
		}

	}

}
