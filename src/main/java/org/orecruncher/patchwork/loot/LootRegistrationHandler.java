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

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.ModOptions;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public class LootRegistrationHandler {

	//@formatter:off
	private static final String[] LOOT_INJECTION_LOCATIONS =
	{
		"minecraft:chests/simple_dungeon",
		"minecraft:chests/abandoned_mineshaft",
		"minecraft:chests/desert_pyramid",
		"minecraft:chests/jungle_temple",
		"minecraft:chests/stronghold_corridor",
		"minecraft:chests/stronghold_crossing",
		"minecraft:chests/stronghold_library",
		"minecraft:chests/igloo_chest",
		"minecraft:chests/woodland_mansion",
		"minecraft:chests/end_city_treasure",
		"minecraft:chests/village_blacksmith"
	};
	//@formatter:on

	public static void initialize() {
		
		// Conditions
		LootConditionManager.registerCondition(new EnableDisableCondition.Serializer());
		
		// Tables
		LootTableList.register(Loot.COIN_PASSIVE);
		LootTableList.register(Loot.COIN_SPAWNER);
		LootTableList.register(Loot.COIN_SMALL);
		LootTableList.register(Loot.COIN_MEDIUM);
		LootTableList.register(Loot.COIN_LARGE);
		LootTableList.register(Loot.COIN_LEGENDARY);
		LootTableList.register(Loot.CHEST_ABANDONED_MINESHAFT);
		LootTableList.register(Loot.CHEST_DESERT_PYRAMID);
		LootTableList.register(Loot.CHEST_END_CITY_TREASURE);
		LootTableList.register(Loot.CHEST_IGLOO_CHEST);
		LootTableList.register(Loot.CHEST_JUNGLE_TEMPLE);
		LootTableList.register(Loot.CHEST_SIMPLE_DUNGEON);
		LootTableList.register(Loot.CHEST_STRONGHOLD_CORRIDOR);
		LootTableList.register(Loot.CHEST_STRONGHOLD_CROSSING);
		LootTableList.register(Loot.CHEST_STRONGHOLD_LIBRARY);
		LootTableList.register(Loot.CHEST_WOODLAND_MANSION);
	}

	@SubscribeEvent
	public static void onLootTableLoadEvent(@Nonnull final LootTableLoadEvent event) {
		if (ModOptions.coins.spawnAsLoot) {
			ModBase.log().debug("Encountered pool [%s]", event.getName().toString());
			for (final String location : LOOT_INJECTION_LOCATIONS) {
				if (event.getName().toString().matches(location)) {
					final ResourceLocation t = new ResourceLocation(location);
					final ResourceLocation rl = new ResourceLocation(ModInfo.MOD_ID, t.getPath());
					final LootTable table = event.getLootTableManager().getLootTableFromLocation(rl);
					if (table != null) {
						final LootPool pool = table.getPool(ModInfo.MOD_ID);
						if (pool != null) {
							event.getTable().addPool(pool);
							ModBase.log().debug("Added pool [%s] to loot table [%s]",
									rl.toString() + "#" + pool.getName(), location);
						} else {
							ModBase.log().warn("Could not find pool [%s] in loot table [%s]", ModInfo.MOD_ID,
									rl.toString());
						}
					} else {
						ModBase.log().warn("Could not find loot table [%s]", rl.toString());
					}
				}
			}
		}
	}
}
