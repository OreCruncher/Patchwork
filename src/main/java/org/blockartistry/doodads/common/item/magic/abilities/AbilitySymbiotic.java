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

package org.blockartistry.doodads.common.item.magic.abilities;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.magic.AbilityHandler;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDeviceSettable;
import org.blockartistry.doodads.util.XpUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class AbilitySymbiotic extends AbilityHandler {

	// Amount of energy gained per point of xp
	private static final int CONVERSION_RATE = 100;

	// Amount of XP that can be drained per tick
	private static final int DRAIN_CAP = 5;

	public AbilitySymbiotic() {
		super("symbiotic");
		setPriority(100000);
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
		final int energyDifferential = caps.getMaxEnergy() - caps.getCurrentEnergy();
		if (energyDifferential > 0) {
			final int xpNeeded = Math.min(DRAIN_CAP, energyDifferential / CONVERSION_RATE);
			final EntityPlayerMP player = (EntityPlayerMP) entity;
			if (player.experienceTotal > 0 && xpNeeded > 0) {
				final int toDrain = Math.min(player.experienceTotal, xpNeeded);
				XpUtil.addPlayerXP(player, -toDrain);
				((IMagicDeviceSettable) caps).addCurrentEnergy(toDrain * CONVERSION_RATE);
			}
		}
	}

}
