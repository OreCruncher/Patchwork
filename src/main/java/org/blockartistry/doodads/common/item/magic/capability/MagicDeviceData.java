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

package org.blockartistry.doodads.common.item.magic.capability;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.common.item.magic.AbilityHandler;
import org.blockartistry.doodads.util.MathStuff;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class MagicDeviceData implements IMagicDeviceSettable {

	protected List<String> abilities = new ArrayList<>();
	protected int variant = 0;
	protected int maxEnergy = 0;
	protected int currentEnergy = 0;
	protected ItemMagicDevice.Quality quality = ItemMagicDevice.Quality.PLAIN;
	protected String moniker = "";

	protected boolean dirty;

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	public void clearDirty() {
		this.dirty = false;
	}

	@Override
	public int getVariant() {
		return this.variant;
	}

	@Override
	@Nonnull
	public List<String> getAbilities() {
		return this.abilities;
	}

	@Override
	public int getMaxEnergy() {
		return this.maxEnergy;
	}

	@Override
	public int getCurrentEnergy() {
		return this.currentEnergy;
	}

	@Override
	public float getPowerRatio() {
		if (this.maxEnergy == 0)
			return 0;
		return ((float) this.currentEnergy / this.maxEnergy) * 100F;
	}

	@Override
	@Nonnull
	public ItemMagicDevice.Quality getQuality() {
		return this.quality;
	}

	@Override
	@Nonnull
	public String getMoniker() {
		return this.moniker;
	}

	@Override
	public void setVariant(final int v) {
		this.variant = v;
		this.dirty = true;
	}

	@Override
	public void addAbilities(@Nonnull final ResourceLocation... ability) {
		for (final ResourceLocation r : ability)
			this.abilities.add(r.toString());
		sortAbilities();
		this.dirty = true;
	}

	@Override
	public void setMaxEnergy(final int energy) {
		this.maxEnergy = MathStuff.clamp(energy, 0, Integer.MAX_VALUE);
		setCurrentEnergy(this.currentEnergy);
		this.dirty = true;
	}

	@Override
	public void setCurrentEnergy(final int energy) {
		this.currentEnergy = MathStuff.clamp(energy, 0, this.maxEnergy);
		this.dirty = true;
	}

	@Override
	public void setQuality(@Nonnull final ItemMagicDevice.Quality q) {
		this.quality = q;
		this.dirty = true;
	}

	@Override
	public void setMoniker(@Nonnull final String moniker) {
		this.moniker = moniker;
		this.dirty = true;
	}

	@Override
	public boolean hasEnergyFor(final int amt) {
		return amt <= this.currentEnergy;
	}

	@Override
	public boolean consumeEnergy(final int amount) {
		if (amount > this.currentEnergy)
			return false;
		this.currentEnergy -= amount;
		this.dirty = true;
		return true;
	}

	@Override
	@Nonnull
	public NBTTagCompound serialize() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte(NBT.VARIANT, (byte) this.variant);
		nbt.setByte(NBT.QUALITY, (byte) this.quality.ordinal());
		nbt.setInteger(NBT.MAX_ENERGY, this.maxEnergy);
		nbt.setInteger(NBT.CURRENT_ENERGY, this.currentEnergy);
		nbt.setString(NBT.MONIKER, this.moniker);

		final NBTTagList theList = new NBTTagList();
		for (final String s : this.abilities)
			theList.appendTag(new NBTTagString(s));
		nbt.setTag(NBT.ABILITIES, theList);
		return nbt;
	}

	@Override
	public void deserialize(@Nonnull final NBTTagCompound nbt) {
		this.variant = nbt.getByte(NBT.VARIANT);
		this.quality = ItemMagicDevice.Quality.values()[nbt.getByte(NBT.QUALITY)];
		this.maxEnergy = nbt.getInteger(NBT.MAX_ENERGY);
		this.currentEnergy = nbt.getInteger(NBT.CURRENT_ENERGY);
		this.moniker = nbt.getString(NBT.MONIKER);

		this.abilities = new ArrayList<>();
		final NBTTagList theList = nbt.getTagList(NBT.ABILITIES, 8);
		final int count = theList.tagCount();
		for (int i = 0; i < count; i++)
			this.abilities.add(theList.getStringTagAt(i));
		sortAbilities();
		this.dirty = true;
	}

	private void sortAbilities() {
		// Sort the abilities based on priority
		this.abilities.sort((s1, s2) -> {
			final AbilityHandler h1 = AbilityHandler.REGISTRY.getValue(new ResourceLocation(s1));
			final AbilityHandler h2 = AbilityHandler.REGISTRY.getValue(new ResourceLocation(s2));
			return h1.getPriority() - h2.getPriority();
		});
	}

	private static class NBT {
		public static final String VARIANT = "v";
		public static final String ABILITIES = "a";
		public static final String MAX_ENERGY = "m";
		public static final String CURRENT_ENERGY = "c";
		public static final String QUALITY = "q";
		public static final String MONIKER = "n";
	}

}
