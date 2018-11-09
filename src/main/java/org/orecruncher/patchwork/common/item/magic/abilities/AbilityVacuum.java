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

package org.orecruncher.patchwork.common.item.magic.abilities;

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.common.item.magic.AbilityHandler;
import org.orecruncher.patchwork.common.item.magic.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDeviceSettable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

public class AbilityVacuum extends AbilityHandler {

	private static final double SPEED = 0.035D;
	private static final int POWER_COST = ItemMagicDevice.BASE_CONSUMPTION_UNIT;
	private static final double RANGE = 6D;

	public AbilityVacuum() {
		super("vacuum");
		setPriority(1000);
	}

	@Override
	public boolean canBeAppliedTo(@Nonnull final ItemMagicDevice.Type type) {
		return type.getBaubleType() != null;
	}

	/**
	 * Called every tick that the item is worn
	 *
	 * @param player
	 * @param device
	 */
	@Override
	public void doTick(@Nonnull final IMagicDevice caps, @Nonnull final EntityLivingBase entity,
			@Nonnull final ItemStack device) {

		// Quick check ... no power no worky
		if (!caps.hasEnergyFor(POWER_COST))
			return;

		final EntityPlayerMP player = (EntityPlayerMP) entity;
		final IMagicDeviceSettable c = (IMagicDeviceSettable) caps;

		// Gather up targets in the area
		// The bounding box for the search area
		final AxisAlignedBB box = player.getEntityBoundingBox().grow(RANGE);

		final List<Entity> items = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box);
		items.addAll(player.getEntityWorld().getEntitiesWithinAABB(EntityXPOrb.class, box));
		items.sort((e1, e2) -> (int) (player.getDistanceSq(e1) - player.getDistanceSq(e2)));

		for (final Entity e : items) {

			final double x = player.posX + 0.5D - e.posX;
			final double y = player.posY + 1.0D - e.posY;
			final double z = player.posZ + 0.5D - e.posZ;
			final double distance = Math.sqrt(x * x + y * y + z * z);

			// The actual effect is in a radius (circle) so it is
			// possible that some items will not be affected because
			// the list query operated on cubic space. Since the list
			// is sorted so the closest is first as soon as we encounter
			// this condition we can break
			if (distance > RANGE)
				break;

			// If the item distance is close just have the player
			// suck it up. Otherwise, put the item in motion
			// toward the player.
			if (distance < 1.25D) {
				if (e instanceof EntityItem)
					((EntityItem) e).setNoPickupDelay();
				e.onCollideWithPlayer(player);
			} else {
				e.motionX += x / distance * SPEED;
				e.motionZ += z / distance * SPEED;
				if (y > 0.0D) {
					e.motionY = 0.12D;
				} else {
					e.motionY += y * SPEED;
				}
			}

			// Eat the energy for the privilege
			c.consumeEnergy(POWER_COST);

			// If we run out of energy break out
			if (!c.hasEnergyFor(POWER_COST))
				break;
		}
	}

}
