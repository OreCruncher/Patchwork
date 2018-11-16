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

package org.orecruncher.patchwork.block.shopshelf;

import org.orecruncher.patchwork.lib.TESRDisplayItems;

public class TESRShopShelf extends TESRDisplayItems<TileEntityShopShelf>  {

	private static final float SCALE = 0.5F;
	
	public TESRShopShelf() {
		super();
		add(ShopShelfStackHandler.TRADE_SLOT_1, 0.2F, 0.1F, 0.25F, SCALE);
		add(ShopShelfStackHandler.TRADE_SLOT_2, 0.2F, -0.25F, 0.25F, SCALE);
		add(ShopShelfStackHandler.TRADE_SLOT_3, 0.2F, -0.55F, 0.25F, SCALE);
		add(ShopShelfStackHandler.TRADE_SLOT_4, -0.2F, 0.1F, 0.25F, SCALE);
		add(ShopShelfStackHandler.TRADE_SLOT_5, -0.2F, -0.25F, 0.25F, SCALE);
		add(ShopShelfStackHandler.TRADE_SLOT_6, -0.2F, -0.55F, 0.25F, SCALE);
	}
}
