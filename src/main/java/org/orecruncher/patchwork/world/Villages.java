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
package org.orecruncher.patchwork.world;

import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModOptions;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class Villages {

	public static void initialize() {
		if (ModOptions.villages.additionalBiomes.length > 0) {
			for (final String id : ModOptions.villages.additionalBiomes) {
				final ResourceLocation loc = new ResourceLocation(id);
				final Biome b = ForgeRegistries.BIOMES.getValue(loc);
				if (b != null)
					MapGenVillage.VILLAGE_SPAWN_BIOMES.add(b);
				else
					ModBase.log().warn("Unknown biome for village spawns [%s]", id);
			}
		}

		if (ModOptions.logging.enableLogging) {
			ModBase.log().info("Village Spawn Biomes");
			ModBase.log().info("====================");
			for (final Biome b : MapGenVillage.VILLAGE_SPAWN_BIOMES)
				ModBase.log().info("%s (%s)", b.getBiomeName(), b.getRegistryName());
		}
	}
}
