package com.yelf42.paradise.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.client.renderer.ParadiseSkyRenderer;
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
import org.spongepowered.asm.mixin.Unique;
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

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void shaderUniforms(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        float time = (float) level.getGameTime() / 20.0f;

        if (ModRenderTypes.hologramShader == null) return;
        Uniform gameTime = ModRenderTypes.hologramShader.getUniform("GameTime");
        if (gameTime != null) gameTime.set(time);

        if (ModRenderTypes.digitalTeleportShader == null) return;
        Uniform gameTime2 = ModRenderTypes.digitalTeleportShader.getUniform("GameTime");
        if (gameTime2 != null) gameTime2.set(time);
    }



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

        if (camX >= border.getMinX() + range &&
                camX <= border.getMaxX() - range &&
                camZ >= border.getMinZ() + range &&
                camZ <= border.getMaxZ() - range) return;

        float time = (float)(Util.getMillis() % 3000L) / 3000.0F;
        float minY = (float)(camY - height / 2.0);
        float maxY = (float)(camY + height / 2.0);
        float vMin = minY * 0.25f + time * 2.0f;
        float vMax = maxY * 0.25f + time * 2.0f;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        // +X wall — U from Z
        if (camX > border.getMaxX() - range) {
            double wallX = border.getMaxX();
            double z0 = Math.max(camZ - range, border.getMinZ());
            double z1 = Math.min(camZ + range, border.getMaxZ());
            for (double z = z0; z < z1; z++) {
                double zNext = Math.min(z + 1, z1);
                float alpha0 = wallAlpha(wallX, z,     camX, camZ, range);
                float alpha1 = wallAlpha(wallX, zNext, camX, camZ, range);
                float u0 = (float)(z * 0.25);
                float u1 = (float)(zNext * 0.25);
                addWallQuad(buffer, wallX, z, wallX, zNext, camX, camY, camZ, minY, maxY, u0, u1, vMin, vMax, alpha0, alpha1);
            }
        }

        // -X wall — U from Z
        if (camX < border.getMinX() + range) {
            double wallX = border.getMinX();
            double z0 = Math.max(camZ - range, border.getMinZ());
            double z1 = Math.min(camZ + range, border.getMaxZ());
            for (double z = z0; z < z1; z++) {
                double zNext = Math.min(z + 1, z1);
                float alpha0 = wallAlpha(wallX, z,     camX, camZ, range);
                float alpha1 = wallAlpha(wallX, zNext, camX, camZ, range);
                float u0 = (float)(z * 0.25);
                float u1 = (float)(zNext * 0.25);
                addWallQuad(buffer, wallX, z, wallX, zNext, camX, camY, camZ, minY, maxY, u0, u1, vMin, vMax, alpha0, alpha1);
            }
        }

        // +Z wall — U from X
        if (camZ > border.getMaxZ() - range) {
            double wallZ = border.getMaxZ();
            double x0 = Math.max(camX - range, border.getMinX());
            double x1 = Math.min(camX + range, border.getMaxX());
            for (double x = x0; x < x1; x++) {
                double xNext = Math.min(x + 1, x1);
                float alpha0 = wallAlpha(x,     wallZ, camX, camZ, range);
                float alpha1 = wallAlpha(xNext, wallZ, camX, camZ, range);
                float u0 = (float)(x * 0.25);
                float u1 = (float)(xNext * 0.25);
                addWallQuad(buffer, x, wallZ, xNext, wallZ, camX, camY, camZ, minY, maxY, u0, u1, vMin, vMax, alpha0, alpha1);
            }
        }

        // -Z wall — U from X
        if (camZ < border.getMinZ() + range) {
            double wallZ = border.getMinZ();
            double x0 = Math.max(camX - range, border.getMinX());
            double x1 = Math.min(camX + range, border.getMaxX());
            for (double x = x0; x < x1; x++) {
                double xNext = Math.min(x + 1, x1);
                float alpha0 = wallAlpha(x,     wallZ, camX, camZ, range);
                float alpha1 = wallAlpha(xNext, wallZ, camX, camZ, range);
                float u0 = (float)(x * 0.25);
                float u1 = (float)(xNext * 0.25);
                addWallQuad(buffer, x, wallZ, xNext, wallZ, camX, camY, camZ, minY, maxY, u0, u1, vMin, vMax, alpha0, alpha1);
            }
        }

        MeshData mesh = buffer.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
    }

    @Unique
    private static float wallAlpha(double x, double z, double camX, double camZ, double range) {
        double dx = x - camX;
        double dz = z - camZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        double t = 1.0 - (dist / range);
        t = Mth.clamp(t, 0.0, 1.0);
        return (float)(t * t * 0.8);
    }

    @Unique
    private static void addWallQuad(BufferBuilder buffer,
                                    double x1, double z1, double x2, double z2,
                                    double camX, double camY, double camZ,
                                    float minY, float maxY,
                                    float u1, float u2, float vMin, float vMax,
                                    float alpha1, float alpha2) {
        float r = 0.4f, g = 0.65f, b = 1.0f;
        buffer.addVertex((float)(x1-camX), minY-(float)camY, (float)(z1-camZ)).setUv(u1, vMin).setColor(r, g, b, alpha1);
        buffer.addVertex((float)(x2-camX), minY-(float)camY, (float)(z2-camZ)).setUv(u2, vMin).setColor(r, g, b, alpha2);
        buffer.addVertex((float)(x2-camX), maxY-(float)camY, (float)(z2-camZ)).setUv(u2, vMax).setColor(r, g, b, alpha2);
        buffer.addVertex((float)(x1-camX), maxY-(float)camY, (float)(z1-camZ)).setUv(u1, vMax).setColor(r, g, b, alpha1);
    }

}

