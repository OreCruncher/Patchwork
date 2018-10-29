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

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.doodads.common.item.ItemMagicDevice;
import org.blockartistry.doodads.common.item.magic.DeviceQuality;
import org.blockartistry.doodads.util.INBTSerialization;

import net.minecraft.nbt.NBTTagCompound;

public interface IMagicDevice extends INBTSerialization<NBTTagCompound> {

	/**
	 * Returns the type of device
	 * 
	 * @return The type of device this guy represents
	 */
	@Nonnull
	ItemMagicDevice.Type getDeviceType();

	/**
	 * Returns a list of the devices abilities
	 * 
	 * @return A list of the devices abilities
	 */
	@Nonnull
	List<String> getAbilities();

	/**
	 * Returns the maximum possible energy for the device
	 * 
	 * @return Maximum possible energy that can be stored in the device
	 */
	int getMaxEnergy();

	/**
	 * Returns the current amount of energy available in the device
	 * 
	 * @return Current amount of energy available in the device
	 */
	int getCurrentEnergy();

	/**
	 * Returns the current amount of energy available as a ratio
	 * 0 - 100.  Think of it as percentage of power remaining.
	 * @return
	 */
	float getPowerRatio();

	/**
	 * Returns quality information about the device
	 * 
	 * @return Device quality information
	 */
	@Nonnull
	DeviceQuality getQuality();

	/**
	 * The fancy name of the device (i.e. "Ralph's Wonderous Ring")
	 * 
	 * @return
	 */
	@Nonnull
	String getMoniker();

	/**
	 * Detemines if the device has enough energy to meet the request.
	 * 
	 * @param amt
	 *                The amount to be requested
	 * @return true if there is enough energy; false otherwise
	 */
	boolean hasEnergyFor(final int amt);

}
