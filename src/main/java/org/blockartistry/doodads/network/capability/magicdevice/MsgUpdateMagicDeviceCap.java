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

package org.blockartistry.doodads.network.capability.magicdevice;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.magic.capability.IMagicDevice;
import org.blockartistry.doodads.network.capability.MessageUpdateContainerCapability;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MsgUpdateMagicDeviceCap extends MessageUpdateContainerCapability<IMagicDevice, NBTTagCompound> {

	protected MsgUpdateMagicDeviceCap(Capability<IMagicDevice> capability, EnumFacing facing, int windowID,
			int slotNumber, IMagicDevice handler) {
		super(capability, facing, windowID, slotNumber, handler);
	}

	@Override
	@Nonnull
	protected NBTTagCompound convertCapabilityToData(@Nonnull final IMagicDevice handler) {
		return handler.serialize();
	}

	@Override
	protected NBTTagCompound readCapabilityData(@Nonnull final ByteBuf buf) {
		return ByteBufUtils.readTag(buf);
	}

	@Override
	protected void writeCapabilityData(@Nonnull final ByteBuf buf, @Nonnull final NBTTagCompound data) {
		ByteBufUtils.writeTag(buf, data);
	}

	public static class Handler
			extends MessageUpdateContainerCapability.Handler<IMagicDevice, NBTTagCompound, MsgUpdateMagicDeviceCap> {

		@Override
		protected void applyCapabilityData(@Nonnull final IMagicDevice handler, @Nonnull final NBTTagCompound data) {
			handler.deserialize(data);
		}
	}
}
