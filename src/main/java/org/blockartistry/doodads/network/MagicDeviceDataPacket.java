package org.blockartistry.doodads.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.common.item.magic.capability.CapabilityMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.MagicDeviceData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
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
	protected byte slot = 0;
	protected NBTTagCompound data;

	public MagicDeviceDataPacket() {
	}

	public MagicDeviceDataPacket(@Nonnull final EntityPlayer p, final int slot, @Nonnull final MagicDeviceData data) {
		this.slot = (byte) slot;
		this.playerId = p.getEntityId();
		this.data = data.serialize();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buffer) {
		buffer.writeInt(this.playerId);
		buffer.writeByte(this.slot);
		ByteBufUtils.writeTag(buffer, this.data);
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buffer) {
		this.playerId = buffer.readInt();
		this.slot = buffer.readByte();
		this.data = ByteBufUtils.readTag(buffer);
	}

	public static class Handler implements IMessageHandler<MagicDeviceDataPacket, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final MagicDeviceDataPacket message, @Nullable final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				final World world = Doodads.proxy().getClientWorld();
				if (world == null)
					return;
				final Entity p = world.getEntityByID(message.playerId);
				if (p != null && p instanceof EntityPlayer) {
					final InventoryPlayer inv = ((EntityPlayer) p).inventory;
					final ItemStack targetStack = inv.getStackInSlot(message.slot);
					if (!targetStack.isEmpty()) {
						final IMagicDevice xface = targetStack.getCapability(CapabilityMagicDevice.MAGIC_DEVICE,
								CapabilityMagicDevice.DEFAULT_FACING);
						if (xface != null)
							xface.deserialize(message.data);
					}
				}
			});
			return null;
		}
	}
}