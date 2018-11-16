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

package org.orecruncher.patchwork.item;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.block.IRotateable;
import org.orecruncher.patchwork.client.ModCreativeTab;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ItemStackHolder;

public class ItemTools extends ItemBase {

	@ItemStackHolder(value = "patchwork:tools")
	public static final ItemStack ROTATE_TOOL = ItemStack.EMPTY;

	public ItemTools() {
		super("tools");
		setCreativeTab(ModCreativeTab.tab);
		setMaxStackSize(1);
		setMaxDamage(50);
		
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void playerInteract(@Nonnull final RightClickBlock event) {
		final World world = event.getWorld();
		if (!world.isRemote) {
			final IBlockState state = world.getBlockState(event.getPos());
			if (state.getBlock() instanceof IRotateable) {
				final ItemStack heldItem = event.getEntityPlayer().getHeldItemMainhand();
				if (ItemStack.areItemsEqual(heldItem, ROTATE_TOOL)) {
					final EntityPlayer player = event.getEntityPlayer();
					final IRotateable rot = (IRotateable) state.getBlock();
					rot.rotateBlock(player, world, event.getPos(), player.getAdjustedHorizontalFacing());
					event.setCanceled(true);
					event.setCancellationResult(EnumActionResult.FAIL);
				}
			}
		}
	}

}
