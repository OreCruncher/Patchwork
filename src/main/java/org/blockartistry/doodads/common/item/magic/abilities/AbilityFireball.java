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

package org.blockartistry.doodads.common.item.magic.abilities;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.common.item.ModItems;
import org.blockartistry.doodads.common.item.magic.AbilityHandler;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDevice;
import org.blockartistry.doodads.common.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AbilityFireball extends AbilityHandler {

	// Figure 20 shots with a normal wand
	private static final int ACTIVATION_COST = ItemMagicDevice.Quality.NORMAL.getMaxPower() / 20;
	private static final int COOLDOWN_TICKS = 100;

	public AbilityFireball() {
		super("fireball");
	}

	@Override
	public boolean canBeAppliedTo(@Nonnull final ItemMagicDevice.Type type) {
		return type == ItemMagicDevice.Type.WAND || type == ItemMagicDevice.Type.ROD
				|| type == ItemMagicDevice.Type.SCROLL;
	}

	@Override
	public void onItemRightClick(@Nonnull final IMagicDevice caps, @Nonnull final ItemStack stack,
			@Nonnull final World world, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
		// There is a cooldown on this item
		if (player.getCooldownTracker().hasCooldown(ModItems.MAGIC_DEVICE))
			return;

		boolean success = false;

		final IMagicDeviceSettable c = (IMagicDeviceSettable) caps;
		if (c.consumeEnergy(ACTIVATION_COST)) {
			// Calculate an acceleration vector based on the player look vector
			final Vec3d lookVec = player.getLookVec();
			final double dX = lookVec.x;
			final double dY = lookVec.y;
			final double dZ = lookVec.z;

			// Create the entity using this CTOR. It sets up the player as the shooter
			final EntitySmallFireball fireball = new EntitySmallFireball(world, player, dX, dY, dZ);

			// Adjust its position so it is not at the players feet :\
			final double x = player.posX + lookVec.x;
			final double y = player.posY + player.getEyeHeight() + lookVec.y;
			final double z = player.posZ + lookVec.z;
			fireball.setPosition(x, y, z);

			// Recalculate the acceleration vectors because the entity CTOR puts a lot of
			// English on the trajectory
			final double d0 = MathHelper.sqrt(dX * dX + dY * dY + dZ * dZ);
			fireball.accelerationX = dX / d0 * 0.1D;
			fireball.accelerationY = dY / d0 * 0.1D;
			fireball.accelerationZ = dZ / d0 * 0.1D;

			// Spawn the sucker
			world.spawnEntity(fireball);

			// Play the sound
			final BlockPos soundPos = new BlockPos(player);
			player.world.playEvent((EntityPlayer) null, 1018, soundPos, 0);

			success = true;

		} else {
			// Need to play a click sound because they are empty
			playNoChargeSound(player);
		}

		setCooldown(player, success ? COOLDOWN_TICKS : 20);
	}

}
