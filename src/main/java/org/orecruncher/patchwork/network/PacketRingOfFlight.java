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
package org.orecruncher.patchwork.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.item.ItemRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.CapabilityRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.IRingOfFlight;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRingOfFlight implements IMessage {

	protected int playerId;
	protected int slotId;
	protected NBTTagCompound nbt;

	public PacketRingOfFlight() {
		// Need for client side factory
	}

	public PacketRingOfFlight(@Nonnull final EntityPlayer p, final int slot, @Nonnull final ItemStack stack) {
		this.playerId = p.getEntityId();
		this.slotId = slot;

		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		this.nbt = caps.serializeNBT();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buffer) {
		buffer.writeInt(this.playerId);
		buffer.writeByte(this.slotId);
		ByteBufUtils.writeTag(buffer, this.nbt);
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buffer) {
		this.playerId = buffer.readInt();
		this.slotId = buffer.readByte();
		this.nbt = ByteBufUtils.readTag(buffer);
	}

	public static class Handler implements IMessageHandler<PacketRingOfFlight, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketRingOfFlight message, @Nullable final MessageContext ctx) {
			ModBase.proxy().getThreadListener(ctx).addScheduledTask(() -> {
				final World world = ModBase.proxy().getClientWorld();
				if (world == null)
					return;
				final Entity p = world.getEntityByID(message.playerId);
				if (p != null && p instanceof EntityPlayer) {
					final IBaublesItemHandler t = BaublesApi.getBaublesHandler((EntityPlayer) p);
					if (t != null) {
						final ItemStack stack = t.getStackInSlot(message.slotId);
						if (!stack.isEmpty() && stack.getItem() instanceof ItemRingOfFlight) {
							final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
							if (caps != null)
								caps.deserializeNBT(message.nbt);
						}
					}
				}
			});
			return null;
		}
	}
}