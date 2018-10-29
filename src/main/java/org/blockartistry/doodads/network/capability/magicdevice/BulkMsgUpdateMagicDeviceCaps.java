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
import org.blockartistry.doodads.network.capability.MessageBulkUpdateContainerCapability;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class BulkMsgUpdateMagicDeviceCaps extends MessageBulkUpdateContainerCapability<IMagicDevice, NBTTagCompound> {

	public BulkMsgUpdateMagicDeviceCaps(Capability<IMagicDevice> capability, EnumFacing facing, int windowID,
			NonNullList<ItemStack> items) {
		super(capability, facing, windowID, items);
	}

	@Override
	@Nonnull
	protected NBTTagCompound convertCapabilityToData(@Nonnull final IMagicDevice handler) {
		return handler.serialize();
	}

	@Override
	@Nonnull
	protected NBTTagCompound readCapabilityData(@Nonnull final ByteBuf buf) {
		return ByteBufUtils.readTag(buf);
	}

	@Override
	protected void writeCapabilityData(@Nonnull final ByteBuf buf, @Nonnull final NBTTagCompound data) {
		ByteBufUtils.writeTag(buf, data);
	}

	public static class Handler extends
			MessageBulkUpdateContainerCapability.Handler<IMagicDevice, NBTTagCompound, BulkMsgUpdateMagicDeviceCaps> {

		@Override
		protected void applyCapabilityData(@Nonnull final IMagicDevice handler, @Nonnull final NBTTagCompound data) {
			handler.deserialize(data);
		}

	}

}
