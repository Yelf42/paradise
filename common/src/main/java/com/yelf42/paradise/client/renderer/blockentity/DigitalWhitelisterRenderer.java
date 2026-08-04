package com.yelf42.paradise.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DigitalWhitelisterBlockEntity;
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
import org.joml.Matrix4f;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class DigitalWhitelisterRenderer<T extends DigitalWhitelisterBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRenderDispatcher entityRenderDispatcher;

    private final ResourceLocation LOCKED = Paradise.identifier("textures/entity/locked_symbol.png");
    private final ResourceLocation UNLOCKED = Paradise.identifier("textures/entity/unlocked_symbol.png");

    public DigitalWhitelisterRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public void render(T t, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        if (ModRenderTypes.hologramShader == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.85f, 0.5);

        Quaternionf orientation = calculateOrientation(new Quaternionf(), t);

        poseStack.mulPose(orientation);
        float halfW = 0.5f;
        float halfH = 0.5f;
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();

        boolean unlocked = t.isPowered();
        VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.HOLOGRAM.apply(unlocked ? UNLOCKED : LOCKED));

        consumer.addVertex(pose, -halfW, -halfH, 0)
                .setColor(19, 19, 1, 0)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, -halfH, 0)
                .setColor(19, 19, 1, 0)
                .setUv(1.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, halfH, 0)
                .setColor(19, 19, 1, 0)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, -halfW, halfH, 0)
                .setColor(19, 19, 1, 0)
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
}
