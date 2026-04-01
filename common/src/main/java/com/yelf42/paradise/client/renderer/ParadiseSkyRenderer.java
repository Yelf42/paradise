package com.yelf42.paradise.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.yelf42.paradise.Paradise;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import static com.yelf42.paradise.Paradise.PARADISE_SKY;
import static com.yelf42.paradise.Paradise.PARADISE_SKY_REFLECTION;

public class ParadiseSkyRenderer {

    public static void renderSky(PoseStack poseStack, float partialTick, ClientLevel level) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.clearColor(PARADISE_SKY.x, PARADISE_SKY.y, PARADISE_SKY.z, 1.0f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderFadingClouds(poseStack, partialTick, level, 48, false, 1.0f);

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private static void renderFadingClouds(PoseStack poseStack, float partialTick, ClientLevel level, float y, boolean flipWinding, float alpha) {
        float gameTime = level.getGameTime() + partialTick;
        float scroll = (gameTime * 0.00005f) % 1.0f;
        float s = 480.0f;
        int subdivisions = 16;
        float step = (s * 2) / subdivisions;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, Paradise.identifier("textures/environment/clouds.png"));
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        Matrix4f pose = poseStack.last().pose();

        for (int xi = 0; xi < subdivisions; xi++) {
            for (int zi = 0; zi < subdivisions; zi++) {
                float x0 = -s + xi * step;
                float x1 = x0 + step;
                float z0 = -s + zi * step;
                float z1 = z0 + step;

                float u0 = (x0 / (s * 2) );
                float u1 = (x1 / (s * 2) );
                float v0 = (z0 / (s * 2) + scroll);
                float v1 = (z1 / (s * 2) + scroll);

                // Alpha per vertex based on distance from center
                float a00 = edgeAlpha(x0, z0, s) * alpha;
                float a10 = edgeAlpha(x1, z0, s) * alpha;
                float a11 = edgeAlpha(x1, z1, s) * alpha;
                float a01 = edgeAlpha(x0, z1, s) * alpha;

                if (flipWinding) {
                    buffer.addVertex(pose, x0, y, z0).setUv(u0, v0).setColor(1f, 1f, 1f, a00);
                    buffer.addVertex(pose, x0, y, z1).setUv(u0, v1).setColor(1f, 1f, 1f, a01);
                    buffer.addVertex(pose, x1, y, z1).setUv(u1, v1).setColor(1f, 1f, 1f, a11);
                    buffer.addVertex(pose, x1, y, z0).setUv(u1, v0).setColor(1f, 1f, 1f, a10);
                } else {
                    buffer.addVertex(pose, x0, y, z0).setUv(u0, v0).setColor(1f, 1f, 1f, a00);
                    buffer.addVertex(pose, x1, y, z0).setUv(u1, v0).setColor(1f, 1f, 1f, a10);
                    buffer.addVertex(pose, x1, y, z1).setUv(u1, v1).setColor(1f, 1f, 1f, a11);
                    buffer.addVertex(pose, x0, y, z1).setUv(u0, v1).setColor(1f, 1f, 1f, a01);
                }
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void renderGroundReflection(PoseStack poseStack, float partialTick,
                                              ClientLevel level, float groundY) {
        renderSolidQuad(poseStack, groundY);
        renderFadingClouds(poseStack, partialTick, level, groundY, true, 0.7F);
    }

    private static void renderSolidQuad(PoseStack poseStack, float y) {
        float s = 480.0f;
        int subdivisions = 16;
        float step = (s * 2) / subdivisions;

        RenderSystem.polygonOffset(1.0f, 1.0f);
        RenderSystem.enablePolygonOffset();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f pose = poseStack.last().pose();

        for (int xi = 0; xi < subdivisions; xi++) {
            for (int zi = 0; zi < subdivisions; zi++) {
                float x0 = -s + xi * step;
                float x1 = x0 + step;
                float z0 = -s + zi * step;
                float z1 = z0 + step;

                int c00 = lerpSkyColor(x0, z0, s);
                int c10 = lerpSkyColor(x1, z0, s);
                int c11 = lerpSkyColor(x1, z1, s);
                int c01 = lerpSkyColor(x0, z1, s);

                buffer.addVertex(pose, x0, y,  z0).setColor(c00);
                buffer.addVertex(pose, x0, y,  z1).setColor(c01);
                buffer.addVertex(pose, x1, y,  z1).setColor(c11);
                buffer.addVertex(pose, x1, y,  z0).setColor(c10);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
    }

    // Returns PARADISE_SKY_REFLECTION at center, blends to PARADISE_SKY at edges
    private static int lerpSkyColor(float x, float z, float s) {
        float dx = Math.abs(x) / s;
        float dz = Math.abs(z) / s;
        float d = Math.max(dx, dz);
        float fadeStart = 0.5f;
        float t = Math.max(0, Math.min(1, (d - fadeStart) / (1.0f - fadeStart)));

        float r = Mth.lerp(t, PARADISE_SKY_REFLECTION.x, PARADISE_SKY.x);
        float g = Mth.lerp(t, PARADISE_SKY_REFLECTION.y, PARADISE_SKY.y);
        float b = Mth.lerp(t, PARADISE_SKY_REFLECTION.z, PARADISE_SKY.z);

        return FastColor.ARGB32.color(255, (int)(r * 255), (int)(g * 255), (int)(b * 255));
    }

    private static float edgeAlpha(float x, float z, float s) {
        float dx = Math.abs(x) / s;
        float dz = Math.abs(z) / s;
        float d = Math.max(dx, dz);
        float fadeStart = 0.6f;
        float alpha = 1.0f - Math.max(0, (d - fadeStart) / (1.0f - fadeStart));
        return Math.max(0, Math.min(1, alpha)) * 0.8f;
    }
}
