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
package org.orecruncher.patchwork.world;

import javax.annotation.Nonnull;

import org.orecruncher.lib.ReflectedField.IntegerField;
import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModOptions;

import net.minecraft.entity.EnumCreatureType;

public class IncreaseMobs {

	//@formatter:off
	private static final IntegerField<EnumCreatureType> maxCreatures =
		new IntegerField<>(
			EnumCreatureType.class,
			"maxNumberOfCreature",
			"field_75606_e"
		);
	//@formatter:on

	public static void initialize() {
		if (ModOptions.features.modifyMobQuantity) {
			set(EnumCreatureType.MONSTER, ModOptions.mobQuantity.monster);
			set(EnumCreatureType.CREATURE, ModOptions.mobQuantity.creature);
			set(EnumCreatureType.AMBIENT, ModOptions.mobQuantity.ambient);
			set(EnumCreatureType.WATER_CREATURE, ModOptions.mobQuantity.water);
		}
	}

	private static void set(@Nonnull final EnumCreatureType type, final int quantity) {
		maxCreatures.set(type, quantity);
		ModBase.log().info("Set spawn cap [%s] to %d", type.name(), quantity);
	}

}
