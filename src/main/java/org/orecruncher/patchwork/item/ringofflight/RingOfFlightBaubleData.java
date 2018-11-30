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

package org.orecruncher.patchwork.item.ringofflight;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.patchwork.item.ItemRingOfFlight;
import org.orecruncher.patchwork.item.ItemRingOfFlight.Variant;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class RingOfFlightBaubleData implements IBauble {

	private static final float DEFAULT_FLY_SPEED = 0.05F;

	@Override
	@Nonnull
	public BaubleType getBaubleType(@Nonnull final ItemStack stack) {
		return BaubleType.RING;
	}

	@Override
	public void onWornTick(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		final EntityPlayer player = (EntityPlayer) entity;
		final World world = player.getEntityWorld();
		if (world.isRemote)
			return;

		final IRingOfFlightSettable caps = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
		if (caps != null && caps.getVariant() != ItemRingOfFlight.Variant.CORE) {
			if (caps.getRemainingDurability() > 0)
				tagAndBag(player, caps);
			if (player.capabilities.isCreativeMode || !player.capabilities.isFlying)
				return;
			if (!caps.damage(1)) {
				onUnequipped(stack, player);
			}
		}
	}

	@Override
	public void onEquipped(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		if (entity.getEntityWorld().isRemote)
			return;

		final EntityPlayer player = (EntityPlayer) entity;
		final IRingOfFlightSettable caps = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
		if (caps != null && caps.getRemainingDurability() > 0)
			tagAndBag(player, caps);
	}

	@Override
	public void onUnequipped(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		if (entity.getEntityWorld().isRemote)
			return;
		final IRingOfFlightSettable caps = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
		if (caps != null && caps.getVariant() != Variant.CORE) {
			final EntityPlayer player = (EntityPlayer) entity;
			if (!player.isCreative()) {
				player.capabilities.isFlying = false;
				player.capabilities.allowFlying = false;
			}
			player.capabilities.setFlySpeed(DEFAULT_FLY_SPEED);
			player.sendPlayerAbilities();
		}
	}

	private void tagAndBag(@Nonnull final EntityPlayer player, @Nonnull final IRingOfFlight caps) {
		if (!player.capabilities.allowFlying || player.capabilities.getFlySpeed() != caps.getVariant().getSpeed()) {
			player.capabilities.allowFlying = true;
			player.capabilities.setFlySpeed(caps.getVariant().getSpeed());
			player.sendPlayerAbilities();
		}
	}

	@Override
	public boolean canEquip(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		return ItemRingOfFlight.getActiveRing((EntityPlayer) entity).isEmpty();
	}

	@Override
	public boolean canUnequip(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		return true;
	}

	@Override
	public boolean willAutoSync(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		return false;
	}

	@Nonnull
	public static ICapabilityProvider createBaubleProvider(@Nonnull final ItemStack stack) {
		return new ICapabilityProvider() {

			@Override
			public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
				return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
			}

			@Nullable
			@Override
			public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
				return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE
						? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(new RingOfFlightBaubleData())
						: null;
			}

		};
	}
}
