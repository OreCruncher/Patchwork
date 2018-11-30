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

package org.orecruncher.patchwork.item.ringofflight;

import javax.annotation.Nonnull;

import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.item.ItemRingOfFlight;

import net.minecraft.nbt.NBTTagCompound;

public class RingOfFlightData implements IRingOfFlightSettable {

	private boolean dirty;
	private int variant;
	private int currentDurability;

	@Override
	public ItemRingOfFlight.Variant getVariant() {
		return ItemRingOfFlight.Variant.bySubTypeId(this.variant);
	}

	@Override
	public void setVariant(@Nonnull final ItemRingOfFlight.Variant v) {
		if (this.variant != v.getSubTypeId()) {
			this.variant = v.getSubTypeId();
			this.dirty = true;
		}
	}

	@Override
	public int getRemainingDurability() {
		return this.currentDurability;
	}

	@Override
	public void setDurability(final int dmg) {
		if (this.currentDurability != dmg) {
			this.currentDurability = MathStuff.clamp(dmg, 0,
					ItemRingOfFlight.Variant.bySubTypeId(this.variant).getMaxDurability());
			this.dirty = true;
		}
	}

	@Override
	public float getDurabilityForDisplay() {
		final int max = ItemRingOfFlight.Variant.bySubTypeId(this.variant).getMaxDurability();
		return (float) (max - this.currentDurability) / (float) max;
	}

	@Override
	public boolean damage(final int amount) {
		if (amount > this.currentDurability)
			return false;
		this.currentDurability -= amount;
		this.dirty = true;
		return true;
	}

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	public void clearDirty() {
		this.dirty = false;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(NBT.VARIANT, this.variant);
		nbt.setInteger(NBT.CURRENT_DURABILITY, this.currentDurability);
		return nbt;
	}

	@Override
	public void deserializeNBT(@Nonnull final NBTTagCompound nbt) {
		this.variant = nbt.getInteger(NBT.VARIANT);
		this.currentDurability = MathStuff.clamp(nbt.getInteger(NBT.CURRENT_DURABILITY), 0,
				getVariant().getMaxDurability());
		this.dirty = true;
	}

	private static class NBT {
		public static final String VARIANT = "v";
		public static final String CURRENT_DURABILITY = "cd";
	}

}
