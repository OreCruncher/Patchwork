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

package org.blockartistry.doodads.sided;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.ItemBase;
import org.blockartistry.doodads.common.item.magic.capability.CapabilityMagicDevice;
import org.blockartistry.doodads.common.loot.LootRegistrationHandler;
import org.blockartistry.doodads.common.recipe.RepairPasteRecipe;
import org.blockartistry.doodads.util.Localization;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public abstract class SideSupport {

	/**
	 * Indicates if the mod is running on a dedicated server. Should be overridden
	 * and the correct information provided.
	 *
	 * @return true if running on dedicated server; false otherwise
	 */
	public abstract boolean isDedicatedServer();

	/**
	 * Override to provide logic. Deriving classes should invoke the super class
	 * method.
	 *
	 * @param event
	 *                  The fired event
	 */
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		final Side targetSide = isDedicatedServer() ? Side.SERVER : Side.CLIENT;
		Localization.initialize(targetSide, ModInfo.MOD_ID);
		CapabilityMagicDevice.register();
	}

	/**
	 * Called during the mod's initialization phase.
	 *
	 * @param event
	 *                  The fired event
	 */
	public void init(@Nonnull final FMLInitializationEvent event) {
		// This method intentionally left blank
	}

	/**
	 * Called during the mod's post initialization phase. Deriving classes should
	 * invoke the super class method.
	 *
	 * @param event
	 *                  The fired event
	 */
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		RepairPasteRecipe.register();
		LootRegistrationHandler.initialize();
	}

	/**
	 * Called when Forge has finished loading.
	 *
	 * @param event
	 *                  The fired event.
	 */
	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
		// This method intentionally left blank
	}

	/**
	 * Called if Forge detects a fingerprint violation with the mod.
	 *
	 * @param event
	 *                  The fired event.
	 */
	public void fingerprintViolation(@Nonnull final FMLFingerprintViolationEvent event) {
		Doodads.log().warn("Invalid fingerprint detected!");
	}

	/**
	 * Called to register an item renderer. The default implementation assumes
	 * server and doesn't do anything. Needs to be overridden in the client proxy.
	 *
	 * @param item
	 * @param meta
	 * @param id
	 */
	public void registerItemRenderer(@Nonnull final ItemBase item, final int meta, @Nonnull final ModelResourceLocation loc) {
		// This method intentionally left blank
	}
	
	@Nullable
	public World getClientWorld() {
		return null;
	}

}