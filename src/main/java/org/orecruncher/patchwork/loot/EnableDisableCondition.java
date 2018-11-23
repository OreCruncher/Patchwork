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

package org.orecruncher.patchwork.loot;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.ModOptions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class EnableDisableCondition implements LootCondition {

	private static final EnableDisableCondition TRUE = new EnableDisableCondition(true);
	private static final EnableDisableCondition FALSE = new EnableDisableCondition(false);

	private final boolean value;

	private EnableDisableCondition(final boolean v) {
		this.value = v;
	}

	@Override
	public boolean testCondition(Random rand, LootContext context) {
		return this.value;
	}

	public static class Serializer extends LootCondition.Serializer<EnableDisableCondition> {
		protected Serializer() {
			super(new ResourceLocation(ModInfo.MOD_ID, "isEnabled"), EnableDisableCondition.class);
		}

		@Override
		public void serialize(@Nonnull JsonObject json, @Nonnull EnableDisableCondition value,
				@Nonnull JsonSerializationContext context) {
		}

		@Nonnull
		@Override
		public EnableDisableCondition deserialize(@Nonnull final JsonObject json,
				@Nonnull final JsonDeserializationContext context) {
			if (json.has("option")) {
				final String option = json.get("option").getAsString();
				switch (option) {
				case "coins":
					return ModOptions.items.enableCoins ? TRUE : FALSE;
				case "repairpaste":
					return (ModOptions.items.enableToolRepair && ModOptions.items.enableTools) ? TRUE : FALSE;
				case "tools":
					return ModOptions.items.enableTools ? TRUE : FALSE;
				case "ringofflight":
					return ModOptions.items.enableRingOfFlight ? TRUE : FALSE;
				case "magnet":
					return ModOptions.items.enableItemMagnet ? TRUE : FALSE;
				default:
					ModBase.log().warn("Unknown LootTable option [%s]", option);
					break;
				}
			}
			ModBase.log().warn("LootTable has a condition but no recognized 'option' tag!");
			return TRUE;
		}
	}

}