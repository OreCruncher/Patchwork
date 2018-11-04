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

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.common.item.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.magic.AbilityHandler;
import org.orecruncher.patchwork.common.item.magic.MagicUtilities;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class AbilityCharging extends AbilityHandler {

	// Max amount of energy that can be shared per tick
	private static final int MAX_CHARGE = 100;

	public AbilityCharging() {
		super("charging");
		setPriority(0);
	}

	@Override
	public void doTick(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase entity,
			@Nonnull final ItemStack device) {
		if (caps.getCurrentEnergy() > 0) {
			final List<ItemStack> devices = MagicUtilities.getMagicDevices((EntityPlayerMP) entity);
			if (devices.size() > 0) {
				int energyForDisbursment = Math.min(MAX_CHARGE, caps.getCurrentEnergy());
				for (final ItemStack stack : devices) {
					// Skip ourselves for obvious reasons
					if (stack.equals(device))
						continue;
					final IMagicDevice deviceCap = ItemMagicDevice.getCapability(stack);
					if (deviceCap != null) {
						final int energyNeeded = deviceCap.getMaxEnergy() - deviceCap.getCurrentEnergy();
						if (energyNeeded > 0) {
							final int amountXferred = Math.min(energyNeeded, energyForDisbursment);
							energyForDisbursment -= amountXferred;
							((IMagicDeviceSettable)caps).addCurrentEnergy(-amountXferred);
							((IMagicDeviceSettable) deviceCap).addCurrentEnergy(amountXferred);
						}
					}

					// If there is no energy left to disburse bail
					if (energyForDisbursment <= 0)
						break;
				}
			}
		}
	}

}
