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
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.ModInfo;
import org.blockartistry.doodads.util.Localization;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class MagicDeviceHelper {

	private static final String LOC_DEVICEABILITY_FMT = "doodads.deviceability.format";
	private static final String FORMAT_STRING = Localization.loadString(LOC_DEVICEABILITY_FMT);
	private static final String NAMESPACE = ModInfo.MOD_ID + ":namespace";
	private static final String ABILITIES = "abilities";

	private MagicDeviceHelper() {
	}

	@Nonnull
	public static ItemStack addDeviceAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		if (DeviceAbility.REGISTRY.containsKey(ability)) {
			final NBTTagCompound nbt = stack.getOrCreateSubCompound(NAMESPACE);
			NBTTagList theList = null;
			if (nbt.hasKey(ABILITIES))
				theList = nbt.getTagList(ABILITIES, 8);
			else
				nbt.setTag(ABILITIES, theList = new NBTTagList());
			theList.appendTag(new NBTTagString(ability.toString()));
		}
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Nonnull
	public static void gatherToolTips(@Nonnull final ItemStack stack, @Nonnull final List<String> tips) {
		process(stack, da -> tips.add(String.format(FORMAT_STRING, Localization.loadString(da.getUnlocalizedName()))));
	}

	public static void process(@Nonnull final ItemStack stack, @Nonnull final Consumer<DeviceAbility> op) {
		getAbilities(stack).stream().map(s -> DeviceAbility.REGISTRY.getValue(new ResourceLocation(s)))
				.filter(e -> e != null).forEach(op);
	}

	@Nonnull
	public static List<String> getAbilities(@Nonnull final ItemStack stack) {
		final List<String> result = new ArrayList<>();
		final NBTTagList nbt = stack.getOrCreateSubCompound(NAMESPACE).getTagList(ABILITIES, 8); // String
		final int count = nbt.tagCount();
		for (int i = 0; i < count; i++)
			result.add(nbt.getStringTagAt(i));
		return result;
	}

}
