package com.yelf42.paradise.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.entities.DigitalWatcher;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DigitalWatcherRenderer extends EntityRenderer<DigitalWatcher>  {
    public static final ResourceLocation TEXTURE_LOCATION = Paradise.identifier("textures/entity/watcher_blink.png");

    public DigitalWatcherRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DigitalWatcher digitalWatcher) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void render(DigitalWatcher digitalWatcher, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (ModRenderTypes.watcherShader == null) return;

        int alpha = digitalWatcher.getAlpha();
        if (alpha <= 0 ) return;

        poseStack.pushPose();

        float halfW = 4.0f;
        float halfH = 4.0f;

        Vec3 watcherPos = digitalWatcher.getPosition(partialTick);

        Quaternionf orientation = calculateOrientation(new Quaternionf(), digitalWatcher.getFaceTowards(), watcherPos);
        poseStack.mulPose(orientation);

        Vec3 cameraPos = this.entityRenderDispatcher.camera.getPosition();
        float scale = (float) (cameraPos.distanceTo(watcherPos) / 128.f) * 8.f;
        scale = Math.clamp(scale, 4.f, 8.f);
        poseStack.scale(scale,scale,scale);

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        VertexConsumer consumer = multiBufferSource.getBuffer(ModRenderTypes.WATCHER.apply(TEXTURE_LOCATION));

        int t = 6 - digitalWatcher.getBlink();
        int frame = t < 4 ? t : 6 - t;

        float u0 = 0.0f;
        float v0 = 0.25f * frame;

        float u1 = 1.0f;
        float v1 = 0.25f + 0.25f * frame;

        consumer.addVertex(pose, -halfW, -halfH, 0)
                .setColor(64, 64, 4, alpha)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, -halfH, 0)
                .setColor(64, 64, 4, alpha)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, halfW, halfH, 0)
                .setColor(64, 64, 4, alpha)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        consumer.addVertex(pose, -halfW, halfH, 0)
                .setColor(64, 64, 4, alpha)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(lastPose, 0, 1, 0);

        poseStack.popPose();
    }

    private Quaternionf calculateOrientation(Quaternionf quaternion, Vector3f to, Vec3 from) {
        double dx = from.x() - to.x();
        double dy = from.y() - to.y();
        double dz = from.z() - to.z();
        float yaw = (float) Math.atan2(dx, dz);
        float pitch = (float) -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
        return quaternion.rotateY(yaw).rotateX(pitch);
    }

    @Override
    public boolean shouldRender(DigitalWatcher entity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
