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

package org.orecruncher.patchwork.common.item;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.lib.IVariant;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.collections.EmptySet;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.patchwork.ModBase;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.client.ModCreativeTab;
import org.orecruncher.patchwork.common.item.magic.AbilityHandler;
import org.orecruncher.patchwork.common.item.magic.MagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.CapabilityMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDevice;
import org.orecruncher.patchwork.common.item.magic.capability.IMagicDeviceSettable;

import com.google.common.collect.Sets;

import baubles.api.BaubleType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicDevice extends ItemBase {

	public static final int BASE_CONSUMPTION_UNIT = 10;
	public static final int TICKS_PER_MINUTE = 20 * 60;

	private static final String POWER_FMT = Localization.loadString("patchwork.magicdevice.powerremaining");
	private static final String DEVICE_POWER_LEVELS_FMT = Localization.loadString("patchwork.magicdevice.powerlevels");
	private static final String DEVICE_QUALITY_FMT = Localization.loadString("patchwork.magicdevice.quality");
	private static final String DEVICE_NAME_SIMPLE_FMT = Localization.loadString("patchwork.magicdevice.basicname");
	private static final String ADVANCED_TOOLTIP_FMT = Localization.loadString("patchwork.msg.advanced_tooltip");

	private static final Map<Type, String> DEVICE_TYPE_NAMES = new EnumMap<>(Type.class);
	private static final Map<Quality, String> DEVICE_QUALITY_NAMES = new EnumMap<>(Quality.class);

	private static final ResourceLocation VARIANT_GETTER_ID = new ResourceLocation(ModInfo.MOD_ID, "variant");
	private static final IItemPropertyGetter VARIANT_GETTER = new IItemPropertyGetter() {
		@SideOnly(Side.CLIENT)
		@Override
		public float apply(@Nonnull final ItemStack stack, @Nonnull final World worldIn,
				@Nonnull final EntityLivingBase entityIn) {
			return ItemMagicDevice.getCapability(stack).getVariant();
		}
	};

	static {
		for (final Type b : Type.values())
			DEVICE_TYPE_NAMES.put(b,
					Localization.loadString(ModInfo.MOD_ID + ".magicdevice." + b.name().toLowerCase() + ".name"));
		for (final Quality d : Quality.values())
			DEVICE_QUALITY_NAMES.put(d, Localization.loadString(d.getUnlocalizedName()));
	}

	public ItemMagicDevice(@Nonnull final String name) {
		super(name);
		setCreativeTab(ModCreativeTab.tab);
		setMaxStackSize(1);
		setHasSubtypes(true);
		addPropertyOverride(VARIANT_GETTER_ID, VARIANT_GETTER);
	}

	@Override
	public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			for (final Type t : Type.values()) {
				final ItemStack stack = new ItemStack(this, 1, t.getSubTypeId());
				final IMagicDeviceSettable xface = (IMagicDeviceSettable) stack
						.getCapability(CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
				xface.setVariant(0);
				items.add(stack);
			}

			for (final MagicDevice device : MagicDevice.DEVICES.values()) {
				final ItemStack stack = new ItemStack(this, 1, device.getType().getSubTypeId());
				final IMagicDeviceSettable xface = (IMagicDeviceSettable) stack
						.getCapability(CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
				xface.setMoniker(device.getUnlocalizedName());
				xface.setQuality(device.getQuality());
				xface.setCurrentEnergy(xface.getMaxEnergy());
				for (final ResourceLocation r : device.getAbilities())
					xface.addAbilities(r);
				items.add(stack);
			}
		}
	}

	@Override
	public void registerItemModel() {
		// Register for each of the bauble slots
		for (final Type bt : Type.values()) {
			ModBase.proxy().registerItemRenderer(this, bt.getSubTypeId(),
					new ModelResourceLocation(ModInfo.MOD_ID + ":magic_device_" + bt.name(), "inventory"));
		}
	}

	@Override
	public boolean showDurabilityBar(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		return caps != null && caps.getMaxEnergy() > 0;
	}

	@Override
	public double getDurabilityForDisplay(@Nonnull final ItemStack stack) {
		final IMagicDeviceSettable caps = (IMagicDeviceSettable) getCapability(stack);
		return caps != null ? (100F - caps.getPowerRatio()) / 100F : 0F;
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		if (caps != null) {
			final String moniker = caps.getMoniker();
			if (moniker.isEmpty()) {
				final String quality = DEVICE_QUALITY_NAMES.get(caps.getQuality());
				final String device = DEVICE_TYPE_NAMES.get(Type.bySubTypeId(stack.getMetadata()));
				return String.format(DEVICE_NAME_SIMPLE_FMT, quality, device);
			}
			return Localization.loadString(moniker);
		}
		return "WFT?";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack stack, @Nullable final World world,
			@Nonnull final List<String> tips, @Nonnull final ITooltipFlag flag) {

		final boolean doAdvanced = GuiScreen.isShiftKeyDown();
		final IMagicDevice caps = getCapability(stack);

		if (showDurabilityBar(stack)) {
			tips.add(String.format(POWER_FMT, caps.getPowerRatio()));
			if (doAdvanced)
				tips.add(String.format(DEVICE_POWER_LEVELS_FMT, caps.getCurrentEnergy(), caps.getMaxEnergy()));
		}

		if (doAdvanced) {
			tips.add(String.format(DEVICE_QUALITY_FMT, DEVICE_QUALITY_NAMES.get(caps.getQuality())));
		}

		gatherHandlers(caps).forEach(h -> {
			tips.add(h.getName());
			if (doAdvanced) {
				for (final String s : h.getToolTip())
					tips.add("    " + s);
			}
		});

		if (!doAdvanced) {
			tips.add("");
			tips.add(ADVANCED_TOOLTIP_FMT);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(@Nonnull final ItemStack stack) {
		return getQuality(stack) == Quality.LEGENDARY;
	}

	@Override
	@Nonnull
	public EnumRarity getRarity(@Nonnull final ItemStack stack) {
		return getQuality(stack).getRarity();
	}

	@Override
	public boolean isBookEnchantable(@Nonnull final ItemStack stack, @Nonnull final ItemStack book) {
		return false;
	}

	@Nonnull
	public Type getDeviceType(@Nonnull final ItemStack stack) {
		return Type.bySubTypeId(stack.getMetadata());
	}

	public Quality getQuality(@Nonnull final ItemStack stack) {
		final IMagicDevice caps = getCapability(stack);
		return caps != null ? caps.getQuality() : Quality.PLAIN;
	}

	@Nonnull
	public static ItemStack addDeviceAbility(@Nonnull final ItemStack stack, @Nonnull final ResourceLocation ability) {
		final IMagicDeviceSettable caps = (IMagicDeviceSettable) getCapability(stack);
		if (caps != null) {
			caps.addAbilities(ability);
		}
		return stack;
	}

	@Nullable
	public static IMagicDevice getCapability(@Nonnull final ItemStack stack) {
		return stack.getCapability(CapabilityMagicDevice.MAGIC_DEVICE, CapabilityMagicDevice.DEFAULT_FACING);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	@Nonnull
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
			float hitX, float hitY, float hitZ) {
		final ItemStack stack = player.getHeldItem(hand);
		if (!player.getEntityWorld().isRemote) {
			final IMagicDevice caps = getCapability(stack);
			gatherHandlers(caps)
					.forEach(da -> da.onItemUse(caps, stack, player, world, pos, hand, facing, hitX, hitY, hitZ));
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(@Nonnull final World world, @Nonnull final EntityPlayer player,
			@Nonnull final EnumHand hand) {
		final ItemStack stack = player.getHeldItem(hand);
		if (!player.getEntityWorld().isRemote) {
			final IMagicDevice caps = getCapability(stack);
			gatherHandlers(caps).forEach(da -> da.onItemRightClick(caps, stack, world, player, hand));
		}
		player.swingArm(EnumHand.MAIN_HAND);
		return super.onItemRightClick(world, player, hand);
	}

	/**
	 * Called when an entity is hit with the item
	 *
	 * @param entity
	 * @param player
	 */
	@Override
	public boolean hitEntity(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase target,
			@Nonnull final EntityLivingBase attacker) {
		if (!attacker.getEntityWorld().isRemote) {
			final IMagicDevice caps = getCapability(stack);
			final Optional<Boolean> result = gatherHandlers(caps).map(da -> da.hitEntity(caps, stack, target, attacker))
					.reduce(Boolean::logicalOr);
			return result.isPresent() ? result.get() : false;
		}
		return false;
	}

	/**
	 * This method is called once per tick if the bauble is being worn by a player
	 * Redirect from BaubleAdaptor.
	 */
	public void onWornTick(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote) {
			final IMagicDevice caps = getCapability(stack);
			gatherHandlers(caps).forEach(da -> da.doTick(caps, player, stack));
		}
	}

	/**
	 * This method is called when the bauble is equipped by a player Redirect from
	 * BaubleAdaptor.
	 */
	public void onEquipped(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote) {
			final IMagicDevice caps = getCapability(stack);
			gatherHandlers(caps).forEach(da -> da.equip(caps, player, stack));
		}
	}

	/**
	 * This method is called when the bauble is unequipped by a player Redirect from
	 * BaubleAdaptor.
	 */
	public void onUnequipped(@Nonnull final ItemStack stack, @Nonnull final EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote) {
			final IMagicDevice caps = getCapability(stack);
			gatherHandlers(caps).forEach(da -> da.unequip(caps, player, stack));
		}
	}

	/**
	 * Indicates if the given ItemStack can be ticked based on the TickWhen supplied
	 *
	 * @param stack
	 *                  Stack to check
	 * @param when
	 *                  When the tick is occuring
	 * @return true if the stack can be ticked; false otherwise
	 */
	public boolean canBeTicked(@Nonnull final ItemStack stack, @Nonnull final TickWhen when) {
		return Type.bySubTypeId(stack.getMetadata()).canTick(when);
	}

	/**
	 * This method return the type of bauble this is. Type is used to determine the
	 * slots it can go into. Redirect from BaubleAdaptor.
	 */
	@Nonnull
	public BaubleType getBaubleType(@Nonnull final ItemStack stack) {
		return Type.bySubTypeId(stack.getMetadata()).getBaubleType();
	}

	@Nonnull
	private static Stream<AbilityHandler> gatherHandlers(@Nonnull final IMagicDevice caps) {
		return caps.getAbilities().stream().map(r -> AbilityHandler.REGISTRY.getValue(r)).filter(Objects::nonNull);
	}

	public static enum TickWhen {
		// Never tick the item. Will still get the other events like clicking
		// equip, and unequip.
		NEVER,
		// Only tick when held in the main or off hand.
		HELD,
		// Tick when worn, such as a bauble or armor slot
		WORN,
		// Always tick - as long as it is somewhere in player inventory
		ALWAYS;
	}

	public static enum Quality {
		//
		PLAIN("plain", 0, 0, EnumRarity.COMMON),
		//
		MUNDANE("mundane", 1, TICKS_PER_MINUTE * 60 * BASE_CONSUMPTION_UNIT, EnumRarity.COMMON),
		//
		NORMAL("normal", 2, TICKS_PER_MINUTE * 60 * 2 * BASE_CONSUMPTION_UNIT, EnumRarity.UNCOMMON),
		//
		PRIZED("prized", 3, TICKS_PER_MINUTE * 60 * 4 * BASE_CONSUMPTION_UNIT, EnumRarity.RARE),
		//
		LEGENDARY("legendary", 4, TICKS_PER_MINUTE * 60 * 8 * BASE_CONSUMPTION_UNIT, EnumRarity.EPIC);

		private final String name;
		private final int maxAbilities;
		private final EnumRarity rarity;
		private final int maxPower;

		private Quality(@Nonnull final String name, final int maxAbilities, final int maxPower,
				@Nonnull final EnumRarity rarity) {
			this.name = ModInfo.MOD_ID + ".devicequality." + name + ".name";
			this.maxAbilities = maxAbilities;
			this.maxPower = maxPower;
			this.rarity = rarity;
		}

		@Nonnull
		public String getUnlocalizedName() {
			return this.name;
		}

		public int getMaxAbilities() {
			return this.maxAbilities;
		}

		@Nonnull
		public EnumRarity getRarity() {
			return this.rarity;
		}

		public int getMaxPower() {
			return this.maxPower;
		}
	}

	public static enum Type implements IVariant {
		//
		AMULET(0, BaubleType.AMULET, "amulet", 8, TickWhen.WORN),
		//
		RING(1, BaubleType.RING, "ring", 5, TickWhen.WORN),
		//
		BELT(2, BaubleType.BELT, "belt", 1, TickWhen.WORN),
		//
		TRINKET(3, BaubleType.TRINKET, "trinket", 7, TickWhen.WORN),
		//
		HEAD(4, BaubleType.HEAD, "head", 8, TickWhen.WORN),
		//
		BODY(5, BaubleType.BODY, "body", 8, TickWhen.WORN),
		//
		CHARM(6, BaubleType.CHARM, "charm", 8, TickWhen.WORN),
		//
		ROD(7, null, "rod", 4, TickWhen.HELD),
		//
		WAND(8, null, "wand", 7, TickWhen.NEVER),
		//
		SCROLL(9, null, "scroll", 8, TickWhen.NEVER),
		//
		STAFF(10, null, "staff", 8, TickWhen.HELD),
		//
		TOME(11, null, "tome", 8, TickWhen.ALWAYS);

		private static final Type[] SUBTYPE_LOOKUP = Stream.of(values())
				.sorted(Comparator.comparing(Type::getSubTypeId)).toArray(Type[]::new);

		private final BaubleType bauble;
		private final String unlocalizedName;
		private final String name;
		private final int subTypeId;
		private final int variants;
		private final Set<TickWhen> whenToTick;

		private Type(final int subTypeId, @Nullable final BaubleType type, @Nonnull final String name, final int v,
				@Nonnull final TickWhen when, @Nonnull final TickWhen... ticking) {
			this.subTypeId = subTypeId;
			this.bauble = type;
			this.name = name;
			this.unlocalizedName = ModInfo.MOD_ID + ".magicdevice." + name + ".name";
			this.variants = v;
			this.whenToTick = ticking == null ? EmptySet.empty() : Sets.immutableEnumSet(when, ticking);
		}

		@Nonnull
		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}

		@Nullable
		public BaubleType getBaubleType() {
			return this.bauble;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public int getSubTypeId() {
			return this.subTypeId;
		}

		public int getVariants() {
			return this.variants;
		}

		public boolean canTick(@Nonnull final TickWhen when) {
			return this.whenToTick.contains(when);
		}

		@Nonnull
		public static Type bySubTypeId(int subTypeId) {
			return SUBTYPE_LOOKUP[MathStuff.clamp(subTypeId, 0, SUBTYPE_LOOKUP.length - 1)];
		}

	}
}
