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

package org.orecruncher.patchwork.block.furnace3d;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.block.BlockContainerBase;
import org.orecruncher.patchwork.block.ITileEntityRegistration;
import org.orecruncher.patchwork.block.ITileEntityTESR;
import org.orecruncher.patchwork.client.ModCreativeTab;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFurnace3D extends BlockContainerBase implements ITileEntityRegistration, ITileEntityTESR {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	public BlockFurnace3D() {
		super("furnace", Material.ROCK);

		setHardness(3.5F);
		setSoundType(SoundType.STONE);
		setCreativeTab(ModCreativeTab.tab);
		setDefaultState(
				this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTIVE, false));
	}

	@Override
	public void registerTileEntity() {
		GameRegistry.registerTileEntity(TileEntityFurnace3D.class, new ResourceLocation(ModInfo.MOD_ID, "furnace3d"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerTESR() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFurnace3D.class, new TESRFurnace3D());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityFurnace3D();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, ACTIVE });
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to
	 * allow for adjustments to the IBlockstate
	 */
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(final int meta) {
		final int bits = (meta & 0x7);
		final int activeBits = (bits & 1);
		final int facingBits = (bits >> 1);
		EnumFacing enumfacing = EnumFacing.byHorizontalIndex(facingBits);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return getDefaultState().withProperty(FACING, enumfacing).withProperty(ACTIVE, activeBits != 0);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		final int activeBits = state.getValue(ACTIVE).booleanValue() ? 1 : 0;
		return activeBits | ((state.getValue(FACING).getHorizontalIndex()) << 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final Random rand) {
		if (state.getValue(ACTIVE).booleanValue()) {
			final double d0 = pos.getX() + rand.nextDouble() * 0.7 + 0.1;
			final double d2 = pos.getZ() + rand.nextDouble() * 0.7 + 0.1;
			final double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;

			if (rand.nextDouble() < 0.1D) {
				world.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}

			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			world.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	@Override
	public boolean onBlockActivated(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand,
			@Nonnull final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return true;
		} else {
			final TileEntity tileentity = world.getTileEntity(pos);

			if (tileentity instanceof TileEntityFurnace3D) {
				player.displayGUIChest((TileEntityFurnace3D) tileentity);
				player.addStat(StatList.FURNACE_INTERACTION);
			}

			return true;
		}
	}

	@Override
	public void breakBlock(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
		final TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityFurnace3D) {
			InventoryHelper.dropInventoryItems(world, pos, (TileEntityFurnace3D) tileentity);
			world.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean hasComparatorInputOverride(@Nonnull final IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(@Nonnull final IBlockState blockState, @Nonnull final World world,
			@Nonnull final BlockPos pos) {
		return Container.calcRedstone(world.getTileEntity(pos));
	}

	@Override
	public int getLightValue(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos) {
		return state.getValue(ACTIVE).booleanValue() ? 7 : 0;
	}
}
