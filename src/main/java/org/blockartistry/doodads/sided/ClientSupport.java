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

package org.blockartistry.doodads.sided;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.ItemBase;
import org.blockartistry.doodads.util.ForgeUtils;
import org.blockartistry.doodads.util.Localization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientSupport extends SideSupport {

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		super.postInit(event);

		// Fancify our mod config info page
		final ModMetadata data = ForgeUtils.getModMetadata(ModInfo.MOD_ID);
		if (data != null) {
			data.name = Localization.format(ModInfo.MOD_ID + ".metadata.Name");
			data.credits = Localization.format(ModInfo.MOD_ID + ".metadata.Credits");
			data.description = Localization.format(ModInfo.MOD_ID + ".metadata.Description");
			data.authorList = Arrays
					.asList(StringUtils.split(Localization.format(ModInfo.MOD_ID + ".metadata.Authors"), ','));
		}
	}

	@Override
	public void registerItemRenderer(@Nonnull final ItemBase item, final int meta,
			@Nonnull final ModelResourceLocation loc) {
		ModelLoader.setCustomModelResourceLocation(item, meta, loc);
		// new ModelResourceLocation(ModInfo.MOD_ID + ":" + id, "inventory"));
	}

	@Override
	@Nullable
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

	@Override
	public IThreadListener getThreadListener(@Nonnull final MessageContext context) {
		if (context.side.isClient()) {
			return Minecraft.getMinecraft();
		} else {
			return context.getServerHandler().player.mcServer;
		}
	}

	@Override
	@Nonnull
	public EntityPlayer getPlayer(@Nonnull final MessageContext context) {
		if (context.side.isClient()) {
			return Minecraft.getMinecraft().player;
		} else {
			return context.getServerHandler().player;
		}
	}
}
