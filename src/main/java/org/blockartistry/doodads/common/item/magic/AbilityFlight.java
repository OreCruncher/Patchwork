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

package org.blockartistry.doodads.common.item.magic;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class AbilityFlight extends DeviceAbility {

	public AbilityFlight() {
		super("flight");
	}

	/**
	 * Called when a player equips the item into a slot.
	 * 
	 * @param player
	 * @param device
	 */
	public void equip(@Nonnull final EntityLivingBase entity, @Nonnull final ItemStack device) {
		final EntityPlayerMP player = (EntityPlayerMP) entity;
		ensureFlightSet(player);
	}

	/**
	 * Called when a player unequips an item from a slot
	 * 
	 * @param player
	 * @param device
	 */
	public void unequip(@Nonnull final EntityLivingBase entity, @Nonnull final ItemStack device) {
		final EntityPlayerMP player = (EntityPlayerMP) entity;
		if (!player.isCreative()) {
			player.capabilities.allowFlying = false;
			player.capabilities.isFlying = false;
			player.sendPlayerAbilities();
		}
	}

	/**
	 * Called every tick that the item is worn
	 * 
	 * @param player
	 * @param device
	 */
	public void doTick(@Nonnull final EntityLivingBase entity, @Nonnull final ItemStack device) {
		final EntityPlayerMP player = (EntityPlayerMP) entity;
		if (!player.isCreative()) {
			ensureFlightSet(player);
			if (player.capabilities.isFlying) {
				device.damageItem(1, entity);
				if (device.isEmpty())
					unequip(entity, device);
			}
		}
	}

	protected void ensureFlightSet(@Nonnull final EntityPlayerMP player) {
		if (!player.isCreative() && !player.capabilities.allowFlying) {
			player.capabilities.allowFlying = true;
			player.sendPlayerAbilities();
		}
	}

}
