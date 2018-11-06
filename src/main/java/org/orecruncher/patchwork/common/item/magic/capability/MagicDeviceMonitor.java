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

package org.orecruncher.patchwork.common.item.magic.capability;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.lib.capability.CapabilityUtils;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.common.item.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.ModItems;
import org.orecruncher.patchwork.network.MagicDeviceDataPacket;
import org.orecruncher.patchwork.network.NetworkHandler;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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

	private static void tickStack(@Nonnull final EntityPlayer player, @Nonnull final ItemStack stack,
			@Nonnull final ItemMagicDevice.TickWhen... when) {
		final IMagicDevice c = ItemMagicDevice.getCapability(stack);
		if (c != null) {
			boolean doTheTick = false;
			for (int i = 0; i < when.length && !doTheTick; i++)
				doTheTick = ItemMagicDevice.Type.bySubTypeId(stack.getMetadata()).canTick(when[i]);
			if (doTheTick)
				ModItems.MAGIC_DEVICE.onWornTick(stack, player);
		}
	}

	public static void tickInventory(@Nonnull final EntityPlayer player, @Nonnull final NonNullList<ItemStack> inv,
			@Nonnull final ItemMagicDevice.TickWhen when) {
		final ItemStack heldStack = player.getHeldItemMainhand();
		for (final ItemStack stack : inv) {
			// We skip the held item - that is processed separately from the general
			// iteration
			if (!heldStack.equals(stack) && !stack.isEmpty() && stack.getItem() instanceof ItemMagicDevice) {
				tickStack(player, stack, when);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void playerTick(@Nonnull final TickEvent.PlayerTickEvent evt) {
		if (evt.side == Side.SERVER && evt.phase == Phase.END) {

			// First thing - tick the items in inventory
			tickInventory(evt.player, evt.player.inventory.mainInventory, ItemMagicDevice.TickWhen.ALWAYS);
			tickInventory(evt.player, evt.player.inventory.armorInventory, ItemMagicDevice.TickWhen.WORN);
			tickStack(evt.player, evt.player.getHeldItemMainhand(), ItemMagicDevice.TickWhen.HELD,
					ItemMagicDevice.TickWhen.ALWAYS);
			tickStack(evt.player, evt.player.getHeldItemOffhand(), ItemMagicDevice.TickWhen.HELD,
					ItemMagicDevice.TickWhen.ALWAYS);

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
