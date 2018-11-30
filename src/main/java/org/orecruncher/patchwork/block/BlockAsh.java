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
package org.orecruncher.patchwork.block;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModOptions;
import org.orecruncher.patchwork.client.ModCreativeTab;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAsh extends BlockBase {

	public BlockAsh() {
		super("ash_block", Material.ROCK, MapColor.BLACK);
		setCreativeTab(ModCreativeTab.tab);
		setHarvestLevel("axe", 1);
		setHardness(2.5F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setDefaultState(this.blockState.getBaseState());
	}

	@Override
	public Item getItemDropped(@Nonnull final IBlockState state, @Nonnull final Random rand, final int fortune) {
		return Items.COAL;
	}

	@Override
	public int damageDropped(@Nonnull final IBlockState state) {
		return 1;
	}

	@Override
	public int quantityDropped(@Nonnull final Random random) {
		return ModOptions.ashBlock.yield;
	}

	@Override
	public boolean canSilkHarvest(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state, @Nonnull final EntityPlayer player) {
		return true;
	}

	@Override
	public int quantityDroppedWithBonus(final int fortune, @Nonnull final Random random) {
		int q = this.quantityDropped(random);
		if (fortune > 0)
			q += random.nextInt(fortune + 1);
		return q;
	}

}
