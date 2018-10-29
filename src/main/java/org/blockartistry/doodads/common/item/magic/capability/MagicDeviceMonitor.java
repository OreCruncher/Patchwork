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

package org.blockartistry.doodads.common.item.magic.capability;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.network.MagicDeviceDataPacket;
import org.blockartistry.doodads.network.NetworkHandler;
import org.blockartistry.doodads.util.capability.CapabilityUtils;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public final class MagicDeviceMonitor {

	private static IMagicDeviceSettable getCaps(@Nonnull final ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return null;
		final IMagicDeviceSettable cap = (IMagicDeviceSettable) CapabilityUtils.getCapability(stack,
				CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
		return (cap != null && cap.isDirty()) ? cap : null;
	}

	@Nonnull
	private static boolean getBaubleCaps(@Nonnull final EntityPlayer p, @Nonnull final List<IMagicDevice> result) {
		boolean hasCaps = false;
		final IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(p);
		if (baubles != null) {
			final int count = baubles.getSlots();
			for (int i = 0; i < count; i++) {
				final IMagicDeviceSettable c = getCaps(baubles.getStackInSlot(i));
				if (c != null) {
					hasCaps = true;
					c.clearDirty();
				}
				result.add(c);
			}
		}
		return hasCaps;
	}

	@Nonnull
	private static boolean getInventoryCaps(@Nonnull final EntityPlayer p, @Nonnull final List<IMagicDevice> result) {
		boolean hasCaps = false;
		final int count = p.inventory.getSizeInventory();
		for (int i = 0; i < count; i++) {
			final IMagicDeviceSettable c = getCaps(p.inventory.getStackInSlot(i));
			if (c != null) {
				hasCaps = true;
				c.clearDirty();
			}
			result.add(c);
		}
		return hasCaps;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void playerTick(@Nonnull final TickEvent.PlayerTickEvent evt) {
		if (evt.side == Side.SERVER && evt.phase == Phase.END) {
			List<IMagicDevice> caps = new ArrayList<>();
			if (getBaubleCaps(evt.player, caps)) {
				final MagicDeviceDataPacket packet = new MagicDeviceDataPacket(evt.player, true, caps);
				NetworkHandler.INSTANCE.sendTo(packet, (EntityPlayerMP) evt.player);
			}

			caps = new ArrayList<>();
			if (getInventoryCaps(evt.player, caps)) {
				final MagicDeviceDataPacket packet = new MagicDeviceDataPacket(evt.player, false, caps);
				NetworkHandler.INSTANCE.sendTo(packet, (EntityPlayerMP) evt.player);
			}
		}
	}

}
