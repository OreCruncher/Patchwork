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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.magic.DeviceAbility;
import org.blockartistry.doodads.common.item.magic.MagicDevice;
import org.blockartistry.doodads.util.Localization;
import org.codehaus.plexus.util.StringUtils;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicDevice extends ItemBase implements IBauble {

	// Number of ticks in a minute so damage can be
	// computed based on time.
	protected static final int TICKS_IN_MINUTES = 20 * 60;

	private static final String LOC_DEVICEABILITY_FMT = "doodads.deviceability.format";
	private static final String FORMAT_STRING = Localization.loadString(LOC_DEVICEABILITY_FMT);
	private static final String NAMESPACE = ModInfo.MOD_ID + ":namespace";
	private static final String ABILITIES = "abilities";
	private static final String DEVICE_TYPE = "type";

	public ItemMagicDevice(@Nonnull final String name) {
		super(name);
		setMaxStackSize(1);
		setPowerMinutes(120);
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		for (final MagicDevice device : MagicDevice.DEVICES.values()) {
			final ItemStack stack = new ItemStack(this);
			for (final ResourceLocation r : device.getAbilities())
				this.addAbility(stack, r);
			this.setBaubleType(stack, device.getType());
			items.add(stack);
		}
	}

	public void registerItemModel() {
		for(final BaubleType bt: BaubleType.values()) {
			Doodads.proxy().registerItemRenderer(this, 0, new ModelResourceLocation(ModInfo.MOD_ID + ":magic_device", "type=" + bt.name()));
		}
	}

	public void setPowerMinutes(final int minutes) {
		setMaxDamage(minutes * TICKS_IN_MINUTES);
	}

	@SuppressWarnings("deprecation")
	public float getPowerRemaining(@Nonnull final ItemStack stack) {
		return (float) (this.getMaxDamage() - getDamage(stack)) / (float) this.getMaxDamage();
	}

	protected void addAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		addDeviceAbility(stack, ability);
	}

	protected void setBaubleType(@Nonnull final ItemStack stack, @Nonnull final BaubleType type) {
		final NBTTagCompound nbt = stack.getOrCreateSubCompound(NAMESPACE);
		nbt.setString(DEVICE_TYPE, type.name());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn,
			@Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
		tooltip.add(Localization.format("doodads.magicaldevice.powerremaining", getPowerRemaining(stack) * 100F));
		gatherToolTips(stack, tooltip);
	}

	@Override
	public boolean isBookEnchantable(@Nonnull final ItemStack stack, @Nonnull final ItemStack book) {
		return false;
	}

	@Override
	@Nonnull
	public BaubleType getBaubleType(@Nonnull final ItemStack stack) {
		final String t = stack.getOrCreateSubCompound(NAMESPACE).getString(DEVICE_TYPE);
		return StringUtils.isEmpty(t) ? BaubleType.TRINKET : BaubleType.valueOf(t);
	}

	/**
	 * This method is called once per tick if the bauble is being worn by a player
	 */
	@Override
	public void onWornTick(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			process(itemstack, da -> da.doTick(player, itemstack));
	}

	/**
	 * This method is called when the bauble is equipped by a player
	 */
	@Override
	public void onEquipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			process(itemstack, da -> da.equip(player, itemstack));
	}

	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	@Override
	public void onUnequipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			process(itemstack, da -> da.unequip(player, itemstack));
	}

	@Nonnull
	public static ItemStack addDeviceAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		if (DeviceAbility.REGISTRY.containsKey(ability)) {
			final NBTTagCompound nbt = stack.getOrCreateSubCompound(NAMESPACE);
			NBTTagList theList = null;
			if (nbt.hasKey(ABILITIES))
				theList = nbt.getTagList(ABILITIES, 8);
			else
				nbt.setTag(ABILITIES, theList = new NBTTagList());
			theList.appendTag(new NBTTagString(ability.toString()));
		}
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Nonnull
	public static void gatherToolTips(@Nonnull final ItemStack stack, @Nonnull final List<String> tips) {
		process(stack, da -> tips.add(String.format(FORMAT_STRING, Localization.loadString(da.getUnlocalizedName()))));
	}

	public static void process(@Nonnull final ItemStack stack, @Nonnull final Consumer<DeviceAbility> op) {
		getAbilities(stack).stream().map(s -> DeviceAbility.REGISTRY.getValue(new ResourceLocation(s)))
				.filter(e -> e != null).forEach(op);
	}

	@Nonnull
	public static List<String> getAbilities(@Nonnull final ItemStack stack) {
		final List<String> result = new ArrayList<>();
		final NBTTagList nbt = stack.getOrCreateSubCompound(NAMESPACE).getTagList(ABILITIES, 8); // String
		final int count = nbt.tagCount();
		for (int i = 0; i < count; i++)
			result.add(nbt.getStringTagAt(i));
		return result;
	}

}
