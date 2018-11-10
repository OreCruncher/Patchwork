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

package org.orecruncher.patchwork.item.magic.abilities;

import javax.annotation.Nonnull;

import org.orecruncher.lib.Localization;
import org.orecruncher.patchwork.item.magic.AbilityHandler;
import org.orecruncher.patchwork.item.magic.ItemMagicDevice;
import org.orecruncher.patchwork.item.magic.capability.IMagicDevice;
import org.orecruncher.patchwork.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AbilityPotion extends AbilityHandler {

	// Consumption calculated at the rate of per minute, so it would be
	// 120 energy per minute of effect.  (Figuring base unit consume per 5
	// seconds.)
	private static final int POWER_COST = ItemMagicDevice.BASE_CONSUMPTION_UNIT * 12;
	// 2 minute duration
	private static final int MAX_DURATION = 2 * 60 * 20;
	// Trigger update on a minute interval.  Attempt to minimize processing as much as possible
	private static final int UPDATE_DURATION = MAX_DURATION - (20 * 60); // Every 1 mins

	private final Potion potion;
	private int amplifier;

	public AbilityPotion(@Nonnull final String potionName) {
		super("potion_" + potionName);
		this.potion = Potion.REGISTRY.getObject(new ResourceLocation(potionName));
		final String pName = Localization.loadString(this.potion.getName());
		this.tipText = new String[] { Localization.format("patchwork.magicdevice.potion_tooltip", pName) };
		this.name = pName;
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

		// Quick check ... no power no worky
		if (!caps.hasEnergyFor(POWER_COST))
			return;

		final EntityPlayerMP player = (EntityPlayerMP) entity;

		final PotionEffect effect = player.getActivePotionEffect(this.potion);
		if (effect == null || effect.getDuration() < UPDATE_DURATION) {
			final PotionEffect update = new PotionEffect(this.potion, MAX_DURATION, this.amplifier, true, true);
			player.addPotionEffect(update);
			final IMagicDeviceSettable c = (IMagicDeviceSettable) caps;
			c.consumeEnergy(POWER_COST);
		}
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
		player.removePotionEffect(this.potion);
	}

}
