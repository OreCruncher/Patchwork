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

package org.orecruncher.patchwork;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
@Config(modid = ModInfo.MOD_ID, type = Type.INSTANCE, name = ModInfo.MOD_ID, category = "")
public class ModOptions {

	private static final String PREFIX = "config." + ModInfo.MOD_ID;

	@Name("Logging")
	@Comment("Options to control logging")
	@LangKey(Logging.PREFIX)
	public static Logging logging = new Logging();

	public static class Logging {

		private static final String PREFIX = ModOptions.PREFIX + ".logging";

		@LangKey(PREFIX + ".enableLogging")
		@Comment({ "Enables debug logging output for diagnostics" })
		public boolean enableLogging = false;

		@LangKey(PREFIX + ".enableVersionCheck")
		@Comment({ "Enables display of chat messages related to newer versions", "of the mod being available." })
		public boolean enableVersionCheck = true;
	}

	@Name("Recipes")
	@Comment("Options to control recipes")
	@LangKey(Recipes.PREFIX)
	public static Recipes recipes = new Recipes();

	public static class Recipes {

		private static final String PREFIX = ModOptions.PREFIX + ".recipes";

		@LangKey(PREFIX + ".enableCoins")
		@Comment({ "Enable Coin recipes" })
		@RequiresMcRestart
		public boolean enableCoins = true;

		@LangKey(PREFIX + ".enableMobnet")
		@Comment({ "Enable Mobnet recipe" })
		@RequiresMcRestart
		public boolean enableMobnet = true;

		@LangKey(PREFIX + ".enableFurnace")
		@Comment({ "Enables 3D Furnace recipe" })
		@RequiresMcRestart
		public boolean enableFurnace = true;

		@LangKey(PREFIX + ".enableShopShelf")
		@Comment({ "Enables Shop Shelf recipe" })
		@RequiresMcRestart
		public boolean enableShopShelf = true;

		@LangKey(PREFIX + ".enableTools")
		@Comment({ "Enables Set of Tools recipe" })
		@RequiresMcRestart
		public boolean enableTools = true;

		@LangKey(PREFIX + ".enableToolRepair")
		@Comment({ "Enables Tool Repair recipe" })
		@RequiresMcRestart
		public boolean enableToolRepair = true;
	}

	@Name("Mobnet")
	@Comment("Options to control the Mobnet")
	@LangKey(Mobnet.PREFIX)
	public static Mobnet mobnet = new Mobnet();

	public static class Mobnet {

		private static final String PREFIX = ModOptions.PREFIX + ".mobnet";

		@LangKey(PREFIX + ".enableVillagerCapture")
		@Comment({ "Enable/disable use of the Mob Net on Villagers" })
		public boolean enableVillagerCapture = false;

		@LangKey(PREFIX + ".enableHostileCapture")
		@Comment({ "Enable/diable use of the Mob Net on hostile entities" })
		public boolean enableHostileCapture = false;

		@LangKey(PREFIX + ".reusable")
		@Comment({ "Enable/disable reuse of Mob Nets" })
		public boolean reusable = true;
	}

	@Name("Features")
	@Comment("Options to control various functions")
	@LangKey(Features.PREFIX)
	public static Features features = new Features();

	public static class Features {

		private static final String PREFIX = ModOptions.PREFIX + ".features";

		@LangKey(PREFIX + ".nodropsfromspawnermobs")
		@Comment({ "Enable/disable scrubbing drops from mobs that come from spawners" })
		public boolean noDropsFromSpawnerMobs = true;
	}

	@Name("Furnace")
	@Comment("Options to control the 3D Furnace")
	@LangKey(Furnace.PREFIX)
	public static Furnace furnace = new Furnace();

	public static class Furnace {

		private static final String PREFIX = ModOptions.PREFIX + ".furnace";

		@LangKey(PREFIX + ".enableImmersiveInteraction")
		@Comment({ "Enable/disable immersive interaction with the 3D furnace" })
		public boolean immersiveInteraction = true;
	}

	@Name("Coins")
	@Comment("Options to control Coins")
	@LangKey(Coins.PREFIX)
	public static Coins coins = new Coins();

	public static class Coins {

		private static final String PREFIX = ModOptions.PREFIX + ".coins";

		@LangKey(PREFIX + ".spawnasloot")
		@Comment({ "Enable/disable generation of coins in dungeon chests" })
		@RequiresMcRestart
		public boolean spawnAsLoot = true;

		@LangKey(PREFIX + ".mobsdropcoins")
		@Comment({ "Enable/disable dropping of coins from mobs" })
		public boolean mobsDropCoins = true;
	}

	@SubscribeEvent
	public static void onConfigChangedEvent(final OnConfigChangedEvent event) {
		if (event.getModID().equals(ModInfo.MOD_ID)) {
			ConfigManager.sync(ModInfo.MOD_ID, Type.INSTANCE);
			ModBase.log().setDebug(ModOptions.logging.enableLogging);
		}
	}
}
