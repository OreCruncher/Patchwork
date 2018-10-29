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
import org.blockartistry.doodads.util.Localization;
import org.codehaus.plexus.util.StringUtils;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicDevice extends ItemBase {

	// Number of ticks in a minute so damage can be
	// computed based on time.
	protected static final int TICKS_IN_MINUTES = 20 * 60;

	// Tag compound for when a stack does not have one.
	protected static final NBTTagCompound NONE = new NBTTagCompound();

	protected static final String FORMAT_STRING = Localization.loadString("doodads.deviceability.format");
	protected static final String NAMESPACE = ModInfo.MOD_ID + ":namespace";
	protected static final String ABILITIES = "abil";
	protected static final String DEVICE_TYPE = "type";
	protected static final String DEVICE_QUALITY = "qual";
	protected static final String DEVICE_MONIKER = "mon";

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
		setPowerMinutes(120);
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		for (final MagicDevice device : MagicDevice.DEVICES.values()) {
			if(device.getType().getBaubleType() == null) {
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
			if(bt.getBaubleType() == null) {
				Doodads.proxy().registerItemRenderer(this, 0,
					new ModelResourceLocation(ModInfo.MOD_ID + ":magic_device", "type=" + bt.name()));
			}
		}
	}

	public void setPowerMinutes(final int minutes) {
		setMaxDamage(minutes * TICKS_IN_MINUTES);
	}

	@SuppressWarnings("deprecation")
	public float getPowerRemaining(@Nonnull final ItemStack stack) {
		return (float) (this.getMaxDamage() - getDamage(stack)) / (float) this.getMaxDamage();
	}

	protected void setDeviceType(@Nonnull final ItemStack stack, @Nonnull final MagicDeviceType type) {
		setProperty(stack, DEVICE_TYPE, type.name());
	}

	protected void addAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		addDeviceAbility(stack, ability);
	}

	protected void setQuality(@Nonnull final ItemStack stack, @Nonnull final DeviceQuality quality) {
		setProperty(stack, DEVICE_QUALITY, quality.name());
	}

	protected void setProperty(@Nonnull final ItemStack stack, @Nonnull final String name,
			@Nonnull final String value) {
		getWriteNameSpace(stack).setString(name, value);
	}

	@Nonnull
	public String getProperty(@Nonnull final ItemStack stack, @Nonnull final String name) {
		return getReadOnlyNameSpace(stack).getString(name);
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
		final String quality = DEVICE_QUALITY_NAMES.get(getQuality(stack));
		final String device = DEVICE_TYPE_NAMES.get(getDeviceType(stack));
		final String moniker = getProperty(stack, DEVICE_MONIKER);
		if (moniker.isEmpty())
			return String.format(DEVICE_NAME_SIMPLE_FMT, quality, device);
		return String.format(DEVICE_NAME_FANCY_FMT, quality, device, Localization.loadString(moniker));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn,
			@Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
		tooltip.add(Localization.format("doodads.magicdevice.powerremaining", getPowerRemaining(stack) * 100F));
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
		final String t = getReadOnlyNameSpace(stack).getString(DEVICE_QUALITY);
		return StringUtils.isEmpty(t) ? DeviceQuality.MUNDANE : DeviceQuality.valueOf(t);
	}

	@Nonnull
	public MagicDeviceType getDeviceType(@Nonnull final ItemStack stack) {
		final String t = getReadOnlyNameSpace(stack).getString(DEVICE_TYPE);
		return StringUtils.isEmpty(t) ? MagicDeviceType.INERT : MagicDeviceType.valueOf(t);
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

	@Nonnull
	public static ItemStack addDeviceAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		if (DeviceAbility.REGISTRY.containsKey(ability)) {
			final NBTTagCompound nbt = getWriteNameSpace(stack);
			NBTTagList theList = null;
			if (nbt.hasKey(ABILITIES))
				theList = nbt.getTagList(ABILITIES, 8);
			else
				nbt.setTag(ABILITIES, theList = new NBTTagList());
			theList.appendTag(new NBTTagString(ability.toString()));
		}
		return stack;
	}

	@Nonnull
	protected static NBTTagCompound getWriteNameSpace(@Nonnull final ItemStack stack) {
		return stack.getOrCreateSubCompound(NAMESPACE);
	}
	
	@Nonnull
	protected static NBTTagCompound getReadOnlyNameSpace(@Nonnull final ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NAMESPACE))
			return stack.getTagCompound().getCompoundTag(NAMESPACE);
		return NONE;
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
		final NBTTagList nbt = getReadOnlyNameSpace(stack).getTagList(ABILITIES, 8); // String
		final int count = nbt.tagCount();
		for (int i = 0; i < count; i++)
			result.add(nbt.getStringTagAt(i));
		return result;
	}
}
