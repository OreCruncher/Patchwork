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

package org.blockartistry.doodads;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = Doodads.MOD_ID)
@Config(modid = Doodads.MOD_ID, type = Type.INSTANCE, name = Doodads.MOD_ID)
public class Configuration {

	@LangKey("config.doodads.logging")
	public static Logging logging = new Logging();

	@LangKey("config.doodads.mobnet")
	public static MobNet mobnet = new MobNet();
	
	public static class Logging {
		@LangKey("config.doodads.logging.enableLogging")
		@Comment({ "Enables debug logging output for diagnostics" })
		public boolean enableLogging = false;

		@LangKey("config.doodads.logging.enableVersionCheck")
		@Comment({ "Enables display of chat messages related to newer versions", "of the mod being available." })
		public boolean enableVersionCheck = true;
	}
	
	public static class MobNet {
		@LangKey("config.doodads.mobnet.enableVillagerCapture")
		@Comment({"Enable/disable use of the Mob Net on Villagers"})
		public boolean enableVillagerCapture = false;

		@LangKey("config.doodads.mobnet.enableHostileCapture")
		@Comment({"Enable/diable use of the Mob Net on hostile entities"})
		public boolean enableHostileCapture = false;
		
		@LangKey("config.doodads.mobnet.reusable")
		@Comment({"Enable/disable reuse of Mob Nets"})
		public boolean reusable = true;
	}

	@SubscribeEvent
	public static void onConfigChangedEvent(final OnConfigChangedEvent event) {
		if (event.getModID().equals(Doodads.MOD_ID)) {
			ConfigManager.sync(Doodads.MOD_ID, Type.INSTANCE);
			Doodads.log().setDebug(Configuration.logging.enableLogging);
		}
	}
}
