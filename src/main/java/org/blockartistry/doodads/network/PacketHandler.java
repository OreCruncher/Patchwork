package org.blockartistry.doodads.network;

import org.blockartistry.doodads.ModInfo;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MOD_ID.toLowerCase());

	public static void init()
	{
		int i = 0;
		INSTANCE.registerMessage(MagicDeviceDataPacket.Handler.class, MagicDeviceDataPacket.class, i++, Side.CLIENT);
	}
}