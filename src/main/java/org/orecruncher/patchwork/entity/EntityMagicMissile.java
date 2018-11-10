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

import javax.annotation.Nonnull;

import org.orecruncher.patchwork.ModInfo;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityMagicMissile extends EntityMagic {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(ModInfo.MOD_ID,
			"textures/entities/missile.png");

	public EntityMagicMissile(@Nonnull final World world) {
		super(world);
	}

	public EntityMagicMissile(@Nonnull final EntityLivingBase caster) {
		this(caster.getEntityWorld(), caster);
	}

	public EntityMagicMissile(@Nonnull final World world, @Nonnull final EntityLivingBase caster) {
		super(world, caster);
		
		this.setNoGravity(false);
	}

	@Override
	@Nonnull
	public ResourceLocation getTexture() {
		return TEXTURE;
	}
	
	@Override
	protected float getVelocity() {
		return 1.2F;
	}

	@Override
	protected float getDamage() {
		return 4.0F;
	}
	
	@Override
	protected float getGravityVelocity() {
		return 0.015F;
	}

	@Override
	@Nonnull
	protected DamageSource getDamageSource() {
		return this.caster == null
				? (new EntityDamageSourceIndirect(moddedDamageSource("missile"), this, this)).setProjectile()
				: (new EntityDamageSourceIndirect(moddedDamageSource("missile"), this, this.caster)).setProjectile();
	}

	@Override
	protected void applyEffects(@Nonnull final RayTraceResult result) {
		// Intentionally left blank
	}

	@Override
	protected void applyBlockEffects(@Nonnull final RayTraceResult result) {
		// Intentionally left blank
	}

}
