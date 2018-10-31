package org.blockartistry.doodads.util.compat;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class EntityVillagerUtil {

	private static final Field careerId = ReflectionHelper.findField(EntityVillager.class, "careerId",
			"field_175563_bv");

	private EntityVillagerUtil() {

	}

	public static int getCareerId(@Nonnull final EntityVillager entity) {
		try {
			return careerId.getInt(entity);
		} catch (@Nonnull final Throwable t) {
			// Nothing...
		}
		return 1;
	}

}
