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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModOptions;
import org.orecruncher.patchwork.client.ModCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockWoodPile extends BlockBase {

	private static final List<EnumFacing> FACINGS = Arrays
			.asList(Stream.of(EnumFacing.VALUES).toArray(EnumFacing[]::new));

	private static final int BURNING_LIGHT = 7;
	private static final int FIRE_SPREAD = 10; // 2x log
	private static final int FLAMMABILITY = 20; // planks, etc.

	private static final int MAX_STAGE = 7;

	public static final PropertyBool IS_BURNING = PropertyBool.create("burning");
	public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, MAX_STAGE);

	public BlockWoodPile() {
		super("wood_pile", Material.WOOD, MapColor.BROWN);
		setCreativeTab(ModCreativeTab.tab);
		setHarvestLevel("axe", 1);
		setHardness(2.5F);
		setResistance(10.0F);
		setSoundType(SoundType.WOOD);
		setDefaultState(this.blockState.getBaseState().withProperty(IS_BURNING, false).withProperty(STAGE, 0));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockModel() {
		final IStateMapper mapper = new StateMap.Builder().ignore(IS_BURNING, STAGE).build();
		ModelLoader.setCustomStateMapper(this, mapper);
		super.registerBlockModel();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, IS_BURNING, STAGE);
	}

	@Override
	public int getMetaFromState(@Nonnull final IBlockState state) {
		final int burningFlag = state.getValue(IS_BURNING) ? 0x8 : 0;
		return burningFlag | state.getValue(STAGE);
	}

	@Override
	public IBlockState getStateFromMeta(final int meta) {
		final boolean burning = (meta & 0x8) != 0;
		final int stage = meta & 0x7;
		return getDefaultState().withProperty(IS_BURNING, burning).withProperty(STAGE, stage);
	}

	@Override
	public int tickRate(@Nonnull final World world) {
		return ModOptions.woodPile.tickRate;
	}

	protected int randomTickRate(@Nonnull final World world) {
		final int rate = tickRate(world);
		return rate + world.rand.nextInt((int) (rate * 0.15F));
	}

	@Override
	public boolean isOpaqueCube(@Nonnull final IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(@Nonnull final IBlockState state) {
		return false;
	}

	protected List<EnumFacing> getFacings(@Nonnull final World world) {
		if (world.isRemote)
			throw new RuntimeException("Cannot call client side!");

		// Generate a randomized list of facings. Helps mitigate the deterministic
		// process that programs are manifesting themselves as patterns in the world.
		Collections.shuffle(FACINGS, world.rand);
		return FACINGS;
	}

	@Override
	public void onBlockAdded(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state) {
		if (!world.isRemote) {
			// If placed next to a fire block then this one will catch when placed.
			for (final EnumFacing facing : getFacings(world)) {
				final IBlockState facingState = world.getBlockState(pos.offset(facing));
				if (facingState.getBlock() == Blocks.FIRE) {
					world.setBlockState(pos, state.withProperty(IS_BURNING, true));
					break;
				}
			}

			// Schedule our next update
			world.scheduleUpdate(pos, this, randomTickRate(world));
		}
	}

	@Override
	public void neighborChanged(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final Block fromBlock, @Nonnull final BlockPos fromPos) {
		// If a neighbor block changed to fire then we set ourselves to burning. This is
		// the ignition point. Note that this is a fire block, so any method of
		// generating a fire block can start the process.
		if (fromBlock == Blocks.FIRE && !state.getValue(IS_BURNING)) {
			burn(state, world, pos, true);
		}
	}

	protected void burn(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos,
			final boolean scheduleUpdate) {
		world.setBlockState(pos, state.withProperty(IS_BURNING, true), 2);

		// Schedule our next update of instructed. Don't wait as long as the fire can
		// quickly spread to other adjacent wood piles.
		if (scheduleUpdate)
			world.scheduleUpdate(pos, this, randomTickRate(world) / 4);
	}

	@Override
	public void updateTick(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state,
			@Nonnull final Random rand) {
		if (state.getValue(IS_BURNING)) {
			for (final EnumFacing facing : getFacings(world)) {
				final BlockPos adjacentPos = pos.offset(facing);
				if (world.isBlockLoaded(adjacentPos)) {
					final IBlockState adjacentBlockState = world.getBlockState(adjacentPos);
					final Block block = adjacentBlockState.getBlock();
					if (block == this) {
						if (!state.getValue(IS_BURNING) && adjacentBlockState.getValue(IS_BURNING)) {
							burn(state, world, pos, false);
						} else if (!adjacentBlockState.getValue(IS_BURNING) && state.getValue(IS_BURNING)) {
							burn(adjacentBlockState, world, adjacentPos, true);
						}
					} else if (world.isAirBlock(adjacentPos)
							|| !adjacentBlockState.isSideSolid(world, adjacentPos, facing.getOpposite())
							|| block.isFlammable(world, adjacentPos, facing.getOpposite())) {
						world.setBlockState(pos, Blocks.FIRE.getDefaultState());
						return;
					}
				}
			}

			// 50/50 shot at bumping age. When can go past the MAX_STAGE it becomes an
			// ash block.
			if (rand.nextBoolean()) {
				if (state.getValue(STAGE) < MAX_STAGE) {
					world.setBlockState(pos, state.withProperty(STAGE, state.getValue(STAGE) + 1), 2);
				} else {
					world.setBlockState(pos, ModBlocks.ASH.getDefaultState(), 2);
				}
			}

			// Schedule our next update
			world.scheduleUpdate(pos, this, randomTickRate(world));
		}
	}

	@Override
	public int getFireSpreadSpeed(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos,
			@Nonnull final EnumFacing face) {
		return FIRE_SPREAD;
	}

	@Override
	public boolean isFlammable(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos,
			@Nonnull final EnumFacing face) {
		return true;
	}

	@Override
	public int getFlammability(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos,
			@Nonnull final EnumFacing face) {
		return FLAMMABILITY;
	}

	@Override
	public int getLightValue(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos) {
		if (state.getValue(IS_BURNING)) {
			return BURNING_LIGHT;
		}
		return super.getLightValue(state, world, pos);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final Random rand) {
		if (state.getValue(IS_BURNING)) {

			// Similar to the Furnace sound profile
			if (rand.nextDouble() < 0.1D) {
				final SoundEvent event;
				if (state.getValue(STAGE) < 4)
					event = SoundEvents.BLOCK_FIRE_AMBIENT;
				else
					event = SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE;
				world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, event, SoundCategory.BLOCKS,
						1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
			}

			final float f = pos.getX() + 0.5F;
			final float f1 = pos.getY() + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
			final float f2 = pos.getZ() + 0.5F;
			final float f3 = 0.52F;
			final float f4 = rand.nextFloat() * 0.6F - 0.3F;

			// Early stage burning is smokier than later stage
			final float large = (float) (MAX_STAGE - state.getValue(STAGE)) / (float) MAX_STAGE;
			if (rand.nextFloat() < large) {
				world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, f + f3 - 0.5, f1 + 1, f2 + f4, 0.0D, 0.15D, 0.0D);
			} else {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f3 - 0.5, f1 + 1, f2 + f4, 0.0D, 0.15D, 0.0D);
			}
		}
	}
}
