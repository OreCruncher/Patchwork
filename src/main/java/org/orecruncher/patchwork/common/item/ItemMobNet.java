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

package org.orecruncher.patchwork.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.patchwork.ModOptions;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.compat.EntityVillagerUtil;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.client.ModCreativeTab;

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
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMobNet extends ItemBase {

	private static final ResourceLocation VARIANT_GETTER_ID = new ResourceLocation(ModInfo.MOD_ID, "variant");
	private static final IItemPropertyGetter VARIANT_GETTER = new IItemPropertyGetter() {
		@SideOnly(Side.CLIENT)
		@Override
		public float apply(@Nonnull final ItemStack stack, @Nonnull final World worldIn,
				@Nonnull final EntityLivingBase entityIn) {
			return hasCapturedAnimal(stack) ? 1 : 0;
		}
	};

	public ItemMobNet() {
		super("mobnet");
		setCreativeTab(ModCreativeTab.tab);
		setMaxStackSize(1);
		addPropertyOverride(VARIANT_GETTER_ID, VARIANT_GETTER);
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
		final StringBuilder baseName = new StringBuilder(super.getItemStackDisplayName(stack));

		if (hasCapturedAnimal(stack)) {
			String name = getProperty(stack, NBT.ENTITY_TYPE_NAME);
			if (!name.isEmpty()) {
				baseName.append(" (").append(Localization.loadString(name));
				name = getProperty(stack, NBT.ENTITY_PROFESSION);
				if (!name.isEmpty()) {
					baseName.append(": ").append(Localization.loadString(name));
				}
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
		if (!target.world.isRemote && isWieldingCorrect(player, hand) && isValidTarget(target)
				&& !hasCapturedAnimal(stack)) {
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

			if (!isWieldingCorrect(player, hand))
				return EnumActionResult.FAIL;

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

				if (!ModOptions.mobnet.reusable)
					stack.setCount(stack.getCount() - 1);

				world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
						SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.NEUTRAL, 0.5F,
						0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

				return EnumActionResult.PASS;
			}
		}
		return EnumActionResult.FAIL;
	}

	private static boolean isWieldingCorrect(@Nonnull final EntityPlayer player, @Nonnull EnumHand hand) {
		return hand == EnumHand.MAIN_HAND && player.getHeldItemMainhand().getItem() == ModItems.MOB_NET
				&& player.getHeldItemOffhand().isEmpty();
	}

	@Nonnull
	private String getProperty(@Nonnull final ItemStack stack, @Nonnull final String prop) {
		if (stack.getTagCompound() != null) {
			return stack.getTagCompound().getString(prop);
		}
		return "";
	}

	private static boolean isValidTarget(@Nonnull final EntityLivingBase entity) {
		if (entity.isBeingRidden() || entity.isRiding())
			return false;
		if (entity instanceof EntityVillager && ModOptions.mobnet.enableVillagerCapture)
			return true;
		if (entity instanceof IMob && ModOptions.mobnet.enableHostileCapture)
			return true;
		return entity instanceof EntityAnimal;
	}

	@Nullable
	private static boolean hasCapturedAnimal(@Nonnull final ItemStack stack) {
		return getEntityTag(stack) != null;
	}

	@Nonnull
	private static ItemStack addEntitytoNet(@Nonnull final ItemStack stack, @Nonnull Entity entity) {
		// Build out the stack
		final NBTTagCompound stacknbt = new NBTTagCompound();
		stack.setTagCompound(stacknbt);

		// Gather up the entity
		final NBTTagCompound eNBT = new NBTTagCompound();
		eNBT.setString(NBT.ID, EntityList.getKey(entity).toString());
		entity.onGround = true;
		entity.writeToNBT(eNBT);
		stacknbt.setTag(NBT.STORED_ENTITY, eNBT);

		// Fill out the details
		stacknbt.setString(NBT.ID, EntityList.getKey(entity).toString());
		gatherEntityNames(entity, stacknbt);

		return stack;
	}

	private static void gatherEntityNames(@Nonnull final Entity entity, @Nonnull final NBTTagCompound nbt) {

		String temp = null;

		// Gather up the entity type name. This will be the resource string to be used
		// to translate the name on the client. Should resovlve to something like
		// "Creeper".
		temp = EntityList.getEntityString(entity);
		if (temp == null)
			temp = "generic";
		nbt.setString(NBT.ENTITY_TYPE_NAME, "entity." + temp + ".name");

		// Get the profession. Applicable for Villagers now a days.
		if (entity instanceof EntityVillager) {
			final EntityVillager villager = (EntityVillager) entity;
			final int careerId = EntityVillagerUtil.getCareerId(villager);
			temp = villager.getProfessionForge().getCareer(careerId - 1).getName();
			nbt.setString(NBT.ENTITY_PROFESSION, "entity.Villager." + temp);
		}

		// Get the custom name, or moniker, if present
		if (entity.hasCustomName()) {
			nbt.setString(NBT.MONIKER, entity.getCustomNameTag());
		}
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

	private static class NBT {
		public static final String ID = "id";
		public static final String STORED_ENTITY = "ent";
		public static final String ENTITY_TYPE_NAME = "typ";
		public static final String ENTITY_PROFESSION = "pro";
		public static final String MONIKER = "mon";
	}
}
