package com.yelf42.paradise.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.renderer.ParadiseSkyRenderer;
import com.yelf42.paradise.client.renderer.Quad;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    private ClientLevel level;
    @Final
    @Shadow
    private Minecraft minecraft;
    @Final
    @Shadow
    private static ResourceLocation FORCEFIELD_LOCATION;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        if (this.level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) {

            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(camera.rotation().conjugate(new Quaternionf()));

            ParadiseSkyRenderer.renderSky(poseStack, partialTick, this.level);
            ci.cancel();
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void onRenderClouds(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (this.level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
                    ordinal = 0))
    private void onAfterSolidChunks(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {

        if (level == null) return;
        if (!level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        PoseStack groundPose = new PoseStack();
        // Apply camera rotation so it stays fixed in world space
        groundPose.mulPose(camera.rotation().conjugate(new Quaternionf()));

        float groundY = (float)(0.0 - camera.getPosition().y);
        ParadiseSkyRenderer.renderGroundReflection(groundPose, deltaTracker.getGameTimeDeltaPartialTick(false), level, groundY + 0.98F);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // TODO
    @Inject(method = "renderWorldBorder", at = @At("HEAD"), cancellable = true)
    private void onRenderWorldBorder(Camera camera, CallbackInfo ci) {
        if (level == null) return;

        if (!level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) return;

        ci.cancel();

        WorldBorder border = this.level.getWorldBorder();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        double range = 6.0;
        double height = 6.0;

        // Only render if near border
        if (camX >= border.getMinX() + range &&
                camX <= border.getMaxX() - range &&
                camZ >= border.getMinZ() + range &&
                camZ <= border.getMaxZ() - range) {
            return;
        }

        double minY = camY - height / 2.0;
        double maxY = camY + height / 2.0;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);

        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.disableCull();

        float time = (float)(Util.getMillis() % 3000L) / 3000.0F;
        float vOffset = (float)(-Mth.frac(camY * 0.5F));

        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR
        );

        // Helper for alpha
        java.util.function.BiFunction<Double, Double, Float> computeAlpha = (worldX, worldZ) -> {
            double dx = worldX - camX;
            double dz = worldZ - camZ;

            double dist = Math.sqrt(dx * dx + dz * dz);

            // --- Horizontal fade (camera distance) ---
            double distFade = 1.0 - (dist / 32.0);
            distFade = Mth.clamp(distFade, 0.0, 1.0);
            distFade = Math.pow(distFade, 1.2);

            // --- Edge fade (distance to render cutoff) ---
            double edgeDist = range - dist; // THIS is the key fix
            double edgeFade = edgeDist / range;
            edgeFade = Mth.clamp(edgeFade, 0.0, 1.0);

            // Make edge fade sharper so it actually reaches 0
            edgeFade = Math.pow(edgeFade, 2.0);

            // --- Combine ---
            double alpha = distFade * edgeFade;

            return (float) Mth.clamp(alpha, 0.0, 1.0);
        };

        // Render one vertical quad strip helper
        java.util.function.Consumer<Quad> renderQuad = (q) -> {
            float alpha1 = computeAlpha.apply(q.x1, q.z1);
            float alpha2 = computeAlpha.apply(q.x2, q.z2);

            float u1 = (float)(q.x1 * 0.25);
            float u2 = (float)(q.x2 * 0.25);
            float v1 = (float)(minY * 0.25);
            float v2 = (float)(maxY * 0.25);

            float vScroll = time * 2.0f;

            buffer.addVertex((float)(q.x1 - camX), (float)(minY - camY), (float)(q.z1 - camZ))
                    .setUv(u1, v1 + vScroll)
                    .setColor(0.4f, 0.65f, 1.0f, alpha1);

            buffer.addVertex((float)(q.x2 - camX), (float)(minY - camY), (float)(q.z2 - camZ))
                    .setUv(u2, v1 + vScroll)
                    .setColor(0.4f, 0.65f, 1.0f, alpha2);

            buffer.addVertex((float)(q.x2 - camX), (float)(maxY - camY), (float)(q.z2 - camZ))
                    .setUv(u2, v2 + vScroll)
                    .setColor(0.4f, 0.65f, 1.0f, alpha2);

            buffer.addVertex((float)(q.x1 - camX), (float)(maxY - camY), (float)(q.z1 - camZ))
                    .setUv(u1, v2 + vScroll)
                    .setColor(0.4f, 0.65f, 1.0f, alpha1);
        };

        double minZ = Math.max(Mth.floor(camZ - range), border.getMinZ());
        double maxZ = Math.min(Mth.ceil(camZ + range), border.getMaxZ());

        double minX = Math.max(Mth.floor(camX - range), border.getMinX());
        double maxX = Math.min(Mth.ceil(camX + range), border.getMaxX());

        // +X side
        if (camX > border.getMaxX() - range) {
            for (double z = minZ; z < maxZ; z++) {
                double nextZ = Math.min(z + 1.0, maxZ);

                renderQuad.accept(new Quad(
                        border.getMaxX(), z,
                        border.getMaxX(), nextZ,
                        0.0f, 0.5f
                ));
            }
        }

        // -X side
        if (camX < border.getMinX() + range) {
            for (double z = minZ; z < maxZ; z++) {
                double nextZ = Math.min(z + 1.0, maxZ);

                renderQuad.accept(new Quad(
                        border.getMinX(), z,
                        border.getMinX(), nextZ,
                        0.0f, 0.5f
                ));
            }
        }

        // +Z side
        if (camZ > border.getMaxZ() - range) {
            for (double x = minX; x < maxX; x++) {
                double nextX = Math.min(x + 1.0, maxX);

                renderQuad.accept(new Quad(
                        x, border.getMaxZ(),
                        nextX, border.getMaxZ(),
                        0.0f, 0.5f
                ));
            }
        }

        // -Z side
        if (camZ < border.getMinZ() + range) {
            for (double x = minX; x < maxX; x++) {
                double nextX = Math.min(x + 1.0, maxX);

                renderQuad.accept(new Quad(
                        x, border.getMinZ(),
                        nextX, border.getMinZ(),
                        0.0f, 0.5f
                ));
            }
        }

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
    }

}

