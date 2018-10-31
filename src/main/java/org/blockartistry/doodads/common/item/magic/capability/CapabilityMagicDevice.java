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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.util.capability.CapabilityProviderSerializable;

import baubles.common.Baubles;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityMagicDevice {

	@CapabilityInject(IMagicDevice.class)
	public static final Capability<IMagicDevice> MAGIC_DEVICE = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModInfo.MOD_ID, "magicdevice_caps");
	public static final EnumFacing DEFAULT_FACING = null;

	private static final ResourceLocation BAUBLE_CAPABILITY_ID = new ResourceLocation(Baubles.MODID, "bauble_cap");

	public static void register() {
		CapabilityManager.INSTANCE.register(IMagicDevice.class, new Capability.IStorage<IMagicDevice>() {
			@Override
			public NBTBase writeNBT(@Nonnull final Capability<IMagicDevice> capability,
					@Nonnull final IMagicDevice instance, @Nullable final EnumFacing side) {
				return ((INBTSerializable<NBTTagCompound>) instance).serializeNBT();
			}

			@Override
			public void readNBT(@Nonnull final Capability<IMagicDevice> capability,
					@Nonnull final IMagicDevice instance, @Nullable final EnumFacing side, @Nonnull final NBTBase nbt) {
				((INBTSerializable<NBTTagCompound>) instance).deserializeNBT((NBTTagCompound) nbt);
			}
		}, () -> new MagicDeviceData());
	}

	@Nonnull
	public static ICapabilityProvider createProvider() {
		return new CapabilityProviderSerializable<>(MAGIC_DEVICE, DEFAULT_FACING);
	}

	@EventBusSubscriber(modid = ModInfo.MOD_ID)
	public static class EventHandler {
		/*
		 * Attach the capability to the ItemStack when it is created.
		 */
		@SubscribeEvent
		public static void attachCapabilities(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
			final ItemStack stack = event.getObject();
			if (stack.getItem() instanceof ItemMagicDevice) {
				event.addCapability(CAPABILITY_ID, createProvider());

				if (ItemMagicDevice.Type.byMetadata(stack.getMetadata()).getBaubleType() != null) {
					event.addCapability(BAUBLE_CAPABILITY_ID, BaubleAdaptor.createBaubleProvider(stack));
				}
			}
		}
	}
}
