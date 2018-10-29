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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.common.item.magic.DeviceAbility;
import org.blockartistry.doodads.common.item.magic.DeviceQuality;
import org.blockartistry.doodads.common.item.magic.MagicDevice;
import org.blockartistry.doodads.common.item.magic.MagicDeviceType;
import org.blockartistry.doodads.common.item.magic.capability.CapabilityMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDeviceSettable;
import org.blockartistry.doodads.util.Localization;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicDevice extends ItemBase implements IBauble {

	protected static final String FORMAT_STRING = Localization.loadString("doodads.deviceability.format");
	private static final String DEVICE_NAME_SIMPLE_FMT = Localization.loadString("doodads.magicdevice.basicname");
	private static final String DEVICE_NAME_FANCY_FMT = Localization.loadString("doodads.magicdevice.fancyname");

	private static final Map<MagicDeviceType, String> DEVICE_TYPE_NAMES = new EnumMap<>(MagicDeviceType.class);
	private static final Map<DeviceQuality, String> DEVICE_QUALITY_NAMES = new EnumMap<>(DeviceQuality.class);

	static {
		for (final MagicDeviceType b : MagicDeviceType.values())
			DEVICE_TYPE_NAMES.put(b,
					Localization.loadString(ModInfo.MOD_ID + ".magicdevice." + b.name().toLowerCase() + ".name"));
		for (final DeviceQuality d : DeviceQuality.values())
			DEVICE_QUALITY_NAMES.put(d, Localization.loadString(d.getUnlocalizedName()));
	}

	public ItemMagicDevice(@Nonnull final String name) {
		super(name);
		setMaxStackSize(1);
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		for (final MagicDevice device : MagicDevice.DEVICES.values()) {
			final ItemStack stack = new ItemStack(this);
			final IMagicDeviceSettable xface = (IMagicDeviceSettable) stack
					.getCapability(CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
			xface.setMoniker(device.getUnlocalizedName());
			xface.setDeviceType(device.getType());
			xface.setQuality(device.getQuality());
			xface.setMaxEnergy(device.getQuality().getMaxPower());
			xface.setCurrentEnergy(xface.getMaxEnergy());
			for (final ResourceLocation r : device.getAbilities())
				xface.addAbilities(r);
			items.add(stack);
		}
	}

	@Override
	public void registerItemModel() {
		// Register for each of the bauble slots
		for (final MagicDeviceType bt : MagicDeviceType.values()) {
			if (bt.getBaubleType() == null) {
				Doodads.proxy().registerItemRenderer(this, 0,
						new ModelResourceLocation(ModInfo.MOD_ID + ":magic_device", "type=" + bt.name()));
			}
		}
	}

	public float getPowerRemaining(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		return caps != null ? caps.getPowerRatio() : 0F;
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		if (caps != null) {
			final String quality = DEVICE_QUALITY_NAMES.get(caps.getQuality());
			final String device = DEVICE_TYPE_NAMES.get(caps.getDeviceType());
			final String moniker = caps.getMoniker();
			if (moniker.isEmpty())
				return String.format(DEVICE_NAME_SIMPLE_FMT, quality, device);
			return String.format(DEVICE_NAME_FANCY_FMT, quality, device, Localization.loadString(moniker));
		}
		return "WFT?";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn,
			@Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
		tooltip.add(Localization.format("doodads.magicdevice.powerremaining", getPowerRemaining(stack)));
		gatherToolTips(stack, tooltip);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(@Nonnull final ItemStack stack) {
		return getQuality(stack) == DeviceQuality.LEGENDARY;
	}

	@Override
	@Nonnull
	public EnumRarity getRarity(@Nonnull final ItemStack stack) {
		return getQuality(stack).getRarity();
	}

	@Override
	public boolean isBookEnchantable(@Nonnull final ItemStack stack, @Nonnull final ItemStack book) {
		return false;
	}

	public DeviceQuality getQuality(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		return caps != null ? caps.getQuality() : DeviceQuality.MUNDANE;
	}

	@Nonnull
	public MagicDeviceType getDeviceType(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		return caps != null ? caps.getDeviceType() : MagicDeviceType.INERT;
	}

	@Nonnull
	public static ItemStack addDeviceAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		final IMagicDeviceSettable caps = (IMagicDeviceSettable) getCapability(stack);
		if (caps != null) {
			caps.addAbilities(ability);
		}
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Nonnull
	public static void gatherToolTips(@Nonnull final ItemStack stack, @Nonnull final List<String> tips) {
		process(stack, da -> tips.add(String.format(FORMAT_STRING, Localization.loadString(da.getUnlocalizedName()))));
	}

	@Nonnull
	public static List<String> getAbilities(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		return caps != null ? caps.getAbilities() : new ArrayList<>();
	}

	@Nullable
	public static IMagicDevice getCapability(@Nonnull final ItemStack stack) {
		return stack.getCapability(CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
	}
	
	/**
	 * This method is called once per tick if the bauble is being worn by a player
	 */
	public void onWornTick(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			process(itemstack, da -> da.doTick(player, itemstack));
	}

	/**
	 * This method is called when the bauble is equipped by a player
	 */
	public void onEquipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			process(itemstack, da -> da.equip(player, itemstack));
	}

	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	public void onUnequipped(@Nonnull final ItemStack itemstack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote)
			process(itemstack, da -> da.unequip(player, itemstack));
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
		return false;
	}

	private static void process(@Nonnull final ItemStack stack, @Nonnull final Consumer<DeviceAbility> op) {
		getAbilities(stack).stream().map(s -> DeviceAbility.REGISTRY.getValue(new ResourceLocation(s)))
				.filter(e -> e != null).sorted((a,b) -> a.getPriority() - b.getPriority()).forEach(op);
	}


}
