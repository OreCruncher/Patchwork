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

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.block.BlockContainerBase;
import org.orecruncher.patchwork.block.ITileEntityRegistration;
import org.orecruncher.patchwork.client.ModCreativeTab;
import org.orecruncher.patchwork.network.GuiHandler;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockShopShelf extends BlockContainerBase implements ITileEntityRegistration {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5D);
	protected static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 1.0D, 1.0D);
	protected static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

	public BlockShopShelf() {
		super("shopshelf", Material.WOOD);

		setHardness(3.5F);
		setSoundType(SoundType.WOOD);
		setCreativeTab(ModCreativeTab.tab);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		// Doesn't occupy the full block and doesn't block light
		this.fullBlock = false;
	}

	@Override
	public void registerTileEntity() {
		GameRegistry.registerTileEntity(TileEntityShopShelf.class, new ResourceLocation(ModInfo.MOD_ID, "shopshelf"));
	}

	@Override
	@Nonnull
	public TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
		return new TileEntityShopShelf();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
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
		final int bits = (meta & 0x3);
		EnumFacing enumfacing = EnumFacing.byHorizontalIndex(bits);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return getDefaultState().withProperty(FACING, enumfacing);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(@Nonnull final IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public boolean isFullCube(@Nonnull final IBlockState state) {
		return false;
	}

	@Override
	public boolean doesSideBlockRendering(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos, @Nonnull final EnumFacing face) {
		return false;
	}

	@Override
	public boolean doesSideBlockChestOpening(@Nonnull final IBlockState blockState, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos, @Nonnull final EnumFacing side) {
		return false;
	}

	@Override
	@Nonnull
	public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState state, @Nonnull final IBlockAccess source,
			@Nonnull final BlockPos pos) {
		final EnumFacing facing = state.getValue(FACING);
		switch (facing) {
		case SOUTH:
			return AABB_SOUTH;
		case EAST:
			return AABB_EAST;
		case WEST:
			return AABB_WEST;
		case NORTH:
			return AABB_NORTH;
		default:
			return AABB_NORTH;
		}
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	@Override
	public boolean onBlockActivated(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final IBlockState state, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand,
			@Nonnull final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
		if (!world.isRemote) {
			
			final TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityShopShelf) {
				final TileEntityShopShelf ss = (TileEntityShopShelf) te;
				if (!ss.isOwned() && !ss.isAdminShop()) {
					ss.setOwner(player);
				}
				
				final GuiHandler.ID openAs;
				if (player.isSneaking()) {
					openAs = GuiHandler.ID.SHOP_SHELF;
				} else if(ss.isOwner(player)) {
					openAs = GuiHandler.ID.SHOP_SHELF_OWNER;
				} else if (player.capabilities.isCreativeMode) {
					openAs = GuiHandler.ID.SHOP_SHELF_OWNER;
				} else {
					openAs = GuiHandler.ID.SHOP_SHELF;
				}
				
				player.openGui(ModBase.instance(), openAs.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
			}
		}

		return true;
	}
}
