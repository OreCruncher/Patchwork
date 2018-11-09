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

import org.orecruncher.lib.Localization;
import org.orecruncher.patchwork.common.item.magic.AbilityHandler;
import org.orecruncher.patchwork.common.item.magic.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class AbilityPotion extends AbilityHandler {

	private static final int POWER_COST = ItemMagicDevice.BASE_CONSUMPTION_UNIT;

	private final Potion potion;
	private int amplifier;

	public AbilityPotion(@Nonnull final String potionName) {
		super("potion_" + potionName);
		this.potion = Potion.REGISTRY.getObject(new ResourceLocation(potionName));
		final String pName = Localization.loadString(this.potion.getName());
		this.tipText = new String[] { Localization.format("patchwork.magicdevice.potion_tooltip", pName) };
		this.name = TextFormatting.YELLOW + pName;
	}

	@Override
	public boolean canBeAppliedTo(@Nonnull final ItemMagicDevice.Type type) {
		return type.getBaubleType() != null;
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
		if (effect == null || effect.getDuration() < 5) {
			final PotionEffect update = new PotionEffect(this.potion, 5 * 20, this.amplifier, true, true);
			player.addPotionEffect(update);
			final IMagicDeviceSettable c = (IMagicDeviceSettable) caps;
			c.consumeEnergy(POWER_COST);
		}
	}

}
