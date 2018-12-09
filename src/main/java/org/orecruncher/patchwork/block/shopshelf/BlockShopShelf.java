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
import javax.annotation.Nullable;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.block.BlockContainerBase;
import org.orecruncher.patchwork.block.IRotateable;
import org.orecruncher.patchwork.block.ITileEntityRegistration;
import org.orecruncher.patchwork.block.ITileEntityTESR;
import org.orecruncher.patchwork.block.ModBlocks;
import org.orecruncher.patchwork.client.ModCreativeTab;
import org.orecruncher.patchwork.lib.property.PropertyItemStack;
import org.orecruncher.patchwork.network.GuiHandler;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockShopShelf extends BlockContainerBase
		implements ITileEntityRegistration, ITileEntityTESR, IRotateable {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5D);
	protected static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 1.0D, 1.0D);
	protected static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

	// Unlisted property for conveying texture
	public static final PropertyItemStack MIMIC = new PropertyItemStack();

	public BlockShopShelf() {
		super("shopshelf", Material.WOOD);

		setHardness(2.5F);
		setSoundType(SoundType.WOOD);
		setHarvestLevel("axe", 1);
		setCreativeTab(ModCreativeTab.tab);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public void registerTileEntity() {
		GameRegistry.registerTileEntity(TileEntityShopShelf.class, new ResourceLocation(ModInfo.MOD_ID, "shopshelf"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerTESR() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShopShelf.class, new TESRShopShelf());
	}

	@Override
	@Nonnull
	public TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
		return new TileEntityShopShelf();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer.Builder(this).add(FACING).add(MIMIC).build();
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

	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos) {
		final IExtendedBlockState extendedState = (IExtendedBlockState) state;

		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityShopShelf) {
			final TileEntityShopShelf s = (TileEntityShopShelf) te;
			return extendedState.withProperty(MIMIC, s.getMimic());
		}

		return super.getExtendedState(state, world, pos);
	}

	@Override
	public boolean isFullCube(@Nonnull final IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(@Nonnull final IBlockState state) {
		return false;
	}
	
	@Override
	public int getLightOpacity(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos) {
		return 0;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
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

	/*
	 * Keep inventory of the block when the block is harvested. From BlockFlowerPot.
	 */
	@Override
	public void getDrops(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockAccess world,
			@Nonnull final BlockPos pos, @Nonnull final IBlockState state, final int fortune) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityShopShelf) {
			final TileEntityShopShelf ss = (TileEntityShopShelf) te;
			if (!ss.isEmpty()) {
				final NBTTagCompound nbt = ss.serializeNBT();
				final ItemStack stack = new ItemStack(ModBlocks.SHOPSHELF, 1);
				stack.setTagInfo("BlockEntityTag", nbt);
				stack.setStackDisplayName(ss.getName());
				drops.add(stack);
				return;
			}
		}
		super.getDrops(drops, world, pos, state, fortune);
	}

	/*
	 * Make sure the block position is wiped out. From BlockFlowerPot.
	 */
	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te,
			ItemStack tool) {
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}

	/*
	 * Only remove the block if it is OK to remove. From BlockFlowerPot.
	 *
	 */
	@Override
	public boolean removedByPlayer(@Nonnull final IBlockState state, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final EntityPlayer player, boolean willHarvest) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityShopShelf) {
			final TileEntityShopShelf ss = (TileEntityShopShelf) te;
			if (!ss.okToBreak(player))
				return false;
			if (!player.capabilities.isCreativeMode)
				return willHarvest;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public float getExplosionResistance(@Nonnull final World world, @Nonnull final BlockPos pos,
			@Nullable final Entity exploder, @Nonnull final Explosion explosion) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityShopShelf) {
			final TileEntityShopShelf ss = (TileEntityShopShelf) te;
			if (ss.isAdminShop())
				return 18000000F; // Bedrock
			else if (ss.isOwned())
				return 6000F; // Obsidian
		}
		return super.getExplosionResistance(world, pos, exploder, explosion);
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
					if (player.capabilities.isCreativeMode)
						ss.setAdminShop();
					else
						ss.setOwner(player);
				}

				// Get the ItemStack in the hand they are activating with. If it
				// is an appropriate mimic block set that.
				if (ss.canConfigure(player)) {
					final ItemStack stack = player.getHeldItem(hand);
					if (ss.isValidMimic(stack)) {
						ss.setMimic(stack);
						return true;
					}
				}

				final GuiHandler.ID openAs;
				if (player.isSneaking()) {
					openAs = GuiHandler.ID.SHOP_SHELF;
				} else if (ss.isOwner(player)) {
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

	@Override
	public boolean rotateBlock(@Nonnull final EntityPlayer player, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final EnumFacing axis) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityShopShelf) {
			final TileEntityShopShelf ss = (TileEntityShopShelf) te;
			if (!ss.canConfigure(player))
				return false;
			super.rotateBlock(world, pos, EnumFacing.UP);
			final NBTTagCompound nbt = te.serializeNBT();
			world.getTileEntity(pos).deserializeNBT(nbt);
			return true;
		}
		return super.rotateBlock(world, pos, axis);
	}

}
