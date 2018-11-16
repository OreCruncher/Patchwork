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

package org.orecruncher.patchwork.block.shopshelf;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.patchwork.ModInfo;
import org.orecruncher.patchwork.block.ModBlocks;
import org.orecruncher.patchwork.lib.ItemStackKey;
import org.orecruncher.patchwork.lib.ModelHelper;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = ModInfo.MOD_ID, value = Side.CLIENT)
public class ShopShelfModel extends BakedModelWrapper<IBakedModel> {

	protected final Object2ObjectOpenHashMap<ItemStackKey, IBakedModel> cache = new Object2ObjectOpenHashMap<>();
	protected final IModel proto;
	protected final VertexFormat format;

	public ShopShelfModel(@Nonnull final IModel proto, @Nonnull final IBakedModel originalModel,
			@Nonnull final VertexFormat format) {
		super(originalModel);
		this.proto = proto;
		this.format = format;
	}

	private static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = location -> {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
	};

	@Override
	@Nonnull
	public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side,
			final long rand) {
		if (state instanceof IExtendedBlockState) {
			final IExtendedBlockState extended = (IExtendedBlockState) state;
			final ItemStack mimic = extended.getValue(BlockShopShelf.MIMIC);
			if (mimic != null && !mimic.isEmpty()) {
				IBakedModel model = this.cache.get(ItemStackKey.getCachedKey(mimic));
				if (model == null) {
					final String texture = ModelHelper.getBlockTexture(mimic).getIconName();
					final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
					builder.put("texture", texture);
					final IModel retexturedModel = this.proto.retexture(builder.build());
					final IModelState modelState = retexturedModel.getDefaultState();
					model = retexturedModel.bake(modelState, this.format, textureGetter);
					this.cache.put(new ItemStackKey(mimic), model);
				}
				return model.getQuads(state, side, rand);
			}
		}
		return super.getQuads(state, side, rand);
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
			@Nonnull final ItemCameraTransforms.TransformType cameraTransformType) {
		final Pair<? extends IBakedModel, Matrix4f> pair = this.originalModel.handlePerspective(cameraTransformType);
		return Pair.of(this, pair.getRight());
	}

	@SubscribeEvent
	public static void onModelBakeEvent(@Nonnull final ModelBakeEvent event) {
		for (final EnumFacing f : BlockHorizontal.FACING.getAllowedValues()) {
			final String variant = "facing=" + f.getName();
			final ModelResourceLocation loc = new ModelResourceLocation(ModBlocks.SHOPSHELF.getRegistryName(), variant);
			final IBakedModel m = event.getModelRegistry().getObject(loc);
			if (m != null) {
				try {
					final IModel model = ModelLoaderRegistry.getModel(loc);
					final ShopShelfModel customModel = new ShopShelfModel(model, m, DefaultVertexFormats.BLOCK);
					event.getModelRegistry().putObject(loc, customModel);
				} catch (@Nonnull final Throwable t) {

				}
			}
		}
	}
}
