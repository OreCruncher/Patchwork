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

package org.blockartistry.doodads.common.item.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.ItemMagicDevice;
import net.minecraft.util.ResourceLocation;

public class MagicDevice {

	// Collect IDs for capabilities here
	public static final ResourceLocation ABILITY_FLIGHT = new ResourceLocation(ModInfo.MOD_ID, "flight");
	public static final ResourceLocation ABILITY_FIREBALL = new ResourceLocation(ModInfo.MOD_ID, "fireball");

	// Pre-built fancy devices
	public static final Map<String, MagicDevice> DEVICES = new HashMap<>();

	private final String name;
	private final List<ResourceLocation> abilities = new ArrayList<>();

	private final String unlocalizedName;
	private ItemMagicDevice.Quality quality = ItemMagicDevice.Quality.NORMAL;
	private ItemMagicDevice.Type type = ItemMagicDevice.Type.AMULET;

	public MagicDevice(@Nonnull final String name) {
		this.name = name;
		this.unlocalizedName = ModInfo.MOD_ID + ".magicdevice." + name + ".moniker";
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	@Nonnull
	public String getUnlocalizedName() {
		return this.unlocalizedName;
	}

	@Nonnull
	public MagicDevice addAbility(@Nonnull final ResourceLocation... ability) {
		for (final ResourceLocation r : ability)
			this.abilities.add(r);
		return this;
	}

	@Nonnull
	public List<ResourceLocation> getAbilities() {
		return this.abilities;
	}

	@Nonnull
	public ItemMagicDevice.Type getType() {
		return this.type;
	}

	@Nonnull
	public MagicDevice setType(@Nonnull final ItemMagicDevice.Type type) {
		this.type = type;
		return this;
	}

	@Nonnull
	public ItemMagicDevice.Quality getQuality() {
		return this.quality;
	}

	@Nonnull
	public MagicDevice setQuality(@Nonnull final ItemMagicDevice.Quality quality) {
		this.quality = quality;
		return this;
	}

	static {
		MagicDevice device = new MagicDevice("flight").setType(ItemMagicDevice.Type.AMULET)
				.setQuality(ItemMagicDevice.Quality.NORMAL).addAbility(ABILITY_FLIGHT);
		DEVICES.put(device.getName(), device);

		device = new MagicDevice("fireball").setType(ItemMagicDevice.Type.WAND)
				.setQuality(ItemMagicDevice.Quality.PRIZED).addAbility(ABILITY_FIREBALL);
		DEVICES.put(device.getName(), device);
	}
}
