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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.common.item.magic.AbilityHandler;
import org.blockartistry.doodads.util.MathStuff;
import org.blockartistry.doodads.util.collections.EmptyList;
import org.blockartistry.doodads.util.simplecaps.SimpleDataRegistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class MagicDeviceData extends SimpleDataRegistry implements IMagicDeviceSettable {

	protected List<ResourceLocation> abilities = null;
	protected int variant = 0;
	protected int maxEnergy = 0;
	protected int currentEnergy = 0;
	protected ItemMagicDevice.Quality quality = ItemMagicDevice.Quality.PLAIN;
	protected String moniker = null;

	@Override
	public int getVariant() {
		return this.variant;
	}

	@Override
	@Nonnull
	public List<ResourceLocation> getAbilities() {
		return this.abilities != null ? this.abilities : EmptyList.empty();
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
		return Objects.toString(this.moniker, "");
	}

	@Override
	public void setVariant(final int v) {
		this.variant = v;
		setDirty();
	}

	@Override
	public void addAbilities(@Nonnull final ResourceLocation... ability) {
		if (ability != null && ability.length > 0) {
			if (this.abilities == null)
				this.abilities = new ArrayList<>(4);
			for (final ResourceLocation r : ability)
				this.abilities.add(r);
			sort(this.abilities);
			setDirty();
		}
	}

	@Override
	public void setMaxEnergy(final int energy) {
		this.maxEnergy = MathStuff.clamp(energy, 0, Integer.MAX_VALUE);
		setCurrentEnergy(this.currentEnergy);
		setDirty();
	}

	@Override
	public void setCurrentEnergy(final int energy) {
		this.currentEnergy = MathStuff.clamp(energy, 0, this.maxEnergy);
		setDirty();
	}

	@Override
	public void setQuality(@Nonnull final ItemMagicDevice.Quality q) {
		this.quality = q;
		setDirty();
	}

	@Override
	public void setMoniker(@Nonnull final String moniker) {
		this.moniker = moniker;
		setDirty();
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
		setDirty();
		return true;
	}

	@Override
	@Nonnull
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte(NBT.VARIANT, (byte) this.variant);
		nbt.setByte(NBT.QUALITY, (byte) this.quality.ordinal());
		nbt.setInteger(NBT.MAX_ENERGY, this.maxEnergy);
		nbt.setInteger(NBT.CURRENT_ENERGY, this.currentEnergy);
		if (this.moniker != null)
			nbt.setString(NBT.MONIKER, this.moniker);

		if (this.abilities != null && this.abilities.size() > 0) {
			final NBTTagList theList = new NBTTagList();
			for (final ResourceLocation r : this.abilities)
				theList.appendTag(new NBTTagString(r.toString()));
			nbt.setTag(NBT.ABILITIES, theList);
		}

		// Serialize base!
		final NBTTagCompound base = super.serializeNBT();
		if (base != null && !base.hasNoTags())
			nbt.setTag(NBT.DATA, base);

		return nbt;
	}

	@Override
	public void deserializeNBT(@Nonnull final NBTTagCompound nbt) {
		this.variant = nbt.getByte(NBT.VARIANT);
		this.quality = ItemMagicDevice.Quality.values()[nbt.getByte(NBT.QUALITY)];
		this.maxEnergy = nbt.getInteger(NBT.MAX_ENERGY);
		this.currentEnergy = nbt.getInteger(NBT.CURRENT_ENERGY);
		if (nbt.hasKey(NBT.MONIKER))
			this.moniker = nbt.getString(NBT.MONIKER);

		if (nbt.hasKey(NBT.ABILITIES)) {
			final NBTTagList theList = nbt.getTagList(NBT.ABILITIES, 8);
			final int count = theList.tagCount();
			this.abilities = new ArrayList<>(count);
			for (int i = 0; i < count; i++)
				this.abilities.add(new ResourceLocation(theList.getStringTagAt(i)));
			sort(this.abilities);
		}

		// Deserialize base!
		if (nbt.hasKey(NBT.DATA))
			super.deserializeNBT(nbt.getCompoundTag(NBT.DATA));

		setDirty();
	}

	private static final void sort(@Nonnull final List<ResourceLocation> theList) {
		if (theList != null && theList.size() > 1)
			theList.sort(ABILITY_SORT);
	}

	private static final Comparator<ResourceLocation> ABILITY_SORT = (r1, r2) -> {
		final AbilityHandler h1 = AbilityHandler.REGISTRY.getValue(r1);
		final AbilityHandler h2 = AbilityHandler.REGISTRY.getValue(r2);
		return h1.getPriority() - h2.getPriority();
	};

	private static class NBT {
		public static final String VARIANT = "v";
		public static final String ABILITIES = "a";
		public static final String MAX_ENERGY = "m";
		public static final String CURRENT_ENERGY = "c";
		public static final String QUALITY = "q";
		public static final String MONIKER = "n";
		public static final String DATA = "d";
	}

}
