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

package org.orecruncher.patchwork.common.item.magic.capability;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.common.item.ItemMagicDevice;
import org.orecruncher.patchwork.common.item.magic.AbilityHandler;
import org.orecruncher.patchwork.util.MathStuff;
import org.orecruncher.patchwork.util.collections.EmptyList;
import org.orecruncher.patchwork.util.simpledata.SimpleDataRegistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class MagicDeviceData extends SimpleDataRegistry implements IMagicDeviceSettable {

	private static final Comparator<ResourceLocation> ABILITY_SORT = (r1, r2) -> {
		final AbilityHandler h1 = AbilityHandler.REGISTRY.getValue(r1);
		final AbilityHandler h2 = AbilityHandler.REGISTRY.getValue(r2);
		return h1.getPriority() - h2.getPriority();
	};

	protected List<ResourceLocation> abilities = null;
	protected int variant = 0;
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
		return this.quality.getMaxPower();
	}

	@Override
	public int getCurrentEnergy() {
		return this.currentEnergy;
	}

	@Override
	public float getPowerRatio() {
		return ((float) this.currentEnergy / getMaxEnergy()) * 100F;
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
	public void setCurrentEnergy(final int energy) {
		this.currentEnergy = MathStuff.clamp(energy, 0, getMaxEnergy());
		setDirty();
	}

	@Override
	public void addCurrentEnergy(final int energy) {
		this.currentEnergy = MathStuff.clamp(this.currentEnergy + energy, 0, getMaxEnergy());
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
		nbt.setByte(NBT.VARIANT, (byte) getVariant());
		nbt.setByte(NBT.QUALITY, (byte) getQuality().ordinal());
		nbt.setInteger(NBT.CURRENT_ENERGY, getCurrentEnergy());
		if (getMoniker() != null)
			nbt.setString(NBT.MONIKER, getMoniker());

		final List<ResourceLocation> a = getAbilities();
		if (a.size() > 0) {
			final NBTTagList theList = new NBTTagList();
			for (final ResourceLocation r : a)
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
		setVariant(nbt.getByte(NBT.VARIANT));
		setQuality(ItemMagicDevice.Quality.values()[nbt.getByte(NBT.QUALITY)]);
		setCurrentEnergy(nbt.getInteger(NBT.CURRENT_ENERGY));
		if (nbt.hasKey(NBT.MONIKER))
			setMoniker(nbt.getString(NBT.MONIKER));

		if (nbt.hasKey(NBT.ABILITIES)) {
			final NBTTagList theList = nbt.getTagList(NBT.ABILITIES, 8);
			if (!theList.hasNoTags()) {
				this.abilities = new ArrayList<>(nbt.getSize());
				final ResourceLocation[] handlers = StreamSupport.stream(theList.spliterator(), false)
						.map(e -> new ResourceLocation(((NBTTagString) e).getString()))
						.toArray(ResourceLocation[]::new);
				addAbilities(handlers);
			}
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

	private static class NBT {
		public static final String VARIANT = "v";
		public static final String ABILITIES = "a";
		public static final String CURRENT_ENERGY = "c";
		public static final String QUALITY = "q";
		public static final String MONIKER = "n";
		public static final String DATA = "d";
	}

}
