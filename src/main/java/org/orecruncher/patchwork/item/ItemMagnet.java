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

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.client.ModCreativeTab;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagnet extends ItemBase implements IBauble {

	private static final double RANGE = 6D;
	private static final double SPEED = 0.035D;

	public ItemMagnet() {
		super("magnet");
		setCreativeTab(ModCreativeTab.tab);
		setMaxStackSize(1);
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.add(new ItemStack(this));
		}
	}

	@Override
	public boolean isBookEnchantable(@Nonnull final ItemStack stack, @Nonnull final ItemStack book) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(@Nonnull final ItemStack stack) {
		return isOn(stack);
	}

	@Override
	@Nonnull
	public BaubleType getBaubleType(@Nonnull final ItemStack itemstack) {
		return BaubleType.TRINKET;
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(@Nonnull final World world, @Nonnull final EntityPlayer player,
			@Nonnull final EnumHand hand) {
		if (!world.isRemote) {
			toggleState(player.getHeldItem(hand));
			player.swingArm(hand);
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public void onWornTick(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase entity) {
		final EntityPlayer player = (EntityPlayer) entity;
		if (player.getEntityWorld().isRemote || !isOn(stack))
			return;

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
		}
	}

	private boolean isOn(@Nonnull final ItemStack stack) {
		return stack.getOrCreateSubCompound(NBT.KEY).hasKey(NBT.IS_ON);
	}

	private void toggleState(@Nonnull final ItemStack stack) {
		if (isOn(stack)) {
			stack.getOrCreateSubCompound(NBT.KEY).removeTag(NBT.IS_ON);
		} else {
			stack.getOrCreateSubCompound(NBT.KEY).setBoolean(NBT.IS_ON, true);
		}
	}

	private static class NBT {
		public static final String KEY = ModInfo.MOD_ID + "_magnet";
		public static final String IS_ON = "magnet_on";
	}

}
