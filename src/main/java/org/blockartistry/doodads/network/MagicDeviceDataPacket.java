package org.blockartistry.doodads.network;

import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.common.item.magic.capability.CapabilityMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDevice;
import org.blockartistry.doodads.util.capability.CapabilityUtils;

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
				final NBTTagCompound d = data.serialize();
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
			Doodads.proxy().getThreadListener(ctx).addScheduledTask(() -> {
				final World world = Doodads.proxy().getClientWorld();
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
							final ItemStack newStack = originalStack.copy();

							final IMagicDevice newHandler = CapabilityUtils.getCapability(newStack,
									CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
							assert newHandler != null;

							newHandler.deserialize(data);

							if (!originalHandler.equals(newHandler)) {
								cracker.putStack(index, newStack);
							}
						}
					}
				}
			});
			return null;
		}
	}
}