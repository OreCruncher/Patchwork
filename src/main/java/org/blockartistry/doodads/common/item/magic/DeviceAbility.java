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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.doodads.ModInfo;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public abstract class DeviceAbility extends IForgeRegistryEntry.Impl<DeviceAbility> {

	public static final IForgeRegistry<DeviceAbility> REGISTRY = new RegistryBuilder<DeviceAbility>()
			.setName(new ResourceLocation(ModInfo.MOD_ID, "deviceabilities")).allowModification()
			.setType(DeviceAbility.class).create();

	private String unlocalizedName;
	private int priority = 10000;

	public DeviceAbility(@Nonnull final String name) {
		this.setRegistryName(name);
		setUnlocalizedName(ModInfo.MOD_ID + ".deviceability." + name + ".name");
	}

	@Nullable
	public String getUnlocalizedName() {
		return this.unlocalizedName;
	}

	@Nonnull
	protected DeviceAbility setUnlocalizedName(@Nonnull final String name) {
		this.unlocalizedName = name;
		return this;
	}

	@Nonnull
	public DeviceAbility register() {
		REGISTRY.register(this);
		return this;
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public DeviceAbility setPriority(final int p) {
		this.priority = p;
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
}
