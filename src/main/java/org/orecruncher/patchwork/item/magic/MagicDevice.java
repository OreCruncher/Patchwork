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

package org.orecruncher.patchwork.item.magic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.item.ModItems;
import org.orecruncher.patchwork.item.magic.Devices.DeviceEntry;
import org.orecruncher.patchwork.item.magic.capability.CapabilityMagicDevice;
import org.orecruncher.patchwork.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MagicDevice {

	private final List<ResourceLocation> abilities = new ArrayList<>();
	private String name;
	private ItemMagicDevice.Quality quality = ItemMagicDevice.Quality.NORMAL;
	private ItemMagicDevice.Type type = ItemMagicDevice.Type.AMULET;
	private int variant;

	@Nonnull
	public String getName() {
		return this.name;
	}
	
	@Nonnull
	public MagicDevice setName(@Nonnull final String name) {
		this.name = name;
		return this;
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

	public int getVariant() {
		return MathStuff.clamp(this.variant, 0, this.type.getVariants() - 1);
	}

	@Nonnull
	public MagicDevice setType(@Nonnull final ItemMagicDevice.Type type) {
		this.type = type;
		return this;
	}

	public void setVariant(final int variant) {
		this.variant = variant;
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
	
	@Nonnull
	public ItemStack toItemStack() {
		final ItemStack stack = new ItemStack(ModItems.MAGIC_DEVICE, 1, this.getType().getSubTypeId());
		final IMagicDeviceSettable xface = (IMagicDeviceSettable) stack
				.getCapability(CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
		xface.setMoniker(this.getName());
		xface.setVariant(this.getVariant());
		xface.setQuality(this.getQuality());
		xface.setCurrentEnergy(xface.getMaxEnergy());
		for (final ResourceLocation r : this.getAbilities())
			xface.addAbilities(r);
		return stack;
	}

	@Nonnull
	public static List<MagicDevice> getBuiltDevices() {
		final List<MagicDevice> result = new ArrayList<>();
		// Load up some prebuilt items from the JAR configuration
		try {
			final Devices devices = JsonUtils.loadFromJar(Devices.class, "/assets/patchwork/data/magic_devices.json");
			for (final DeviceEntry entry : devices.entries) {
				final MagicDevice md = entry.create();
				if (md != null)
					result.add(md);
			}
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Unable to load device info from JAR", t);
		}
		return result;
	}

}
