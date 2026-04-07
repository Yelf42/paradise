package com.yelf42.paradise.client.renderer.blockentity;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DataReaderBlockEntity;
import com.yelf42.paradise.client.ModRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataReaderBlockEntityRenderer<T extends DataReaderBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRenderDispatcher entityRenderDispatcher;

    public DataReaderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.getBlockEntityRenderDispatcher();
    }

    public static final ResourceLocation PORTAL_LOCATION = Paradise.identifier("textures/entity/portal.png");

    @Override
    public void render(T t, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        Level level = t.getLevel();
        if (level == null) return;
        if (ModRenderTypes.hologramShader == null) return;
        long time = t.getLevel().getGameTime();
        int frame = Math.floorMod(time, 12);

        poseStack.pushPose();
        poseStack.translate(0.5, 2.0, 0.5);
        Quaternionf orientation = calculateOrientation(new Quaternionf(), t.getBlockPos().getBottomCenter().add(0, 2, 0));
        poseStack.mulPose(orientation);
        float halfW = 0.5f;
        float halfH = 1.0f;
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.HOLOGRAM.apply(PORTAL_LOCATION));

        float u0 = frame / 12.0F;
        float u1 = u0 + 0.083333F;

        consumer.addVertex(pose, -halfW, -halfH, 0)
                .setColor(16, 32, 12, 0)
                .setUv(u0, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, -halfH, 0)
                .setColor(16, 32, 12, 0)
                .setUv(u1, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, halfH, 0)
                .setColor(16, 32, 12, 0)
                .setUv(u1, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, -halfW, halfH, 0)
                .setColor(16, 32, 12, 0)
                .setUv(u0, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderPortal() && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }


    private Quaternionf calculateOrientation(Quaternionf quaternion, Vec3 pos) {
        Vec3 cameraPos = this.entityRenderDispatcher.camera.getPosition();
        double dx = cameraPos.x() - pos.x();
        double dz = cameraPos.z() - pos.z();
        float yaw = (float) Math.atan2(dx, dz);
        return quaternion.rotateY(yaw);
    }

}
