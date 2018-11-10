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

import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.item.magic.Devices;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

public class CreateMagicDevice extends LootFunction {

	protected final Devices.DeviceEntry device;

	protected CreateMagicDevice(@Nonnull final Devices.DeviceEntry device,
			@Nonnull final LootCondition[] conditionsIn) {
		super(conditionsIn);
		this.device = device;
	}

	@Override
	@Nonnull
	public ItemStack apply(@Nonnull final ItemStack stack, @Nonnull final Random rand,
			@Nonnull final LootContext context) {
		return this.device.copy().create().toItemStack();
	}

	public static class Serializer extends LootFunction.Serializer<CreateMagicDevice> {

		protected Serializer() {
			super(new ResourceLocation(ModInfo.MOD_ID, "create_magic_device"), CreateMagicDevice.class);
		}

		@Override
		public void serialize(@Nonnull final JsonObject object, @Nonnull final CreateMagicDevice function,
				@Nonnull final JsonSerializationContext serializationContext) {
			final JsonElement element = new Gson().toJsonTree(function.device, Devices.DeviceEntry.class);
			object.add("device", element);
		}

		@Override
		public CreateMagicDevice deserialize(@Nonnull final JsonObject object,
				@Nonnull final JsonDeserializationContext deserializationContext,
				@Nonnull final LootCondition[] conditions) {
			if (object.has("device")) {
				return new CreateMagicDevice(new Gson().fromJson(object.get("device"), Devices.DeviceEntry.class),
						conditions);
			}
			return new CreateMagicDevice(new Devices.DeviceEntry(), conditions);
		}

	}

}
