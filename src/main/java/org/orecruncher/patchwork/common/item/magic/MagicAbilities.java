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
package org.orecruncher.patchwork.common.item.magic;

import org.orecruncher.patchwork.common.item.magic.abilities.AbilityCharging;
import org.orecruncher.patchwork.common.item.magic.abilities.AbilityFireball;
import org.orecruncher.patchwork.common.item.magic.abilities.AbilityFlight;
import org.orecruncher.patchwork.common.item.magic.abilities.AbilitySymbiotic;
import org.orecruncher.patchwork.common.item.magic.abilities.AbilityVacuum;

import net.minecraft.util.ResourceLocation;

public class MagicAbilities {

	public static final ResourceLocation ABILITY_FLIGHT = new AbilityFlight().register();
	public static final ResourceLocation ABILITY_FIREBALL = new AbilityFireball().register();
	public static final ResourceLocation ABILITY_SYMBIOTIC = new AbilitySymbiotic().register();
	public static final ResourceLocation ABILITY_CHARGING = new AbilityCharging().register();
	public static final ResourceLocation ABILITY_VACUUM = new AbilityVacuum().register();

	public static void initialize() {
		// Nothing here.  Called to tickle the static CTORs
	}

}
