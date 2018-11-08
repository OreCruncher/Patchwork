package org.orecruncher.patchwork.common.block.entity;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;

public class ContainerFurnace3D extends ContainerFurnace {

	public ContainerFurnace3D(@Nonnull final InventoryPlayer playerInventory, @Nonnull final IInventory furnaceInventory) {
		super(playerInventory, furnaceInventory);

	}

}
