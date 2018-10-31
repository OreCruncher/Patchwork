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
package org.blockartistry.doodads.common.item.magic;

import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.magic.abilities.AbilityFireball;
import org.blockartistry.doodads.common.item.magic.abilities.AbilityFlight;
import org.blockartistry.doodads.common.item.magic.abilities.AbilityCharging;
import org.blockartistry.doodads.common.item.magic.abilities.AbilitySymbiotic;

import net.minecraft.util.ResourceLocation;

public class MagicAbilities {

	// Collect IDs for capabilities here
	public static final ResourceLocation ABILITY_FLIGHT = new ResourceLocation(ModInfo.MOD_ID, "flight");
	public static final ResourceLocation ABILITY_FIREBALL = new ResourceLocation(ModInfo.MOD_ID, "fireball");
	public static final ResourceLocation ABILITY_SYMBIOTIC = new ResourceLocation(ModInfo.MOD_ID, "symbiotic");
	public static final ResourceLocation ABILITY_CHARGE = new ResourceLocation(ModInfo.MOD_ID, "charging");

	public static final AbilityHandler FLIGHT = new AbilityFlight().register();
	public static final AbilityHandler FIREBALL = new AbilityFireball().register();
	public static final AbilityHandler SYMBIOTIC = new AbilitySymbiotic().register();
	public static final AbilityHandler CHARGING = new AbilityCharging().register();

	public static void initialize() {
		// Nothing here
	}

}
