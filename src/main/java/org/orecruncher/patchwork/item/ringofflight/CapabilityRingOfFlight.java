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

import org.orecruncher.lib.capability.CapabilityProviderSerializable;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.item.ItemRingOfFlight;
import org.orecruncher.patchwork.network.NetworkHandler;
import org.orecruncher.patchwork.network.PacketRingOfFlight;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.Baubles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public class CapabilityRingOfFlight {

	@CapabilityInject(IRingOfFlight.class)
	public static final Capability<IRingOfFlight> RING_DEVICE = null;

	private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModInfo.MOD_ID, "rof_caps");
	private static final ResourceLocation BAUBLE_CAPABILITY_ID = new ResourceLocation(Baubles.MODID, "bauble_cap");

	public static void register() {
		CapabilityManager.INSTANCE.register(IRingOfFlight.class, new Capability.IStorage<IRingOfFlight>() {
			@Override
			public NBTBase writeNBT(@Nonnull final Capability<IRingOfFlight> capability,
					@Nonnull final IRingOfFlight instance, @Nullable final EnumFacing side) {
				return ((INBTSerializable<NBTTagCompound>) instance).serializeNBT();
			}

			@Override
			public void readNBT(@Nonnull final Capability<IRingOfFlight> capability,
					@Nonnull final IRingOfFlight instance, @Nullable final EnumFacing side,
					@Nonnull final NBTBase nbt) {
				((INBTSerializable<NBTTagCompound>) instance).deserializeNBT((NBTTagCompound) nbt);
			}
		}, () -> new RingOfFlightData());
	}

	@Nullable
	public static IRingOfFlight getCapability(@Nonnull final ItemStack stack) {
		return stack.getCapability(RING_DEVICE, null);
	}

	@Nonnull
	public static ICapabilityProvider createProvider() {
		return new CapabilityProviderSerializable<>(RING_DEVICE, null);
	}

	@EventBusSubscriber(modid = ModInfo.MOD_ID)
	public static class EventHandler {

		private static final int RING1 = 1;
		private static final int RING2 = 2;

		/*
		 * Attach the capability to the ItemStack when it is created.
		 */
		@SubscribeEvent
		public static void attachCapabilities(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
			final ItemStack stack = event.getObject();
			if (stack.getItem() instanceof ItemRingOfFlight) {
				event.addCapability(CAPABILITY_ID, createProvider());
				event.addCapability(BAUBLE_CAPABILITY_ID, RingOfFlightBaubleData.createBaubleProvider(stack));
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void playerTick(@Nonnull final TickEvent.PlayerTickEvent evt) {
			if (evt.side == Side.SERVER && evt.phase == Phase.END) {
				final World world = evt.player.getEntityWorld();
				if ((world.getTotalWorldTime() % 5) != 0)
					return;
				final IBaublesItemHandler h = BaublesApi.getBaublesHandler(evt.player);
				if (h != null) {
					processSlot(evt.player, h, RING1);
					processSlot(evt.player, h, RING2);
				}
			}
		}

		private static void processSlot(@Nonnull final EntityPlayer player, @Nonnull final IBaublesItemHandler handler,
				final int slot) {
			final ItemStack stack = handler.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				final IRingOfFlightSettable caps = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
				if (caps != null && caps.isDirty()) {
					caps.clearDirty();
					final PacketRingOfFlight message = new PacketRingOfFlight(player, slot, stack);
					NetworkHandler.INSTANCE.sendTo(message, (EntityPlayerMP) player);
				}
			}
		}
	}

}
