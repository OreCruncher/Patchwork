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

package org.orecruncher.patchwork.common.item.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;
import org.orecruncher.lib.Localization;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.common.item.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.ModItems;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDevice;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public abstract class AbilityHandler extends IForgeRegistryEntry.Impl<AbilityHandler> {

	public static final IForgeRegistry<AbilityHandler> REGISTRY = new RegistryBuilder<AbilityHandler>()
			.setName(new ResourceLocation(ModInfo.MOD_ID, "deviceabilities")).allowModification()
			.setType(AbilityHandler.class).create();

	protected static final SoundEvent NO_CHARGE = SoundEvent.REGISTRY
			.getObject(new ResourceLocation("ui.button.click"));

	private static final int TOOLTIP_WIDTH = 40;

	private final String name;
	private final String[] tipText;

	private int priority = 10000;

	public AbilityHandler(@Nonnull final String name) {
		this.setRegistryName(name);
		this.name = Localization.loadString(ModInfo.MOD_ID + ".deviceability." + name + ".name");
		this.tipText = WordUtils.wrap(Localization.loadString(ModInfo.MOD_ID + ".deviceability." + name + ".tooltip"), TOOLTIP_WIDTH).split("\\r?\\n");
	}

	@Nonnull
	public String getName() {
		return this.name;
	}
	
	@SideOnly(Side.CLIENT)
	@Nullable
	public String[] getToolTip() {
		return this.tipText;
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
	 * Registers the Ability with the system so that it can be referenced.
	 *
	 * @return An instance of the Ability.
	 */
	@Nonnull
	public ResourceLocation register() {
		REGISTRY.register(this);
		return getRegistryName();
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
	public void equip(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase player, @Nonnull final ItemStack device) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when a player unequips an item from a slot
	 *
	 * @param player
	 * @param device
	 */
	public void unequip(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase player, @Nonnull final ItemStack device) {
		// Override to provide functionality
	}

	/**
	 * Called every tick that the item is worn
	 *
	 * @param player
	 * @param device
	 */
	public void doTick(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase player, @Nonnull final ItemStack device) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public void onItemUse(@Nonnull final IMagicDevice caps, @Nonnull final ItemStack stack, @Nonnull final EntityPlayer player,
			@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final EnumHand hand,
			@Nonnull final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	public void onItemRightClick(@Nonnull final IMagicDevice caps, @Nonnull final ItemStack stack, @Nonnull final World worldIn,
			@Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand handIn) {
		// Override in a sub-class to provide functionality
	}

	/**
	 * Called when an entity is hit with the item
	 *
	 * @param entity
	 * @param player
	 */
	public boolean hitEntity(@Nonnull final IMagicDevice caps, @Nonnull final ItemStack stack, @Nonnull final EntityLivingBase target,
			@Nonnull final EntityLivingBase attacker) {
		// Override in a sub-class to provide functionality
		return false;
	}

	/**
	 * Do an arm swing and perform a cooldown using the specified amount
	 */
	protected void setCooldown(@Nonnull final EntityPlayer player, final int cooldownTicks) {
		player.getCooldownTracker().setCooldown(ModItems.MAGIC_DEVICE, cooldownTicks);
	}
	
	/**
	 * Play a sound by the player indicating no charge available on the device
	 *
	 * @param player
	 */
	protected void playNoChargeSound(@Nonnull final EntityPlayer player) {
		playSoundAtPlayer(player, NO_CHARGE, 0.15F, 1.5F);
	}

	/**
	 * Play a sound at the player location. The player will also recieve the sound.
	 * Don't forget processing in an AbilityHandler is always on the server thread.
	 * 
	 * @param player
	 * @param event
	 * @param v
	 * @param p
	 */
	protected void playSoundAtPlayer(@Nonnull final EntityPlayer player, @Nonnull final SoundEvent event, final float v,
			final float p) {
		player.getEntityWorld().playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, event,
				SoundCategory.PLAYERS, v, p);

	}
}