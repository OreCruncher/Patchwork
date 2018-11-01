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

package org.blockartistry.patchwork.network;

import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.patchwork.ModBase;
import org.blockartistry.patchwork.common.item.magic.capability.CapabilityMagicDevice;
import org.blockartistry.patchwork.common.item.magic.capability.IMagicDevice;
import org.blockartistry.patchwork.util.capability.CapabilityUtils;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MagicDeviceDataPacket implements IMessage {

	protected int playerId;
	protected boolean baublesSync;
	protected final Int2ObjectOpenHashMap<NBTTagCompound> data = new Int2ObjectOpenHashMap<>();

	public MagicDeviceDataPacket() {
	}

	public MagicDeviceDataPacket(@Nonnull final EntityPlayer p, final boolean isBaubles,
			@Nonnull final List<IMagicDevice> items) {
		this.playerId = p.getEntityId();
		this.baublesSync = isBaubles;

		for (int index = 0; index < items.size(); index++) {
			final IMagicDevice data = items.get(index);
			if (data != null) {
				final NBTTagCompound d = data.serializeNBT();
				this.data.put(index, d);
			}
		}
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buffer) {
		buffer.writeInt(this.playerId);
		buffer.writeBoolean(this.baublesSync);
		buffer.writeByte(this.data.size());
		for (final Entry<Integer, NBTTagCompound> e : this.data.entrySet()) {
			buffer.writeByte(e.getKey());
			ByteBufUtils.writeTag(buffer, e.getValue());
		}
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buffer) {
		this.playerId = buffer.readInt();
		this.baublesSync = buffer.readBoolean();
		final int size = buffer.readByte();
		for (int i = 0; i < size; i++) {
			final int idx = buffer.readByte();
			final NBTTagCompound d = ByteBufUtils.readTag(buffer);
			this.data.put(idx, d);
		}
	}

	public static class Handler implements IMessageHandler<MagicDeviceDataPacket, IMessage> {

		private static interface SlotCracker {
			ItemStack getStack(int slot);

			void putStack(int slot, ItemStack stack);
		}

		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final MagicDeviceDataPacket message, @Nullable final MessageContext ctx) {
			ModBase.proxy().getThreadListener(ctx).addScheduledTask(() -> {
				final World world = ModBase.proxy().getClientWorld();
				if (world == null)
					return;
				final Entity p = world.getEntityByID(message.playerId);
				if (p != null && p instanceof EntityPlayer) {
					final SlotCracker cracker;
					if (message.baublesSync) {
						final IBaublesItemHandler t = BaublesApi.getBaublesHandler((EntityPlayer) p);
						if (t != null) {
							cracker = new SlotCracker() {
								@Override
								public ItemStack getStack(int slot) {
									return t.getStackInSlot(slot);
								}

								@Override
								public void putStack(int slot, ItemStack stack) {
									t.setStackInSlot(slot, stack);
								}
							};
						} else {
							cracker = null;
						}

					} else {
						final InventoryPlayer inv = ((EntityPlayer) p).inventory;
						cracker = new SlotCracker() {
							@Override
							public ItemStack getStack(int slot) {
								return inv.getStackInSlot(slot);
							}

							@Override
							public void putStack(int slot, ItemStack stack) {
								inv.setInventorySlotContents(slot, stack);
							}
						};
					}

					if (cracker == null)
						return;

					for (final Entry<Integer, NBTTagCompound> e : message.data.entrySet()) {
						final int index = e.getKey();
						final NBTTagCompound data = e.getValue();

						final ItemStack originalStack = cracker.getStack(index);
						final IMagicDevice originalHandler = CapabilityUtils.getCapability(originalStack,
								CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);

						if (originalHandler != null) {
							originalHandler.deserializeNBT(data);
						}
					}
				}
			});
			return null;
		}
	}
}