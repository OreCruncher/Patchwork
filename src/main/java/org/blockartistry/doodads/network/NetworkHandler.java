package org.blockartistry.doodads.network;

import org.blockartistry.doodads.ModInfo;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MOD_ID);

	private static int msgId = 1;

	public static void init() {
		registerMessage(MagicDeviceDataPacket.Handler.class, MagicDeviceDataPacket.class, Side.CLIENT);
	}

	private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
			Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType,
			Side receivingSide) {
		INSTANCE.registerMessage(messageHandler, requestMessageType, msgId++, receivingSide);
	}
}