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

import org.blockartistry.doodads.common.item.magic.DeviceQuality;
import org.blockartistry.doodads.common.item.magic.MagicDeviceType;
import org.blockartistry.doodads.util.MathStuff;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class MagicDeviceData implements IMagicDeviceSettable {

	protected MagicDeviceType type = MagicDeviceType.INERT;
	protected List<String> abilities = new ArrayList<>();
	protected int maxEnergy = 120 * 60 * 20;
	protected int currentEnergy = this.maxEnergy;
	protected DeviceQuality quality = DeviceQuality.MUNDANE;
	protected String moniker = "";

	protected boolean dirty;

	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	@Nonnull
	public MagicDeviceType getDeviceType() {
		return this.type;
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
	@Nonnull
	public DeviceQuality getQuality() {
		return this.quality;
	}

	@Override
	@Nonnull
	public String getMoniker() {
		return this.moniker;
	}

	@Override
	public void setDeviceType(@Nonnull final MagicDeviceType type) {
		this.type = type;
		this.dirty = true;
	}

	@Override
	public void addAbilities(@Nonnull final ResourceLocation... ability) {
		for (final ResourceLocation r : ability)
			this.abilities.add(r.toString());
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
	public void setQuality(@Nonnull final DeviceQuality q) {
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
		nbt.setString(NBT.TYPE, this.type.name());
		nbt.setString(NBT.QUALITY, this.quality.name());
		nbt.setInteger(NBT.MAX_ENERGY, this.maxEnergy);
		nbt.setInteger(NBT.CURRENT_ENERGY, this.currentEnergy);
		nbt.setString(NBT.MONIKER, this.moniker);

		final NBTTagList theList = new NBTTagList();
		for (final String s : this.abilities)
			theList.appendTag(new NBTTagString(s));
		nbt.setTag(NBT.ABILITIES, theList);

		this.dirty = false;
		return nbt;
	}

	@Override
	public void deserialize(@Nonnull final NBTTagCompound nbt) {
		this.type = MagicDeviceType.valueOf(nbt.getString(NBT.TYPE));
		this.quality = DeviceQuality.valueOf(nbt.getString(NBT.QUALITY));
		this.maxEnergy = nbt.getInteger(NBT.MAX_ENERGY);
		this.currentEnergy = nbt.getInteger(NBT.CURRENT_ENERGY);
		this.moniker = nbt.getString(NBT.MONIKER);

		this.abilities = new ArrayList<>();
		final NBTTagList theList = nbt.getTagList(NBT.ABILITIES, 8);
		final int count = theList.tagCount();
		for (int i = 0; i < count; i++)
			this.abilities.add(theList.getStringTagAt(i));

		this.dirty = false;
	}

	private static class NBT {
		public static final String TYPE = "t";
		public static final String ABILITIES = "a";
		public static final String MAX_ENERGY = "m";
		public static final String CURRENT_ENERGY = "c";
		public static final String QUALITY = "q";
		public static final String MONIKER = "n";
	}

}
