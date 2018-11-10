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
import javax.annotation.Nullable;

import org.orecruncher.patchwork.ModInfo;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;

public class Loot {

	public static final ResourceLocation COIN_PASSIVE = new ResourceLocation(ModInfo.MOD_ID, "coins_passive");
	public static final ResourceLocation COIN_SPAWNER = new ResourceLocation(ModInfo.MOD_ID, "coins_spawner");
	public static final ResourceLocation COIN_SMALL = new ResourceLocation(ModInfo.MOD_ID, "coins_small");
	public static final ResourceLocation COIN_MEDIUM = new ResourceLocation(ModInfo.MOD_ID, "coins_medium");
	public static final ResourceLocation COIN_LARGE = new ResourceLocation(ModInfo.MOD_ID, "coins_large");
	public static final ResourceLocation COIN_LEGENDARY = new ResourceLocation(ModInfo.MOD_ID, "coins_legendary");
	
	public static final ResourceLocation CHEST_ABANDONED_MINESHAFT = new ResourceLocation(ModInfo.MOD_ID, "chests/abandoned_mineshaft");
	public static final ResourceLocation CHEST_DESERT_PYRAMID = new ResourceLocation(ModInfo.MOD_ID, "chests/desert_pyramid");
	public static final ResourceLocation CHEST_END_CITY_TREASURE = new ResourceLocation(ModInfo.MOD_ID, "chests/end_city_treasure");
	public static final ResourceLocation CHEST_IGLOO_CHEST = new ResourceLocation(ModInfo.MOD_ID, "chests/igloo_chest");
	public static final ResourceLocation CHEST_JUNGLE_TEMPLE = new ResourceLocation(ModInfo.MOD_ID, "chests/jungle_temple");
	public static final ResourceLocation CHEST_SIMPLE_DUNGEON = new ResourceLocation(ModInfo.MOD_ID, "chests/simple_dungeon");
	public static final ResourceLocation CHEST_STRONGHOLD_CROSSING = new ResourceLocation(ModInfo.MOD_ID, "chests/stronghold_crossing");
	public static final ResourceLocation CHEST_STRONGHOLD_CORRIDOR = new ResourceLocation(ModInfo.MOD_ID, "chests/stronghold_corridor");
	public static final ResourceLocation CHEST_STRONGHOLD_LIBRARY = new ResourceLocation(ModInfo.MOD_ID, "chests/stronghold_library");
	public static final ResourceLocation CHEST_WOODLAND_MANSION = new ResourceLocation(ModInfo.MOD_ID, "chests/woodland_mansion");

	@Nullable
	public static LootTable getTable(@Nonnull final World world, @Nonnull final ResourceLocation resource) {
		return world.getLootTableManager().getLootTableFromLocation(resource);
	}

	@Nullable
	public static LootPool getPool(@Nonnull final World world, @Nonnull final ResourceLocation resource,
			@Nonnull final String pool) {
		final LootTable table = getTable(world, resource);
		return table != null ? table.getPool(pool) : null;
	}
}
