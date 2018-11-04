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

package org.orecruncher.patchwork.common.item.magic.abilities;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.common.item.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.magic.AbilityHandler;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class AbilityFlight extends AbilityHandler {

	private static final int POWER_COST = 10;

	public AbilityFlight() {
		super("flight");
	}

	@Override
	public boolean canBeAppliedTo(@Nonnull final ItemMagicDevice.Type type) {
		return type == ItemMagicDevice.Type.AMULET || type == ItemMagicDevice.Type.BODY
				|| type == ItemMagicDevice.Type.BELT;
	}

	/**
	 * Called when a player equips the item into a slot.
	 *
	 * @param player
	 * @param device
	 */
	@Override
	public void equip(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase entity,
			@Nonnull final ItemStack device) {
		final EntityPlayerMP player = (EntityPlayerMP) entity;
		ensureFlightSet(player);
	}

	/**
	 * Called when a player unequips an item from a slot
	 *
	 * @param player
	 * @param device
	 */
	@Override
	public void unequip(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase entity,
			@Nonnull final ItemStack device) {
		final EntityPlayerMP player = (EntityPlayerMP) entity;
		if (!player.isCreative()) {
			player.capabilities.isFlying = false;
			ensureFlightNotSet(player);
		}
	}

	/**
	 * Called every tick that the item is worn
	 *
	 * @param player
	 * @param device
	 */
	@Override
	public void doTick(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase entity,
			@Nonnull final ItemStack device) {
		final EntityPlayerMP player = (EntityPlayerMP) entity;
		if (!player.isCreative()) {
			final IMagicDeviceSettable c = (IMagicDeviceSettable) caps;
			if (caps.hasEnergyFor(POWER_COST)) {
				ensureFlightSet(player);
				if (player.capabilities.isFlying)
					c.consumeEnergy(POWER_COST);
			} else {
				unequip(caps, player, device);
			}
		}
	}

	protected void ensureFlightNotSet(@Nonnull final EntityPlayerMP player) {
		if (player.capabilities.allowFlying) {
			player.capabilities.allowFlying = false;
			player.sendPlayerAbilities();
		}
	}

	protected void ensureFlightSet(@Nonnull final EntityPlayerMP player) {
		if (!player.capabilities.allowFlying) {
			player.capabilities.allowFlying = true;
			player.sendPlayerAbilities();
		}
	}

}
