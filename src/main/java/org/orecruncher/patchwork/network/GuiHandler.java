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

package org.orecruncher.patchwork.network;

import org.orecruncher.patchwork.block.shopshelf.ShopShelfContainer;
import org.orecruncher.patchwork.block.shopshelf.ShopShelfGui;
import org.orecruncher.patchwork.block.shopshelf.ShopShelfOwnerGui;
import org.orecruncher.patchwork.block.shopshelf.TileEntityShopShelf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	//@formatter:off
	public static enum ID {
		SHOP_SHELF,
		SHOP_SHELF_OWNER;
	}
	//@formatter:on

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity != null) {
			if (id == ID.SHOP_SHELF.ordinal()) {
				return new ShopShelfContainer((TileEntityShopShelf) tileEntity, player);
			}

			if (id == ID.SHOP_SHELF_OWNER.ordinal()) {
				return new ShopShelfContainer((TileEntityShopShelf) tileEntity, player, true);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity != null) {
			if (id == ID.SHOP_SHELF.ordinal()) {
				return new ShopShelfGui((TileEntityShopShelf) tileEntity, player);
			}

			if (id == ID.SHOP_SHELF_OWNER.ordinal()) {
				return new ShopShelfOwnerGui((TileEntityShopShelf) tileEntity, player);
			}
		}
		return null;
	}

}
