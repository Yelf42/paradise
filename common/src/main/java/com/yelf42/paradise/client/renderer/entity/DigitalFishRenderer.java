package com.yelf42.paradise.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.entities.DigitalFish;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class DigitalFishRenderer extends EntityRenderer<DigitalFish> {

    public static final ResourceLocation FISH_LOCATION = Paradise.identifier("textures/entity/digital_fish.png");

    public DigitalFishRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DigitalFish digitalFish) {
        return FISH_LOCATION;
    }

    @Override
    public void render(DigitalFish p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (ModRenderTypes.shimmerShader == null) return;

        poseStack.pushPose();
        poseStack.translate(0, 0.3, 0);

        float yBodyRot = Mth.rotLerp(partialTick, p_entity.yBodyRotO, p_entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - yBodyRot));

        float halfW = 1.0f;
        float halfH = 0.5f;
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.SHIMMER.apply(FISH_LOCATION));

        float time = (float) p_entity.level().getGameTime() / 20.0f;
        float alpha = p_entity.getLifespanFade();
        stripTesselation(consumer, pose, lastPose, halfW, halfH, 32, time, alpha);

        poseStack.popPose();
    }

    private void stripTesselation(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose lastPose, float halfW, float halfH, int strips, float time, float alpha) {
        float fullW = halfW * 2;
        for (int i = 0; i < strips; i++) {
            float u0 = (float) i / strips;
            float u1 = (float) (i + 1) / strips;
            float x0 = -halfW + u0 * fullW;
            float x1 = -halfW + u1 * fullW;

            float zBump = computeZBump(i, strips, time);

            // top-left
            consumer.addVertex(pose, x0, halfH, zBump)
                    .setColor(32, 16, 1, alpha)
                    .setUv(u0, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);
            // top-right
            consumer.addVertex(pose, x1, halfH, zBump)
                    .setColor(32, 16, 1, alpha)
                    .setUv(u1, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);
            // bottom-right
            consumer.addVertex(pose, x1, -halfH, zBump)
                    .setColor(32, 16, 1, alpha)
                    .setUv(u1, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);
            // bottom-left
            consumer.addVertex(pose, x0, -halfH, zBump)
                    .setColor(32, 16, 1, alpha)
                    .setUv(u0, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);
        }
    }

    private float computeZBump(int strip, int strips, float gameTime) {
        float time = gameTime * 15.0f;

        float scanlinePos = (time % strips) / strips;
        float stripPos = (float) strip / strips;

        float dist = stripPos - scanlinePos;
        dist = dist - (float) Math.floor(dist + 0.5f);

        float falloffWidth = 0.5f;
        float bumpStrength = 0.1f;

        float t = Math.abs(dist) / falloffWidth;
        if (t >= 1.0f) return 0.0f;

        float bump = Math.copySign(bumpStrength * (float) Math.sin(t * Math.PI), dist);
        return Math.round(bump / 0.01f) * 0.01f;
    }

    // TODO just face where the entity should face
    private Quaternionf calculateOrientation(Quaternionf quaternion, Vec3 pos) {
        Vec3 cameraPos = this.entityRenderDispatcher.camera.getPosition();
        double dx = cameraPos.x() - pos.x();
        double dz = cameraPos.z() - pos.z();
        float yaw = (float) Math.atan2(dx, dz);
        return quaternion.rotateY(yaw);
    }
}
