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
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.WeightTable;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.patchwork.ModBase;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import net.minecraft.util.ResourceLocation;

// Useful command for testing loot tables:
// 	/setblock ~ ~2 ~ minecraft:chest 1 0 {LootTable:"patchwork:chests/village_blacksmith"}
public class Devices {

	private static final WeightTable<ItemMagicDevice.Type> TYPE = new WeightTable<>();
	private static final WeightTable<ItemMagicDevice.Quality> QUALITY = new WeightTable<>();
	private static final EnumMap<ItemMagicDevice.Type, WeightTable<AbilityHandler>> ABILITIES = new EnumMap<>(
			ItemMagicDevice.Type.class);

	static {
		TYPE.add(ItemMagicDevice.Type.AMULET, 10);
		TYPE.add(ItemMagicDevice.Type.BELT, 10);
		TYPE.add(ItemMagicDevice.Type.BODY, 10);
		TYPE.add(ItemMagicDevice.Type.CHARM, 10);
		TYPE.add(ItemMagicDevice.Type.HEAD, 10);
		TYPE.add(ItemMagicDevice.Type.RING, 10);
		TYPE.add(ItemMagicDevice.Type.ROD, 10);
		TYPE.add(ItemMagicDevice.Type.SCROLL, 10);
		TYPE.add(ItemMagicDevice.Type.STAFF, 10);
		TYPE.add(ItemMagicDevice.Type.WAND, 10);

		QUALITY.add(ItemMagicDevice.Quality.MUNDANE, 60);
		QUALITY.add(ItemMagicDevice.Quality.NORMAL, 45);
		QUALITY.add(ItemMagicDevice.Quality.PRIZED, 20);
		QUALITY.add(ItemMagicDevice.Quality.LEGENDARY, 5);

		for (final ItemMagicDevice.Type t : ItemMagicDevice.Type.values()) {
			final WeightTable<AbilityHandler> wt = new WeightTable<>();
			for (final AbilityHandler handler : AbilityHandler.queryForType(t)) {
				wt.add(handler, handler.getWeight());
			}
			ABILITIES.put(t, wt);
		}
	}

	public static class DeviceEntry {
		@SerializedName("name")
		public String name;
		@SerializedName("type")
		public ItemMagicDevice.Type type;
		@SerializedName("variant")
		public Integer variant;
		@SerializedName("quality")
		public ItemMagicDevice.Quality quality;
		@SerializedName("abilities")
		public List<String> abilities = new ArrayList<>();

		private void ensureComplete() {
			if (this.type == null)
				this.type = TYPE.next();
			if (this.variant == null)
				this.variant = XorShiftRandom.current().nextInt(this.type.getVariants());
			if (this.quality == null)
				this.quality = QUALITY.next();
			if (this.abilities.size() == 0) {
				final WeightTable<AbilityHandler> wt = ABILITIES.get(this.type);
				final int count = this.quality.getMaxAbilities();
				for (int i = 0; i < count; i++) {
					final String handler = wt.next().getRegistryName().toString();
					if (!this.abilities.contains(handler))
						this.abilities.add(handler);
				}
			}
		}

		@Nonnull
		public DeviceEntry copy() {
			final DeviceEntry result = new DeviceEntry();
			result.name = this.name;
			result.type = this.type;
			result.variant = this.variant;
			result.quality = this.quality;
			result.abilities = new ArrayList<>(this.abilities);
			return result;
		}

		@Nullable
		public MagicDevice create() {
			ensureComplete();
			final MagicDevice md = new MagicDevice();
			if (!StringUtils.isEmpty(this.name))
				md.setName(this.name);
			if (this.quality != null)
				md.setQuality(this.quality);
			if (this.type != null)
				md.setType(this.type);
			if (this.variant != null)
				md.setVariant(this.variant);
			if (this.abilities != null)
				for (final String s : this.abilities) {
					final int maxAbilities = md.getQuality().getMaxAbilities();
					final ResourceLocation ability = new ResourceLocation(s);
					final AbilityHandler handler = AbilityHandler.REGISTRY.getValue(ability);
					if (handler != null) {
						if (md.getAbilities().size() >= maxAbilities) {
							ModBase.log().warn(
									"Unable to add ability [%s] to [%s] - max abilities would be exceeded (%d)", s,
									md.getName(), maxAbilities);
						} else if (handler.canBeAppliedTo(md.getType())) {
							md.addAbility(ability);
						} else {
							ModBase.log().warn(
									"Ability [%s] cannot be applied to device [%s] because it is the wrong type [%s]",
									s, md.getName(), md.getType().toString());
						}
					} else {
						ModBase.log().warn("Unknown magic ability [%s] for device [%s]- ignored", s, md.getName());
					}
				}

			if (md.getAbilities().size() == 0) {
				ModBase.log().warn("No abilities were added to device [%s]; was this intended?", md.getName());
			}

			return md;
		}

	}

	@SerializedName("devices")
	public List<DeviceEntry> entries = ImmutableList.of();

}
