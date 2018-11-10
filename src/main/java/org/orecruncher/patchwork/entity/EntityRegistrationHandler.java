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

package org.orecruncher.patchwork.entity;

import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModInfo;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public class EntityRegistrationHandler {

	private static int entityId = 0;
	
	//@formatter:off
    public static final Set<EntityEntry> SET_ENTITIES = ImmutableSet.of(
    	EntityEntryBuilder.create()
        	.entity(EntityMagicFireball.class)
            .id(new ResourceLocation(ModInfo.MOD_ID, "magic_fireball"), entityId++)
            .name("magic_fireball")
            .tracker(80, 3, true)
            .build(),
        EntityEntryBuilder.create()
        	.entity(EntityMagicMissile.class)
            .id(new ResourceLocation(ModInfo.MOD_ID, "magic_missile"), entityId++)
            .name("magic_missile")
            .tracker(80, 3, true)
            .build()
    );
    //@formatter:on

	@SubscribeEvent
	public static void registerEntities(final Register<EntityEntry> event) {
		final IForgeRegistry<EntityEntry> registry = event.getRegistry();

		for (final EntityEntry entityEntry : SET_ENTITIES)
			registry.register(entityEntry);
	}

	@SubscribeEvent
	public static void registerModels(@Nonnull final ModelRegistryEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicFireball.class,
				m -> new RenderMagic(m, 0.6f, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicMissile.class,
				m -> new RenderMagic(m, 0.6f, false));
	}

}
