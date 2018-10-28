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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.common.item.magic.MagicDeviceHelper;
import org.blockartistry.doodads.util.Localization;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicDevice extends ItemBase implements IBauble {

	// Number of ticks in a minute so damage can be
	// computed based on time.
	protected static final int TICKS_IN_MINUTES = 20 * 60;

	public static enum Quality {
		REGULAR(1), HIGH(2), LEGENDARY(3);

		private final int maxAbilities;
		private final String unlocalizedName;

		private Quality(final int maxAbilities) {
			this.maxAbilities = maxAbilities;
			this.unlocalizedName = String.format("%s.%s.name", Doodads.MOD_ID, name().toLowerCase());
		}

		public int getMaxAbilities() {
			return this.maxAbilities;
		}

		@Nonnull
		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}
	}

	private final BaubleType deviceType;
	private Quality quality;

	public ItemMagicDevice(@Nonnull final String name, @Nonnull final BaubleType type) {
		super(name);
		this.deviceType = type;
		this.quality = Quality.REGULAR;
		setMaxStackSize(1);
		setPowerMinutes(120);
	}

	public ItemMagicDevice setQuality(@Nonnull final Quality q) {
		this.quality = q;
		return this;
	}

	public Quality getQuality() {
		return this.quality;
	}

	public void setPowerMinutes(final int minutes) {
		setMaxDamage(minutes * TICKS_IN_MINUTES);
	}

	@SuppressWarnings("deprecation")
	public float getPowerRemaining(@Nonnull final ItemStack stack) {
		return (float) (this.getMaxDamage() - getDamage(stack)) / (float) this.getMaxDamage();
	}

	public void addAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		MagicDeviceHelper.addDeviceAbility(stack, ability);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn,
			@Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
		tooltip.add(Localization.format("doodads.magicaldevice.powerremaining", getPowerRemaining(stack) * 100F));
		MagicDeviceHelper.gatherToolTips(stack, tooltip);
	}

	@Override
	public boolean isBookEnchantable(@Nonnull final ItemStack stack, @Nonnull final ItemStack book) {
		return false;
	}

	@Override
	@Nonnull
	public BaubleType getBaubleType(@Nonnull final ItemStack itemstack) {
		return this.deviceType;
	}

	/**
	 * This method is called once per tick if the bauble is being worn by a player
	 */
	@Override
	public void onWornTick(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			MagicDeviceHelper.process(itemstack, da -> da.doTick(player, itemstack));
	}

	/**
	 * This method is called when the bauble is equipped by a player
	 */
	@Override
	public void onEquipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			MagicDeviceHelper.process(itemstack, da -> da.equip(player, itemstack));
	}

	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	@Override
	public void onUnequipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			MagicDeviceHelper.process(itemstack, da -> da.unequip(player, itemstack));
	}

}
