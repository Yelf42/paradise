package com.yelf42.paradise.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.blocks.AbstractDigitalSymbolBlockEntity;
import com.yelf42.paradise.client.ModRenderTypes;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class DigitalSymbolRenderer<T extends AbstractDigitalSymbolBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRenderDispatcher entityRenderDispatcher;

    private final ResourceLocation texture;
    private final double height;

    public DigitalSymbolRenderer(BlockEntityRendererProvider.Context context, ResourceLocation texture, double height) {
        this.entityRenderDispatcher = context.getBlockEntityRenderDispatcher();
        this.texture = texture;
        this.height = height;
    }

    @Override
    public void render(T t, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        if (ModRenderTypes.hologramShader == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, this.height, 0.5);

        Quaternionf orientation = calculateOrientation(new Quaternionf(), t);

        poseStack.mulPose(orientation);
        float halfW = 0.5f;
        float halfH = 0.5f;
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.HOLOGRAM.apply(this.texture));

        consumer.addVertex(pose, -halfW, -halfH, 0)
                .setColor(17, 17, 1, 0)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, -halfH, 0)
                .setColor(17, 17, 1, 0)
                .setUv(1.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, halfH, 0)
                .setColor(17, 17, 1, 0)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, -halfW, halfH, 0)
                .setColor(17, 17, 1, 0)
                .setUv(0.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        poseStack.popPose();
    }


    private Quaternionf calculateOrientation(Quaternionf quaternion, T t) {
        Vec3 cameraPos = this.entityRenderDispatcher.camera.getPosition();

        Position tPos = t.getBlockPos().getBottomCenter().add(0, 1.5, 0);
        Position pos = SableCompanion.INSTANCE.projectOutOfSubLevel(t.getLevel(), tPos);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(t);

        double dx = cameraPos.x() - pos.x();
        double dz = cameraPos.z() - pos.z();
        double worldYaw = Math.atan2(dx, dz);

        double localYaw = worldYaw;
        if (subLevelAccess != null) {
            Quaterniondc subLevelOrientation = subLevelAccess.logicalPose().orientation();
            double subLevelYaw = subLevelOrientation.getEulerAnglesYXZ(new Vector3d()).y();
            localYaw = worldYaw - subLevelYaw;
        }

        return quaternion.rotateY((float) localYaw);
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRender() && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }
}
