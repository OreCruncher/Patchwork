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
import org.blockartistry.doodads.common.item.ItemMagicDevice;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public abstract class AbilityHandler extends IForgeRegistryEntry.Impl<AbilityHandler> {

	public static final IForgeRegistry<AbilityHandler> REGISTRY = new RegistryBuilder<AbilityHandler>()
			.setName(new ResourceLocation(ModInfo.MOD_ID, "deviceabilities")).allowModification()
			.setType(AbilityHandler.class).create();

	private String unlocalizedName;
	private int priority = 10000;

	public AbilityHandler(@Nonnull final String name) {
		this.setRegistryName(name);
		setUnlocalizedName(ModInfo.MOD_ID + ".deviceability." + name + ".name");
	}

	/**
	 * Indicates if the Ability can be applied to the type of device specified.
	 *
	 * @param type
	 *                 The type of device being crafted/manipulated
	 * @return true if it can be applied, false otherwise
	 */
	public boolean canBeAppliedTo(@Nonnull final ItemMagicDevice.Type type) {
		return true;
	}

	/**
	 * Unlocalized name of the ability. Need to be translated via Localization.
	 *
	 * @return Unlocalized name of the Ability
	 */
	@Nullable
	public String getUnlocalizedName() {
		return this.unlocalizedName;
	}

	@Nonnull
	protected AbilityHandler setUnlocalizedName(@Nonnull final String name) {
		this.unlocalizedName = name;
		return this;
	}

	/**
	 * Registers the Ability with the system so that it can be referenced.
	 *
	 * @return An instance of the Ability.
	 */
	@Nonnull
	public AbilityHandler register() {
		REGISTRY.register(this);
		return this;
	}

	/**
	 * The priority of the Ability as compared to other abilities. The greater the
	 * value the higher the priority.
	 *
	 * @return
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * Sets the priority of the Ability
	 *
	 * @param p
	 *              Priority to set
	 * @return Reference to the AbilityHandler useful for further configuration
	 */
	public AbilityHandler setPriority(final int p) {
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

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public void onItemUse(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player,
			@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final EnumHand hand,
			@Nonnull final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	public void onItemRightClick(@Nonnull final ItemStack stack, @Nonnull final World worldIn,
			@Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand handIn) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when an entity is hit with the item
	 *
	 * @param entity
	 * @param player
	 */
	public boolean hitEntity(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase target,
			@Nonnull final EntityLivingBase attacker) {
		// Override in a sub-class to provide functionality
		return false;
	}
}
