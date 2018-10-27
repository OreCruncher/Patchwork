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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Configuration;
import org.blockartistry.doodads.client.DoodadsCreativeTab;

import net.minecraft.block.BlockFence;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMobNet extends ItemBase {

	private static class NBT {
		public static final String ID = "id";
		public static final String STORED_ENTITY = "entity";
		public static final String ENTITY_NAME = "name";
		public static final String MONIKER = "moniker";
	}

	public ItemMobNet() {
		super("mobnet");
		setCreativeTab(DoodadsCreativeTab.tab);
		setMaxStackSize(1);
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
		final StringBuilder baseName = new StringBuilder(super.getItemStackDisplayName(stack));

		if (hasCapturedAnimal(stack)) {
			String name = getProperty(stack, NBT.ENTITY_NAME);
			if (!name.isEmpty()) {
				baseName.append(" (").append(name);
				name = getProperty(stack, NBT.MONIKER);
				if (!name.isEmpty()) {
					baseName.append(" - ").append(name);
				}
				baseName.append(')');
			}
		}
		return baseName.toString();
	}

	@Override
	public boolean itemInteractionForEntity(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player,
			@Nonnull final EntityLivingBase target, @Nonnull final EnumHand hand) {
		if (!target.world.isRemote) {
			if (hand == EnumHand.OFF_HAND || !isValidTarget(target) || hasCapturedAnimal(stack))
				return false;

			final EntityLiving entity = (EntityLiving) target;
			final ItemStack newStack = addEntitytoNet(stack, entity);
			if (player.isCreative())
				player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, newStack);
			entity.playLivingSound();
			entity.world.removeEntity(target);
			return true;
		}
		return false;
	}

	@Override
	public EnumActionResult onItemUse(@Nonnull final EntityPlayer player, @Nonnull final World world,
			@Nonnull final BlockPos pos, @Nonnull final EnumHand hand, @Nonnull final EnumFacing facing,
			final float hitX, final float hitY, final float hitZ) {

		if (!world.isRemote) {

			if (hand == EnumHand.OFF_HAND && player.getHeldItemMainhand().getItem() == ModItems.MOB_NET) {
				return EnumActionResult.FAIL;
			}

			final ItemStack stack = player.getHeldItem(hand);
			final NBTTagCompound entitynbt = getEntityTag(stack);
			if (entitynbt != null) {

				final Entity entity = buildEntity(world, entitynbt);

				if (entity == null)
					return EnumActionResult.FAIL;

				final BlockPos pos2 = pos.offset(facing);
				double d0 = 0.0D;
				// Forge: Fix Vanilla bug comparing state instead of block
				if (facing == EnumFacing.UP && player.world.getBlockState(pos2).getBlock() instanceof BlockFence) {
					d0 = 0.5D;
				}

				entity.setPositionAndRotation(pos2.getX() + 0.5D, pos2.getY() + d0, pos2.getZ() + 0.5D,
						Math.abs(player.rotationYaw), 0);
				entity.motionX = 0;
				entity.motionY = 0;
				entity.motionZ = 0;

				world.spawnEntity(entity);

				if (entity instanceof EntityAnimal && ((EntityAnimal) entity).isAIDisabled()) {
					((EntityAnimal) entity).setNoAI(false);
				}

				stack.setTagCompound(null);

				if (!Configuration.mobnet.reusable)
					stack.setCount(stack.getCount() - 1);

				world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
						SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.NEUTRAL, 0.5F,
						0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

				return EnumActionResult.PASS;
			}
		}
		return EnumActionResult.FAIL;
	}

	@Nonnull
	private String getProperty(@Nonnull final ItemStack stack, @Nonnull final String prop) {
		if (stack.getTagCompound() != null) {
			return stack.getTagCompound().getString(prop);
		}
		return "";
	}

	private static boolean isValidTarget(@Nonnull final EntityLivingBase entity) {
		if (entity instanceof EntityVillager && Configuration.mobnet.enableVillagerCapture)
			return true;
		if (entity instanceof IMob && Configuration.mobnet.enableHostileCapture)
			return true;
		return entity instanceof EntityAnimal;
	}

	@Nullable
	private static boolean hasCapturedAnimal(@Nonnull final ItemStack stack) {
		return getEntityTag(stack) != null;
	}

	@Nonnull
	private static ItemStack addEntitytoNet(@Nonnull final ItemStack stack, @Nonnull Entity entity) {
		final NBTTagCompound eNBT = new NBTTagCompound();
		entity.onGround = true;
		eNBT.setString(NBT.ID, EntityList.getKey(entity).toString());
		entity.writeToNBT(eNBT);

		final String entityTypeName = resolveEntityProfession(entity);
		final NBTTagCompound stacknbt = new NBTTagCompound();
		stacknbt.setString(NBT.ID, EntityList.getKey(entity).toString());
		stacknbt.setString(NBT.ENTITY_NAME, StringUtils.isNullOrEmpty(entityTypeName) ? "Generic" : entityTypeName);
		if (entity.hasCustomName())
			stacknbt.setString(NBT.MONIKER, entity.getCustomNameTag());
		stacknbt.setTag(NBT.STORED_ENTITY, eNBT);

		stack.setTagCompound(stacknbt);
		return stack;
	}

	private static String resolveEntityProfession(@Nonnull final Entity entity) {
		final StringBuilder entityType = new StringBuilder(EntityList.getEntityString(entity));
		if (entity instanceof EntityVillager && !entity.hasCustomName()) {
			final String displayName = ((EntityVillager) entity).getDisplayName().getFormattedText();
			if (!StringUtils.isNullOrEmpty(displayName)) {
				entityType.append(": ");
				entityType.append(displayName);
			}
		}
		return entityType.toString();
	}

	@Nullable
	private static NBTTagCompound getEntityTag(@Nonnull final ItemStack stack) {
		final NBTTagCompound nbt = stack.getTagCompound();
		return nbt != null ? nbt.getCompoundTag(NBT.STORED_ENTITY) : null;
	}

	@Nullable
	private static Entity buildEntity(@Nonnull final World worldIn, @Nonnull final NBTTagCompound entitynbt) {
		try {
			return EntityList.createEntityFromNBT(entitynbt, worldIn);
		} catch (final Throwable e) {
			return null;
		}
	}
}
