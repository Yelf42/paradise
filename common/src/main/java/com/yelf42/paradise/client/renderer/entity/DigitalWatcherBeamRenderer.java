package com.yelf42.paradise.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.entities.DigitalWatcherBeam;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class DigitalWatcherBeamRenderer extends EntityRenderer<DigitalWatcherBeam> {
    public static final ResourceLocation TEXTURE_LOCATION = Paradise.identifier("textures/entity/watcher_beam_indicator.png");
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");


    public DigitalWatcherBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(DigitalWatcherBeam digitalWatcherBeam) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void render(DigitalWatcherBeam beam, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        long time = beam.level().getGameTime();

        int lifespan = beam.getLifespan();
        if (lifespan < 2) return;

        if (lifespan < DigitalWatcherBeam.INDICATE_THRESHOLD) {
            // Render eye
            if (ModRenderTypes.watcherShader == null) return;

            float halfW = 0.5f;
            float halfH = 0.5f;

            poseStack.pushPose();
            poseStack.translate(0, beam.getIndicatorHeight() + 0.05f, 0);
            poseStack.mulPose(new Quaternionf().rotateY(time / 5.f).rotateX((float) (Math.PI / 2.0f)));
            
            PoseStack.Pose lastPose = poseStack.last();
            Matrix4f pose = lastPose.pose();

            VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.WATCHER.apply(TEXTURE_LOCATION));

            consumer.addVertex(pose, -halfW, -halfH, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);

            consumer.addVertex(pose, halfW, -halfH, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);

            consumer.addVertex(pose, halfW, halfH, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);

            consumer.addVertex(pose, -halfW, halfH, 0)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(lastPose, 0, 1, 0);
            
            poseStack.popPose();

        } else {
            // Render beam
            renderBeam(poseStack, multiBufferSource, BEAM_LOCATION, partialTick, 1.0f, time, 0, 1024, 16262179, 0.2F, 0.25F);
        }
    }

    @Override
    public boolean shouldRender(DigitalWatcherBeam livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    public static void renderBeam(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation beamLocation, float partialTick, float textureScale, long gameTime, int yOffset, int height, int color, float beamRadius, float glowRadius) {
        int i = yOffset + height;
        poseStack.pushPose();
        float f = (float)Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f6 = -beamRadius;
        float f9 = -beamRadius;
        float f12 = -1.0F + f2;
        float f13 = (float)height * textureScale * (0.5F / beamRadius) + f12;
        renderPart(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, false)), color, yOffset, i, 0.0F, beamRadius, beamRadius, 0.0F, f6, 0.0F, 0.0F, f9, 0.0F, 1.0F, f13, f12);
        poseStack.popPose();
        float f3 = -glowRadius;
        float f4 = -glowRadius;
        float f5 = -glowRadius;
        f6 = -glowRadius;
        f12 = -1.0F + f2;
        f13 = (float)height * textureScale + f12;
        renderPart(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, true)), FastColor.ARGB32.color(32, color), yOffset, i, f3, f4, glowRadius, f5, f6, glowRadius, glowRadius, glowRadius, 0.0F, 1.0F, f13, f12);
        poseStack.popPose();
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer consumer, int color, int minY, int maxY, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float minU, float maxU, float minV, float maxV) {
        PoseStack.Pose posestack$pose = poseStack.last();
        renderQuad(posestack$pose, consumer, color, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
        renderQuad(posestack$pose, consumer, color, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV);
        renderQuad(posestack$pose, consumer, color, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV);
        renderQuad(posestack$pose, consumer, color, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, int color, int minY, int maxY, float minX, float minZ, float maxX, float maxZ, float minU, float maxU, float minV, float maxV) {
        addVertex(pose, consumer, color, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, color, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, color, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, color, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, int color, int y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, (float)y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
