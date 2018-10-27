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

package org.blockartistry.doodads.world.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.Doodads;
import org.blockartistry.doodads.common.loot.Loot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = Doodads.MOD_ID)
public class CoinDrop {

	private static enum Quality {
		//
		NONE(""),
		//
		SPAWNER("spawner"),
		//
		PASSIVE("passive"),
		//
		SMALL("small"),
		//
		MEDIUM("medium"),
		//
		LARGE("large"),
		//
		LEGENDARY("legendary");

		private final String poolName;

		private Quality(@Nonnull final String pool) {
			this.poolName = pool;
		}

		@Nonnull
		public String getPool() {
			return this.poolName;
		}
	}

	private static final Random RAND = new Random();

	private static Quality assess(@Nonnull final Entity entity) {
		if (entity instanceof EntityItem)
			return Quality.NONE;
		if (entity instanceof EntityAnimal)
			return Quality.PASSIVE;
		if (entity instanceof EntityWither || entity instanceof EntityDragon)
			return Quality.LEGENDARY;
		if (entity instanceof AbstractIllager)
			return Quality.LARGE;
		if (entity instanceof EntityWitch || entity instanceof EntityEnderman)
			return Quality.MEDIUM;
		return Quality.SMALL;
	}

	@SubscribeEvent(receiveCanceled = false)
	public static void onLivingDrops(@Nonnull final LivingDropsEvent event) {
		final EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
		if (player != null && !(player instanceof FakePlayer)) {
			final Entity entity = event.getEntity();
			final Quality q = assess(entity);
			if (q != Quality.NONE) {
				final World world = entity.getEntityWorld();
				final LootPool pool = Loot.getPool(world, Loot.COIN_LOOT_TABLE, q.getPool());
				if (pool != null) {
					final LootContext ctx = new LootContext.Builder((WorldServer) world).withLootedEntity(entity)
							.build();
					final List<ItemStack> drops = new ArrayList<>();
					pool.generateLoot(drops, RAND, ctx);
					for (final ItemStack stack : drops) {
						final EntityItem item = new EntityItem(world, entity.posX, entity.posY, entity.posZ, stack);
						event.getDrops().add(item);
					}
				}
			}
		}
	}

}
