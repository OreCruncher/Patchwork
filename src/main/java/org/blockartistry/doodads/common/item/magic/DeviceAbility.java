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

package org.blockartistry.doodads.common.item.magic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.util.Localization;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public abstract class DeviceAbility extends IForgeRegistryEntry.Impl<DeviceAbility> {

	public static final IForgeRegistry<DeviceAbility> REGISTRY = new RegistryBuilder<DeviceAbility>()
			.setName(new ResourceLocation(Doodads.MOD_ID, "deviceabilities")).allowModification()
			.setType(DeviceAbility.class).create();

	public static class NBT {
		public static final String ABILITIES = "abilities";
	}

	public static interface IProcessor {
		void doProcess(@Nonnull DeviceAbility da);
	}

	private String unlocalizedName;

	public DeviceAbility(@Nonnull final String name) {
		this.setRegistryName(name);
		this.setUnlocalizedName(Doodads.MOD_ID + ".deviceability." + name + ".name");
	}

	@Nullable
	public String getUnlocalizedName() {
		return this.unlocalizedName;
	}

	@Nonnull
	public DeviceAbility setUnlocalizedName(@Nonnull final String name) {
		this.unlocalizedName = name;
		return this;
	}

	@Nonnull
	public DeviceAbility register() {
		REGISTRY.register(this);
		return this;
	}

	/**
	 * Called when a player equips the item into a slot.
	 * 
	 * @param player
	 * @param device
	 */
	public void equip(@Nonnull final EntityLivingBase player, @Nonnull final ItemStack device) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when a player unequips an item from a slot
	 * 
	 * @param player
	 * @param device
	 */
	public void unequip(@Nonnull final EntityLivingBase player, @Nonnull final ItemStack device) {
		// Override to provide functionality
	}

	/**
	 * Called every tick that the item is worn
	 * 
	 * @param player
	 * @param device
	 */
	public void doTick(@Nonnull final EntityLivingBase player, @Nonnull final ItemStack device) {
		// Override in a sub-class to provide functionality
	}

	// =====================
	//
	// Helper methods
	//
	// =====================

	@Nonnull
	public static ItemStack addDeviceAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		if (REGISTRY.containsKey(ability)) {
			// Ensure the stack has a tag compound
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
				stack.setTagCompound(nbt = new NBTTagCompound());
			NBTTagList theList = null;
			if (nbt.hasKey(NBT.ABILITIES))
				theList = nbt.getTagList(NBT.ABILITIES, 8);
			else
				nbt.setTag(NBT.ABILITIES, theList = new NBTTagList());
			theList.appendTag(new NBTTagString(ability.toString()));
		}
		return stack;
	}

	public static List<String> gatherToolTips(@Nonnull final ItemStack stack) {
		final List<String> result = new ArrayList<>();
		process(stack, da -> {
			final String name = Localization.loadString(da.getUnlocalizedName());
			final String msg = Localization.format("doodads.deviceability.format", name);
			result.add(msg);
		});
		return result;
	}

	public static void process(@Nonnull final ItemStack stack, @Nonnull final IProcessor pred) {
		if (stack.hasTagCompound()) {
			final NBTTagList nbt = stack.getTagCompound().getTagList(NBT.ABILITIES, 8); // String
			if (!nbt.hasNoTags()) {
				final Iterator<NBTBase> itr = nbt.iterator();
				while (itr.hasNext()) {
					final NBTBase e = itr.next();
					final String id = ((NBTTagString) e).getString();
					final DeviceAbility da = DeviceAbility.REGISTRY.getValue(new ResourceLocation(id));
					if (da != null)
						pred.doProcess(da);
				}
			}
		}
	}

}
