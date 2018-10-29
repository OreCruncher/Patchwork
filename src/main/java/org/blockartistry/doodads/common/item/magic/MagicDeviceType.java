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

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.ModInfo;

import baubles.api.BaubleType;

public enum MagicDeviceType {
	//
	INERT(null, "inert"),
	//
	AMULET(BaubleType.AMULET, "amulet"),
	//
	RING(BaubleType.RING, "ring"),
	//
	BELT(BaubleType.BELT, "belt"),
	//
	TRINKET(BaubleType.TRINKET, "trinket"),
	//
	HEAD(BaubleType.HEAD, "head"),
	//
	BODY(BaubleType.BODY, "body"),
	//
	CHARM(BaubleType.CHARM, "charm"),
	//
	HELMET(null, "helmet"),
	//
	CHESTPLATE(null, "chestplate"),
	//
	LEGGINGS(null, "leggings"),
	//
	BOOTS(null, "boots"),
	//
	WEAPON(null, "weapon"),
	//
	SHIELD(null, "shield"),
	//
	STAFF(null, "staff"),
	//
	WAND(null, "wand");

	private static final Map<BaubleType, MagicDeviceType> fromBauble = new EnumMap<>(BaubleType.class);
	
	static {
		for(final MagicDeviceType t: MagicDeviceType.values())
			if(t.getBaubleType() != null)
				fromBauble.put(t.getBaubleType(), t);
	}
	
	@Nullable
	public static MagicDeviceType fromBauble(@Nonnull final BaubleType t) {
		return fromBauble.get(t);
	}
	
	private final BaubleType bauble;
	private final String unlocalizedName;
	
	private MagicDeviceType(@Nullable final BaubleType type, @Nonnull final String name) {
		this.bauble = type;
		this.unlocalizedName = ModInfo.MOD_ID + ".magicdevice." + name + ".name";
	}

	@Nonnull
	public String getUnlocalizedName() {
		return this.unlocalizedName;
	}

	@Nullable
	public BaubleType getBaubleType() {
		return this.bauble;
	}

}
