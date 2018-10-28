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

package org.blockartistry.doodads.common.item;

import org.blockartistry.doodads.client.DoodadsCreativeTab;
import org.blockartistry.doodads.common.item.ItemCoin.Type;
import org.blockartistry.doodads.common.item.magic.MagicAbilities;

import net.minecraft.item.Item;

public class ModItems {

	public static final Item COIN_COPPER = new ItemCoin(Type.COPPER);
	public static final Item COIN_BRONZE = new ItemCoin(Type.BRONZE);
	public static final Item COIN_SILVER = new ItemCoin(Type.SILVER);
	public static final Item COIN_GOLD = new ItemCoin(Type.GOLD);
	public static final Item COIN_PLATINUM = new ItemCoin(Type.PLATINUM);

	public static final Item REPAIR_PASTE = new ItemBase("repairpaste").setCreativeTab(DoodadsCreativeTab.tab);

	public static final Item MOB_NET = new ItemMobNet();

	public static final Item FEATHER_OF_FLIGHT = new ItemFeatherOfFlight();

	// Currently a do nothing function. By calling this it triggers the
	// classes static initializers to run.
	public static void initialize() {
		MagicAbilities.initialize();
	}

}
