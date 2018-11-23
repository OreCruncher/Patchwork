/*
 * Licensed under the MIT License (MIT).
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
package org.orecruncher.patchwork.item;

import java.util.Comparator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.lib.IVariant;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.client.ModCreativeTab;
import org.orecruncher.patchwork.item.ringofflight.CapabilityRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.IRingOfFlight;
import org.orecruncher.patchwork.item.ringofflight.IRingOfFlightSettable;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// So we don't lose it
// https://github.com/spacechase0/SurvivalWings/blob/master/com/spacechase0/minecraft/wings/client/WingsModel.java
public class ItemRingOfFlight extends ItemBase {

	// Don't forget items if this is changed!
	private static final int BASE_DURABILITY = 1000;
	private static final int RINGSLOT1 = 1;
	private static final int RINGSLOT2 = 2;

	private static final ResourceLocation VARIANT_GETTER_ID = new ResourceLocation(ModInfo.MOD_ID, "rof_variant");
	private static final IItemPropertyGetter VARIANT_GETTER = new IItemPropertyGetter() {
		@SideOnly(Side.CLIENT)
		@Override
		public float apply(@Nonnull final ItemStack stack, @Nonnull final World worldIn,
				@Nonnull final EntityLivingBase entityIn) {
			return CapabilityRingOfFlight.getCapability(stack).getVariant().getSubTypeId();
		}
	};

	public ItemRingOfFlight() {
		super("ringofflight");
		setCreativeTab(ModCreativeTab.tab);
		setMaxStackSize(1);
		addPropertyOverride(VARIANT_GETTER_ID, VARIANT_GETTER);
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			for (final Variant v : Variant.values()) {
				final ItemStack stack = new ItemStack(this);
				final IRingOfFlightSettable caps = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
				caps.setVariant(v);
				caps.setDurability(v.getMaxDamage());
				items.add(stack);
			}
		}
	}

	@Override
	public boolean showDurabilityBar(@Nonnull final ItemStack stack) {
		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		return caps != null && caps.getVariant() != Variant.CORE && caps.getDurabilityForDisplay() != 0F;
	}

	@Override
	public double getDurabilityForDisplay(@Nonnull final ItemStack stack) {
		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		return caps != null ? caps.getDurabilityForDisplay() : 0F;
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		if (caps != null) {
			// Generate name based on variant
			return Localization.loadString(super.getTranslationKey() + "." + caps.getVariant().getName());
		}
		return "WFT?";
	}

	@Override
	public boolean isBookEnchantable(@Nonnull final ItemStack stack, @Nonnull final ItemStack book) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(@Nonnull final ItemStack stack) {
		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		return caps != null ? caps.getVariant().getRarity() : EnumRarity.COMMON;
	}

	@Nonnull
	public static ItemStack getActiveRing(@Nonnull final EntityPlayer player) {
		final IBaublesItemHandler h = BaublesApi.getBaublesHandler(player);
		ItemStack stack = h.getStackInSlot(RINGSLOT1);
		if (stack.getItem() instanceof ItemRingOfFlight)
			return stack;
		stack = h.getStackInSlot(RINGSLOT2);
		if (stack.getItem() instanceof ItemRingOfFlight)
			return stack;
		return ItemStack.EMPTY;
	}

	/*
	 * Used to make the ring inert. Happens when the ring runs out of juice.
	 */
	public void makeCore(@Nonnull final ItemStack stack) {
		final IRingOfFlightSettable caps = (IRingOfFlightSettable) CapabilityRingOfFlight.getCapability(stack);
		if (caps != null) {
			caps.setVariant(Variant.CORE);
			caps.setDurability(0);
		}
	}

	@Override
	@Nullable
	public NBTTagCompound getNBTShareTag(@Nonnull final ItemStack stack) {
		NBTTagCompound nbt = super.getNBTShareTag(stack);
		if (nbt == null)
			nbt = new NBTTagCompound();
		final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
		if (caps != null) {
			final NBTTagCompound tag = caps.serializeNBT();
			nbt.setTag(NBT.RING_DATA, tag);
		}
		return nbt;
	}

	@Override
	public void readNBTShareTag(@Nonnull final ItemStack stack, @Nullable final NBTTagCompound nbt) {
		super.readNBTShareTag(stack, nbt);
		if (nbt != null && nbt.hasKey(NBT.RING_DATA)) {
			final IRingOfFlight caps = CapabilityRingOfFlight.getCapability(stack);
			if (caps != null) {
				caps.deserializeNBT(nbt.getCompoundTag(NBT.RING_DATA));
			}
		}
	}

	private static class NBT {
		public static final String RING_DATA = "rof_data";
	}

	public static enum Variant implements IVariant {
		//@formatter:off
		CORE("core", 0, 0 * BASE_DURABILITY, 0F, EnumRarity.COMMON),
		FEATHER("feather", 1, 25 * BASE_DURABILITY, 0.05F, EnumRarity.COMMON),
		STURDY("sturdy", 2, 75 * BASE_DURABILITY, 0.05F, EnumRarity.UNCOMMON),
		OBSIDIAN("obsidian", 3, 100 * BASE_DURABILITY, 0.02F, EnumRarity.UNCOMMON),
		SPEED("speed", 4, 50 * BASE_DURABILITY, 0.08F, EnumRarity.RARE);
		//@formatter:on

		private static final Variant[] SUBTYPE_LOOKUP = Stream.of(values())
				.sorted(Comparator.comparing(Variant::getSubTypeId)).toArray(Variant[]::new);

		private final String name;
		private final int subTypeId;
		private final int maxDamage;
		private final float speed;
		private final EnumRarity rarity;

		private Variant(@Nonnull final String name, final int id, final int maxDamage, final float speed,
				@Nonnull final EnumRarity r) {
			this.name = name;
			this.subTypeId = id;
			this.maxDamage = maxDamage;
			this.speed = speed;
			this.rarity = r;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public int getSubTypeId() {
			return this.subTypeId;
		}

		public int getMaxDamage() {
			return this.maxDamage;
		}

		public float getSpeed() {
			return this.speed;
		}

		public EnumRarity getRarity() {
			return this.rarity;
		}

		@Nonnull
		public static Variant bySubTypeId(final int subTypeId) {
			return SUBTYPE_LOOKUP[MathStuff.clamp(subTypeId, 0, SUBTYPE_LOOKUP.length - 1)];
		}
	}
}
