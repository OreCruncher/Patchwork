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

import javax.annotation.Nonnull;

import org.blockartistry.doodads.ModInfo;

import net.minecraft.item.EnumRarity;

public enum DeviceQuality {
	//
	MUNDANE("mundane", 0, 20 * 60 * 60, EnumRarity.COMMON),
	//
	NORMAL("normal", 1, 20 * 60 * 60 * 2, EnumRarity.UNCOMMON),
	//
	PRIZED("prized", 2, 20 * 60 * 60 * 4, EnumRarity.RARE),
	//
	LEGENDARY("legendary", 3, 20 * 60 * 60 * 8, EnumRarity.EPIC);

	private final String name;
	private final int maxAbilities;
	private final EnumRarity rarity;
	private final int maxPower;

	private DeviceQuality(@Nonnull final String name, final int maxAbilities, final int maxPower,
			@Nonnull final EnumRarity rarity) {
		this.name = ModInfo.MOD_ID + ".devicequality." + name + ".name";
		this.maxAbilities = maxAbilities;
		this.maxPower = maxPower;
		this.rarity = rarity;
	}

	@Nonnull
	public String getUnlocalizedName() {
		return this.name;
	}

	public int getMaxAbilities() {
		return this.maxAbilities;
	}

	@Nonnull
	public EnumRarity getRarity() {
		return this.rarity;
	}

	public int getMaxPower() {
		return this.maxPower;
	}
}
