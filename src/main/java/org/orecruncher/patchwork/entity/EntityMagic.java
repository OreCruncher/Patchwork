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

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.ModInfo;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityMagic extends Entity implements IProjectile, IEntityAdditionalSpawnData {

	// Used to calculate hand location based on player rotation
	private static final float DISTANCE_TO_CENTER = 0.2F;

	protected EntityLivingBase caster;
	private String casterName;
	private int age;
	private final int maxAge = 20 * 3; // 3 seconds
	private float drag = 0.99F;

	// Used to prevent the entity from attacking the caster when it is first
	// launched
	public Entity ignoreEntity;
	private int ignoreTime;

	public EntityMagic(@Nonnull final World world) {
		super(world);
		setSize(0.25F, 0.25F);
	}

	public EntityMagic(@Nonnull final World world, final double x, final double y, final double z) {
		this(world);
		setPosition(x, y, z);
	}

	public EntityMagic(@Nonnull final World world, @Nonnull final EntityLivingBase caster) {
		this(world);
		this.caster = caster;

		// Calculate the position. We want it toward the wielding hand right around
		// eyesight level. Want to make it look like it came from the item.
		final float rotDegrees = MathStuff.wrapDegrees(this.caster.rotationYaw);
		final double rot = MathStuff.toRadians(rotDegrees);
		final float offset = isRightHand() ? -DISTANCE_TO_CENTER : DISTANCE_TO_CENTER;

		final double x = this.caster.posX + MathStuff.cos(rot) * offset;
		final double z = this.caster.posZ + MathStuff.sin(rot) * offset;
		final double y = this.caster.posY + this.caster.getEyeHeight() - 0.10000000149011612D;

		this.setPosition(x, y, z);

		// Calculate an acceleration vector based on the player look vector. The
		// look vector is a unit vector so it makes life easier.
		final Vec3d lookVec = this.caster.getLookVec();
		final double dX = lookVec.x;
		final double dY = lookVec.y;
		final double dZ = lookVec.z;

		// Acceleration along the look vector moving pretty fast and high accuracy
		this.shoot(dX, dY, dZ, getVelocity(), getAccuracy());

		// Magic effect typically do not suffer from gravity. Turn back on
		// if needed otherwise.
		setNoGravity(true);

		// Same with drag. To increase drag decrease the value. 1F means no
		// drag.
		this.drag = 1F;
	}

	protected float getVelocity() {
		return 1F;
	}
	
	protected float getAccuracy() {
		return 0F;
	}
	
	private boolean isRightHand() {
		return this.caster.getPrimaryHand() == EnumHandSide.RIGHT;
	}

	@Override
	protected void entityInit() {
		// Do nothing
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	/**
	 * Sets throwable heading based on an entity that's throwing it
	 */
	public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset,
			float velocity, float inaccuracy) {
		final float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
		final float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
		final float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
		this.shoot(f, f1, f2, velocity, inaccuracy);
		this.motionX += entityThrower.motionX;
		this.motionZ += entityThrower.motionZ;

		if (!entityThrower.onGround) {
			this.motionY += entityThrower.motionY;
		}
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
	 * direction.
	 */
	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		final float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / f;
		y = y / f;
		z = z / f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		x = x * velocity;
		y = y * velocity;
		z = z * velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		final float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float) (MathHelper.atan2(y, f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
	}

	/**
	 * Updates the entity motion clientside, called by packets from the server
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			final float f = MathHelper.sqrt(x * x + z * z);
			this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
			this.rotationPitch = (float) (MathHelper.atan2(y, f) * (180D / Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		super.onUpdate();

		++this.age;

		if (this.age > this.maxAge) {
			setDead();
			return;
		}

		Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1);
		vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		if (raytraceresult != null) {
			vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
		}

		Entity entity = null;
		final List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this,
				getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
		double d0 = 0.0D;
		boolean flag = false;

		for (int i = 0; i < list.size(); ++i) {
			final Entity entity1 = list.get(i);

			if (entity1.canBeCollidedWith()) {
				if (entity1 == this.ignoreEntity) {
					flag = true;
				} else if (this.caster != null && this.ticksExisted < 2 && this.ignoreEntity == null) {
					this.ignoreEntity = entity1;
					flag = true;
				} else {
					flag = false;
					final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
					final RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);

					if (raytraceresult1 != null) {
						final double d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}
		}

		if (this.ignoreEntity != null) {
			if (flag) {
				this.ignoreTime = 2;
			} else if (this.ignoreTime-- <= 0) {
				this.ignoreEntity = null;
			}
		}

		if (entity != null) {
			raytraceresult = new RayTraceResult(entity);
		}

		if (raytraceresult != null) {
			if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK
					&& this.world.getBlockState(raytraceresult.getBlockPos()).getBlock() == Blocks.PORTAL) {
				setPortal(raytraceresult.getBlockPos());
			} else if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
				onImpact(raytraceresult);
			}
		}

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		final float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

		for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * (180D / Math.PI)); this.rotationPitch
				- this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			;
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
		float f1 = this.drag;

		if (isInWater()) {
			for (int j = 0; j < 4; ++j) {
				final float f3 = 0.25F;
				this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f3,
						this.posY - this.motionY * f3, this.posZ - this.motionZ * f3, this.motionX, this.motionY,
						this.motionZ);
			}

			f1 = 0.8F;
		}

		this.motionX *= f1;
		this.motionY *= f1;
		this.motionZ *= f1;

		if (!hasNoGravity()) {
			this.motionY -= getGravityVelocity();
		}

		setPosition(this.posX, this.posY, this.posZ);
		if (isEntityAlive())
			doParticleEffect();
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	protected float getGravityVelocity() {
		return 0.03F;
	}

	protected void doParticleEffect() {
		// Override to provide particle effect while traveling
	}

	protected void onImpact(@Nonnull final RayTraceResult result) {
		if (!this.world.isRemote) {
			if (result.entityHit != null) {
				if (!isImmune(result)) {
					final boolean flag = result.entityHit.attackEntityFrom(getDamageSource(), getDamage());

					if (flag) {
						applyEnchantments(this.caster, result.entityHit);
						applyEffects(result);
					}
				}
			} else {
				boolean flag1 = true;

				if (this.caster != null && this.caster instanceof EntityLiving) {
					flag1 = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.caster);
				}

				if (flag1) {
					applyBlockEffects(result);
				}
			}

			setDead();
		}
	}

	/*
	 * Called to check to see if the target is immune to the magic effect
	 */
	protected boolean isImmune(@Nonnull final RayTraceResult result) {
		return false;
	}

	/*
	 * Called to get the damage source object of the effect
	 */
	@Nonnull
	protected abstract DamageSource getDamageSource();

	/*
	 * Called to get the damage inflicted by the effect
	 */
	protected abstract float getDamage();

	/*
	 * Called to apply additional effects beyond damage to the target, like potion
	 * effects.
	 */
	protected abstract void applyEffects(@Nonnull final RayTraceResult result);

	/*
	 * Called to apply effects to blocks if the target that was hit was a block.
	 * MobGriefing has been checked in the case of a mob being the caster.
	 */
	protected abstract void applyBlockEffects(@Nonnull final RayTraceResult result);

	/*
	 * Subclass must provide a TEXTURE for the rendering engine
	 */
	public abstract ResourceLocation getTexture();

	@Override
	public void writeSpawnData(@Nonnull final ByteBuf buffer) {
		// Hook provided for sub-classes
	}

	@Override
	public void readSpawnData(@Nonnull final ByteBuf additionalData) {
		// Hook provided for sub-classes
	}

	@Nullable
	public EntityLivingBase getThrower() {
		if (this.caster == null && this.casterName != null && !this.casterName.isEmpty()) {
			this.caster = this.world.getPlayerEntityByName(this.casterName);

			if (this.caster == null && this.world instanceof WorldServer) {
				try {
					final Entity entity = ((WorldServer) this.world)
							.getEntityFromUuid(UUID.fromString(this.casterName));

					if (entity instanceof EntityLivingBase) {
						this.caster = (EntityLivingBase) entity;
					}
				} catch (final Throwable var2) {
					this.caster = null;
				}
			}
		}

		return this.caster;
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(@Nonnull final NBTTagCompound nbt) {
		if ((this.casterName == null || this.casterName.isEmpty()) && this.caster instanceof EntityPlayer) {
			this.casterName = this.caster.getName();
		}
		nbt.setString(NBT.THROWER, this.casterName == null ? "" : this.casterName);
		nbt.setInteger(NBT.AGE, this.age);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(@Nonnull final NBTTagCompound nbt) {
		this.caster = null;
		this.casterName = nbt.getString(NBT.THROWER);

		if (this.casterName != null && this.casterName.isEmpty()) {
			this.casterName = null;
		}

		this.caster = getThrower();

		this.age = nbt.getInteger(NBT.AGE);
	}
	
	@Nonnull
	protected String moddedDamageSource(@Nonnull final String src) {
		return ModInfo.MOD_ID + src;
	}

	private static class NBT {
		public static final String THROWER = "t";
		public static final String AGE = "a";
	}

}
