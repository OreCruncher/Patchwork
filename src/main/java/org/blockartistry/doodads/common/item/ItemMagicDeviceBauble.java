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

package org.blockartistry.doodads.common.item;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.magic.MagicDevice;
import org.blockartistry.doodads.common.item.magic.MagicDeviceType;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class ItemMagicDeviceBauble extends ItemMagicDevice implements IBauble {

	public ItemMagicDeviceBauble() {
		super("bauble_device");
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		for (final MagicDevice device : MagicDevice.DEVICES.values()) {
			if(device.getType().getBaubleType() != null) {
				final ItemStack stack = new ItemStack(this);
				setProperty(stack, DEVICE_MONIKER, device.getUnlocalizedName());
				setDeviceType(stack, device.getType());
				setQuality(stack, device.getQuality());
				for (final ResourceLocation r : device.getAbilities())
					addAbility(stack, r);
				items.add(stack);
			}
		}
	}

	@Override
	public void registerItemModel() {
		// Register for each of the bauble slots
		for (final MagicDeviceType bt : MagicDeviceType.values()) {
			if(bt.getBaubleType() != null) {
				Doodads.proxy().registerItemRenderer(this, 0,
					new ModelResourceLocation(ModInfo.MOD_ID + ":magic_device", "type=" + bt.name()));
			}
		}
	}

	protected void setBaubleType(@Nonnull final ItemStack stack, @Nonnull final BaubleType type) {
		setDeviceType(stack, MagicDeviceType.fromBauble(type));
	}

	protected void setDeviceType(@Nonnull final ItemStack stack, @Nonnull final MagicDeviceType type) {
		if (type.getBaubleType() == null)
			throw new IllegalStateException("Attempt to set a MagicDeviceType that is not supported by Bauble");
		super.setDeviceType(stack, type);
	}

	/**
	 * This method return the type of bauble this is. Type is used to determine the
	 * slots it can go into.
	 */
	@Nonnull
	public BaubleType getBaubleType(@Nonnull final ItemStack itemstack) {
		return getDeviceType(itemstack).getBaubleType();
	}

	/**
	 * This method is called once per tick if the bauble is being worn by a player
	 */
	@Override
	public void onWornTick(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		super.onWornTick(itemstack, player);
	}

	/**
	 * This method is called when the bauble is equipped by a player
	 */
	@Override
	public void onEquipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		super.onEquipped(itemstack, player);
	}

	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	@Override
	public void onUnequipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		super.onUnequipped(itemstack, player);
	}

	/**
	 * can this bauble be placed in a bauble slot
	 */
	@Override
	public boolean canEquip(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		return true;
	}

	/**
	 * Can this bauble be removed from a bauble slot
	 */
	@Override
	public boolean canUnequip(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		return true;
	}

	/**
	 * Will bauble automatically sync to client if a change is detected in its NBT
	 * or damage values? Default is off, so override and set to true if you want to
	 * auto sync. This sync is not instant, but occurs every 10 ticks (.5 seconds).
	 */
	@Override
	public boolean willAutoSync(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		return true;
	}

}
