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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.lib.JsonUtils;
import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModOptions;
import org.orecruncher.patchwork.item.magic.MagicAbilities.AbilityConfig.ConfigEntry;
import org.orecruncher.patchwork.item.magic.abilities.AbilityCharging;
import org.orecruncher.patchwork.item.magic.abilities.AbilityFireball;
import org.orecruncher.patchwork.item.magic.abilities.AbilityFlight;
import org.orecruncher.patchwork.item.magic.abilities.AbilityMissile;
import org.orecruncher.patchwork.item.magic.abilities.AbilityPotion;
import org.orecruncher.patchwork.item.magic.abilities.AbilitySymbiotic;
import org.orecruncher.patchwork.item.magic.abilities.AbilityVacuum;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.SerializedName;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;

public class MagicAbilities {

	public static final ResourceLocation ABILITY_FLIGHT = new AbilityFlight().register();
	public static final ResourceLocation ABILITY_FIREBALL = new AbilityFireball().register();
	public static final ResourceLocation ABILITY_MISSILE = new AbilityMissile().register();
	public static final ResourceLocation ABILITY_SYMBIOTIC = new AbilitySymbiotic().register();
	public static final ResourceLocation ABILITY_CHARGING = new AbilityCharging().register();
	public static final ResourceLocation ABILITY_VACUUM = new AbilityVacuum().register();

	// Potion based effects
	public static final ResourceLocation ABILITY_POTION_SPEED = new AbilityPotion("speed").register();
	public static final ResourceLocation ABILITY_POTION_HASTE = new AbilityPotion("haste").register();
	public static final ResourceLocation ABILITY_POTION_STRENGTH = new AbilityPotion("strength").register();
	public static final ResourceLocation ABILITY_POTION_JUMP_BOOST = new AbilityPotion("jump_boost").register();
	public static final ResourceLocation ABILITY_POTION_RESISTANCE = new AbilityPotion("resistance").register();
	public static final ResourceLocation ABILITY_POTION_FIRE_RESISTANCE = new AbilityPotion("fire_resistance")
			.register();
	public static final ResourceLocation ABILITY_POTION_WATER_BREATHING = new AbilityPotion("water_breathing")
			.register();
	public static final ResourceLocation ABILITY_POTION_NIGHT_VISION = new AbilityPotion("night_vision").register();
	public static final ResourceLocation ABILITY_POTION_HEALTH_BOOST = new AbilityPotion("health_boost").register();
	public static final ResourceLocation ABILITY_POTION_ABSORPTION = new AbilityPotion("absorption").register();
	public static final ResourceLocation ABILITY_POTION_LUCK = new AbilityPotion("luck").register();
	public static final ResourceLocation ABILITY_POTION_SATURATION = new AbilityPotion("saturation").register();

	// Curses
	public static final ResourceLocation ABILITY_POTION_SLOWNESS = new AbilityPotion("slowness").register();
	public static final ResourceLocation ABILITY_POTION_FATIGUE = new AbilityPotion("mining_fatigue").register();
	public static final ResourceLocation ABILITY_POTION_HUNGER = new AbilityPotion("hunger").register();
	public static final ResourceLocation ABILITY_POTION_WEAKNESS = new AbilityPotion("weakness").register();
	public static final ResourceLocation ABILITY_POTION_POISON = new AbilityPotion("poison").register();
	public static final ResourceLocation ABILITY_POTION_UNLUCK = new AbilityPotion("unluck").register();

	public static final Set<ResourceLocation> CURSES = new ObjectOpenHashSet<>();

	static {
		CURSES.add(ABILITY_POTION_SLOWNESS);
		CURSES.add(ABILITY_POTION_FATIGUE);
		CURSES.add(ABILITY_POTION_HUNGER);
		CURSES.add(ABILITY_POTION_WEAKNESS);
		CURSES.add(ABILITY_POTION_POISON);
		CURSES.add(ABILITY_POTION_UNLUCK);
	}

	public static class AbilityConfig {
		public static class ConfigEntry {
			@SerializedName("priority")
			public Integer priority = 10000;
			@SerializedName("weight")
			public Integer weight = 100;
			@SerializedName("isCursed")
			public Boolean isCursed = false;
			@SerializedName("appliesTo")
			public Set<ItemMagicDevice.Type> appliesTo = ImmutableSet.of();
		}

		@SerializedName("abilities")
		public Map<String, ConfigEntry> abilities = ImmutableMap.of();
	}

	public static void initialize() {

		try {
			// Scan through the configuration Json making tweaks to the abilities that were
			// loaded
			final AbilityConfig config = JsonUtils.loadFromJar(AbilityConfig.class,
					"/assets/patchwork/data/ability_config.json");
			for (final Entry<String, ConfigEntry> e : config.abilities.entrySet()) {
				final AbilityHandler handler = AbilityHandler.REGISTRY.getValue(new ResourceLocation(e.getKey()));
				if (handler != null) {
					final ConfigEntry c = e.getValue();
					if (c.priority != null)
						handler.setPriority(c.priority);
					if (c.weight != null)
						handler.setWeight(c.weight);
					if (c.isCursed != null)
						handler.setCursed(c.isCursed.booleanValue());
					if (c.appliesTo != null)
						handler.setAllowedTypes(c.appliesTo.toArray(new ItemMagicDevice.Type[0]));
				} else {
					ModBase.log().warn("Handler [%s] not found", e.getKey());
				}
			}

		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Unable to configure abilities", t);
		}

		// Calling this triggers the construction of all the handlers. Dump out the
		// tables based on type if in debug
		if (ModOptions.logging.enableLogging) {
			for (final ItemMagicDevice.Type t : ItemMagicDevice.Type.values()) {
				ModBase.log().info("Abilities for [%s]", t.getName());
				ModBase.log().info("==============================");
				final List<AbilityHandler> handlers = AbilityHandler.queryForType(t);
				if (handlers.size() == 0)
					ModBase.log().info("NONE");
				else
					for (final AbilityHandler h : handlers)
						ModBase.log().info("[%s] %s", h.getRegistryName().toString(), h.getName());
			}
		}
	}
}
