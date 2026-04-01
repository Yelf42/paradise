package com.yelf42.paradise.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.blocks.DataCoreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Blocks;

public class DataCoreBlockEntityRenderer<T extends DataCoreBlockEntity> implements BlockEntityRenderer<T> {
    public DataCoreBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T t, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {

        VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.translucent());

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        BakedModel model = dispatcher.getBlockModel(Blocks.TINTED_GLASS.defaultBlockState());

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                Blocks.AIR.defaultBlockState(), // dummy state is fine
                model,
                1f, 1f, 1f,
                i,
                i1
        );
    }
}
