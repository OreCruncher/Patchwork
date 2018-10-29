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

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.common.item.magic.DeviceQuality;
import net.minecraft.util.ResourceLocation;

public interface IMagicDeviceSettable extends IMagicDevice {

	void setDeviceType(@Nonnull final ItemMagicDevice.Type type);

	void addAbilities(@Nonnull final ResourceLocation... ability);

	void setMaxEnergy(final int energy);

	void setCurrentEnergy(final int energy);

	void setQuality(@Nonnull final DeviceQuality q);

	void setMoniker(@Nonnull final String moniker);

	/**
	 * Consumes the specified amount of energy from the device
	 * 
	 * @param amount
	 *                   The amount of energy to be consumed
	 * @return true if the operation completed; false if there isn't enough energy
	 */
	boolean consumeEnergy(final int amount);
	
	boolean isDirty();

	void clearDirty();
}
