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

	@Name("Items")
	@Comment("Options to control item items and loot tables")
	@LangKey(Items.PREFIX)
	public static Items items = new Items();

	public static class Items {

		private static final String PREFIX = ModOptions.PREFIX + ".items";

		@LangKey(PREFIX + ".enableCoins")
		@Comment({ "Enable Coins" })
		@RequiresMcRestart
		public boolean enableCoins = true;

		@LangKey(PREFIX + ".enableMobnet")
		@Comment({ "Enable Mobnet" })
		@RequiresMcRestart
		public boolean enableMobnet = true;

		@LangKey(PREFIX + ".enableFurnace")
		@Comment({ "Enable 3D Furnace" })
		@RequiresMcRestart
		public boolean enableFurnace = true;

		@LangKey(PREFIX + ".enableShopShelf")
		@Comment({ "Enables Shop Shelf" })
		@RequiresMcRestart
		public boolean enableShopShelf = true;

		@LangKey(PREFIX + ".enableTools")
		@Comment({ "Enables Set of Tools recipe" })
		@RequiresMcRestart
		public boolean enableTools = true;

		@LangKey(PREFIX + ".enableRingOfFlight")
		@Comment({ "Enables Ring of Flight" })
		@RequiresMcRestart
		public boolean enableRingOfFlight = true;

		@LangKey(PREFIX + ".enableMagnet")
		@Comment({ "Enables Item Magnet" })
		@RequiresMcRestart
		public boolean enableItemMagnet = true;

		@LangKey(PREFIX + ".enableToolRepair")
		@Comment({ "Enables Tool Repair" })
		@RequiresMcRestart
		public boolean enableToolRepair = true;

		@LangKey(PREFIX + ".enableWoodPile")
		@Comment({ "Enables Wood Pile" })
		@RequiresMcRestart
		public boolean enableWoodPile = true;
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

		@LangKey(PREFIX + ".renderwings")
		@Comment({ "Enable/disable rendering of wings for Ring Of Flight" })
		@RequiresMcRestart
		public boolean renderWings = true;

		@LangKey(PREFIX + ".mobquantity")
		@Comment({ "Enable/disable increase/decrease mob spawn quantities" })
		@RequiresMcRestart
		public boolean modifyMobQuantity = false;

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

	@Name("RepairPaste")
	@Comment("Options to control Repair Paste")
	@LangKey(RepairPaste.PREFIX)
	public static RepairPaste repairPaste = new RepairPaste();

	public static class RepairPaste {
		private static final String PREFIX = ModOptions.PREFIX + ".repairpaste";

		@LangKey(PREFIX + ".repairamount")
		@Comment({ "Amount repaired per Repair Paste" })
		public int repairAmount = 250;

	}

	@Name("RingOfFlight")
	@Comment("Options to control Ring of Flight")
	@LangKey(RingOfFlight.PREFIX)
	public static RingOfFlight ringOfFlight = new RingOfFlight();

	public static class RingOfFlight {

		private static final String PREFIX = ModOptions.PREFIX + ".ringofflight";

		@LangKey(PREFIX + ".refuel")
		@Comment({ "Amount refueld per Fire Charge" })
		public int refuelAmount = 5000;

	}

	@Name("AshBlock")
	@Comment("Options to control Ash Block")
	@LangKey(AshBlock.PREFIX)
	public static AshBlock ashBlock = new AshBlock();

	public static class AshBlock {

		private static final String PREFIX = ModOptions.PREFIX + ".ashblock";

		@LangKey(PREFIX + ".yield")
		@Comment({ "Number of charcoal to drop when broken" })
		public int yield = 8;
	}

	@Name("WoodPile")
	@Comment("Options to control Wood Pile")
	@LangKey(WoodPile.PREFIX)
	public static WoodPile woodPile = new WoodPile();

	public static class WoodPile {

		private static final String PREFIX = ModOptions.PREFIX + ".woodpile";

		@LangKey(PREFIX + ".tickrate")
		@Comment({ "Ticks between stage checks" })
		public int tickRate = 600;
	}

	@Name("MobQuantity")
	@Comment("Options to control Mob Quantities")
	@LangKey(MobQuantity.PREFIX)
	public static MobQuantity mobQuantity = new MobQuantity();

	public static class MobQuantity {

		private static final String PREFIX = ModOptions.PREFIX + ".mobquantity";

		@LangKey(PREFIX + ".monster")
		@Comment({ "Max number of monster mobs to spawn" })
		@RequiresMcRestart
		public int monster = 70;

		@LangKey(PREFIX + ".creature")
		@Comment({ "Max number of creature mobs to spawn" })
		@RequiresMcRestart
		public int creature = 10;

		@LangKey(PREFIX + ".ambient")
		@Comment({ "Max number of ambient mobs to spawn" })
		@RequiresMcRestart
		public int ambient = 15;

		@LangKey(PREFIX + ".water")
		@Comment({ "Max number of water mobs to spawn" })
		@RequiresMcRestart
		public int water = 5;
	}

	@Name("Villages")
	@Comment("Options to control Mob Villages")
	@LangKey(Villages.PREFIX)
	public static Villages villages = new Villages();

	public static class Villages {

		private static final String PREFIX = ModOptions.PREFIX + ".villages";

		@LangKey(PREFIX + ".additionalbiomes")
		@Comment({ "Additional biomes where villages can spawn" })
		@RequiresMcRestart
		public String[] additionalBiomes = {};
	}

	@SubscribeEvent
	public static void onConfigChangedEvent(final OnConfigChangedEvent event) {
		if (event.getModID().equals(ModInfo.MOD_ID)) {
			ConfigManager.sync(ModInfo.MOD_ID, Type.INSTANCE);
			ModBase.log().setDebug(ModOptions.logging.enableLogging);
		}
	}
}
