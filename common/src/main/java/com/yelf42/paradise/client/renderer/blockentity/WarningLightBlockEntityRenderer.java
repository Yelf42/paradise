package com.yelf42.paradise.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.WarningLightBlockEntity;
import com.yelf42.paradise.client.ModRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class WarningLightBlockEntityRenderer<T extends WarningLightBlockEntity> implements BlockEntityRenderer<T> {

    private final ResourceLocation WHITE = Paradise.identifier("textures/environment/white.png");

    public WarningLightBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(WarningLightBlockEntity blockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        if (ModRenderTypes.unshadedColorShader == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        Direction facing = blockEntity.getFacing();
        poseStack.mulPose(directionToQuaternion(facing));

        float time = (float) blockEntity.getLevel().getGameTime() / 5.0f;
        Quaternionf orientation = new Quaternionf().rotateY(time);
        poseStack.mulPose(orientation);

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.UNSHADED_COLOR);

        if (blockEntity.isPowered()) {
            float minY1 = -0.2f;
            float minY2 = -0.6f;

            float maxY1 = 0.0f;
            float maxY2 = 0.4f;

            Vector3f n = new Vector3f(0, 0, 0);

            // Quad 1
            consumer.addVertex(pose, 0.0f, minY1, 0)
                    .setColor(255, 0, 0, 155)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            consumer.addVertex(pose, 2.5f, minY2, 0)
                    .setColor(255, 0, 0, 0)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            consumer.addVertex(pose, 2.5f, maxY2, 0)
                    .setColor(255, 0, 0, 0)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            consumer.addVertex(pose, 0.0f, maxY1, 0)
                    .setColor(255, 0, 0, 155)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            // Quad 2
            consumer.addVertex(pose, 0.0f, minY1, 0)
                    .setColor(255, 0, 0, 155)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            consumer.addVertex(pose, -2.5f, minY2, 0)
                    .setColor(255, 0, 0, 0)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            consumer.addVertex(pose, -2.5f, maxY2, 0)
                    .setColor(255, 0, 0, 0)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());

            consumer.addVertex(pose, -0.0f, maxY1, 0)
                    .setColor(255, 0, 0, 155)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, n.x(), n.y(), n.z());
        }

        ((MultiBufferSource.BufferSource) multiBufferSource).endBatch(ModRenderTypes.UNSHADED_COLOR);

        VertexConsumer solidConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(WHITE));
        if (blockEntity.isPowered()) {
            renderSquarePrism(lastPose, pose, solidConsumer,0.125f, 0.25f, -0.125f, 255, 0, 0);
        } else {
            renderSquarePrism(lastPose, pose, solidConsumer,0.125f, 0.25f, -0.125f, 100, 0, 0);
        }

        poseStack.popPose();
    }

    private void renderSquarePrism(PoseStack.Pose lastPose, Matrix4f pose, VertexConsumer consumer, float halfW, float halfH, float offsetY, int r, int g, int b) {
        float y0 = offsetY - halfH;
        float y1 = offsetY + halfH;

        // +Z face (front)
        consumer.addVertex(pose, -halfW, y0,  halfW).setColor(r,g,b,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,1);
        consumer.addVertex(pose,  halfW, y0,  halfW).setColor(r,g,b,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,1);
        consumer.addVertex(pose,  halfW, y1,  halfW).setColor(r,g,b,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,1);
        consumer.addVertex(pose, -halfW, y1,  halfW).setColor(r,g,b,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,1);

        // -Z face (back)
        consumer.addVertex(pose,  halfW, y0, -halfW).setColor(r,g,b,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,-1);
        consumer.addVertex(pose, -halfW, y0, -halfW).setColor(r,g,b,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,-1);
        consumer.addVertex(pose, -halfW, y1, -halfW).setColor(r,g,b,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,-1);
        consumer.addVertex(pose,  halfW, y1, -halfW).setColor(r,g,b,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,0,-1);

        // +X face (right)
        consumer.addVertex(pose, halfW, y0, -halfW).setColor(r,g,b,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 1,0,0);
        consumer.addVertex(pose, halfW, y0,  halfW).setColor(r,g,b,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 1,0,0);
        consumer.addVertex(pose, halfW, y1,  halfW).setColor(r,g,b,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 1,0,0);
        consumer.addVertex(pose, halfW, y1, -halfW).setColor(r,g,b,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 1,0,0);

        // -X face (left)
        consumer.addVertex(pose, -halfW, y0,  halfW).setColor(r,g,b,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, -1,0,0);
        consumer.addVertex(pose, -halfW, y0, -halfW).setColor(r,g,b,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, -1,0,0);
        consumer.addVertex(pose, -halfW, y1, -halfW).setColor(r,g,b,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, -1,0,0);
        consumer.addVertex(pose, -halfW, y1,  halfW).setColor(r,g,b,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, -1,0,0);

        // +Y face
        consumer.addVertex(pose, -halfW, y1,  halfW).setColor(r,g,b,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,1,0);
        consumer.addVertex(pose,  halfW, y1,  halfW).setColor(r,g,b,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,1,0);
        consumer.addVertex(pose,  halfW, y1, -halfW).setColor(r,g,b,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,1,0);
        consumer.addVertex(pose, -halfW, y1, -halfW).setColor(r,g,b,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(lastPose, 0,1,0);
    }

    private static Quaternionf directionToQuaternion(Direction direction) {
        return switch (direction) {
            case UP    -> new Quaternionf();
            case DOWN  -> new Quaternionf().rotateZ((float) Math.PI);
            case NORTH -> new Quaternionf().rotateX((float) (-Math.PI / 2));
            case SOUTH -> new Quaternionf().rotateX((float) (Math.PI / 2));
            case EAST  -> new Quaternionf().rotateZ((float) (-Math.PI / 2));
            case WEST  -> new Quaternionf().rotateZ((float) (Math.PI / 2));
        };
    }

}
