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

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.doodads.proxy.IProxy;
import org.blockartistry.doodads.util.ForgeUtils;
import org.blockartistry.doodads.util.Localization;
import org.blockartistry.doodads.util.ModLog;
import org.blockartistry.doodads.util.VersionChecker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod(
		modid = Doodads.MOD_ID,
		useMetadata = true,
		dependencies = Doodads.DEPENDENCIES,
		version = Doodads.VERSION,
		acceptedMinecraftVersions = Doodads.MINECRAFT_VERSIONS,
		updateJSON = Doodads.UPDATE_URL,
		certificateFingerprint = Doodads.FINGERPRINT
)
public class Doodads {
	public static final String MOD_ID = "doodads";
	public static final String MOD_NAME = "Doodads";
	public static final String VERSION = "@VERSION@";
	public static final String MINECRAFT_VERSIONS = "[1.12.2,)";
	public static final String DEPENDENCIES = "required-after:forge@[14.23.1.2555,);required-after:baubles@[1.5.2,)";
	public static final String UPDATE_URL = "https://raw.githubusercontent.com/OreCruncher/Doodads/master/version.json";
	public static final String FINGERPRINT = "7a2128d395ad96ceb9d9030fbd41d035b435753a";

	@Instance(MOD_ID)
	protected static Doodads instance;

	@SidedProxy(clientSide = "org.blockartistry.doodads.proxy.ClientProxy", serverSide = "org.blockartistry.doodads.proxy.ServerProxy")
	protected static IProxy proxy;
	protected static ModLog logger = ModLog.NULL_LOGGER;

	@Nonnull
	public static Doodads instance() {
		return instance;
	}

	@Nonnull
	public static IProxy proxy() {
		return proxy;
	}

	@Nonnull
	public static ModLog log() {
		return logger;
	}

	//==================================
	//
	// Standard proxy event handling.
	//
	//==================================
	
	@EventHandler
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {

		logger = ModLog.setLogger(Doodads.MOD_ID, event.getModLog());
		logger.setDebug(Configuration.logging.enableLogging);
		MinecraftForge.EVENT_BUS.register(this);
		
		proxy().preInit(event);
	}

	@EventHandler
	public void init(@Nonnull final FMLInitializationEvent event) {
		proxy().init(event);
	}

	@EventHandler
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		proxy().postInit(event);

		// Patch up metadata
		if (!proxy().isDedicatedServer()) {
			final ModMetadata data = ForgeUtils.getModMetadata(Doodads.MOD_ID);
			if (data != null) {
				data.name = Localization.format("doodads.metadata.Name");
				data.credits = Localization.format("doodads.metadata.Credits");
				data.description = Localization.format("doodads.metadata.Description");
				data.authorList = Arrays
						.asList(StringUtils.split(Localization.format("doodads.metadata.Authors"), ','));
			}
		}
	}
	
	@EventHandler
	public void loadComplete(@Nonnull final FMLLoadCompleteEvent event) {
		proxy().loadCompleted(event);
	}

	//==================================
	//
	// Extra event handling
	//
	//==================================

	@EventHandler
	public void onFingerprintViolation(@Nonnull final FMLFingerprintViolationEvent event) {
		log().warn("Invalid fingerprint detected!");
	}

	@SubscribeEvent
	public void playerLogin(final PlayerLoggedInEvent event) {
		if (Configuration.logging.enableVersionCheck)
			new VersionChecker(Doodads.MOD_ID, "doodads.msg.NewVersion").playerLogin(event);
	}

}
