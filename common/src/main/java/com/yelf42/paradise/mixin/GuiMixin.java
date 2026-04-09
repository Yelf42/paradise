package com.yelf42.paradise.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DataReaderBlock;
import com.yelf42.paradise.blocks.DigitalUploaderBlock;
import com.yelf42.paradise.blocks.EmergencyExitBlock;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.dimensions.DigitalOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Portal;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private static final ResourceLocation PORTAL_LOCATION = Paradise.identifier("textures/environment/white.png");

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void overrideNetherPortal(GuiGraphics guiGraphics, float alpha, CallbackInfo ci) {
        if (minecraft.player == null) return;

        // Should be safe
        DigitalOverlay portalOverlay = (DigitalOverlay) minecraft.player;
        if (portalOverlay.paradise$getPortalOverlayTime() > 0.0F) {
            ci.cancel();
            return;
        }

        if (minecraft.player.portalProcess == null) return;

        // Back up
        Portal portal = ((PortalProcessorAccessor)minecraft.player.portalProcess).paradise$getPortal();
        boolean inPortal = (portal instanceof DigitalUploaderBlock) || (portal instanceof DataReaderBlock) || (portal instanceof EmergencyExitBlock);
        if (inPortal) ci.cancel();
    }

    @Inject(method = "renderCameraOverlays", at = @At("TAIL"))
    private void renderDigitalPortal(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.player == null) return;
        DigitalOverlay portalOverlay = (DigitalOverlay) minecraft.player;
        if (portalOverlay.paradise$getPortalOverlayTime() > 0.0F) {
            paradise$renderPortalOverlay(guiGraphics, Math.min(portalOverlay.paradise$getPortalOverlayTime(), 1.0f));
        }
    }

    @Unique
    private void paradise$renderPortalOverlay(GuiGraphics guiGraphics, float progress) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        MultiBufferSource.BufferSource bufferSource = guiGraphics.bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.DIGITAL_TELEPORT.apply(PORTAL_LOCATION));

        Matrix4f pose = guiGraphics.pose().last().pose();
        int w = guiGraphics.guiWidth();
        int h = guiGraphics.guiHeight();

        float g = 0;
        float b = 0;
        float a = 0;

        consumer.addVertex(pose, 0, h, -90).setColor(progress,g,b,a).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(guiGraphics.pose().last(), 0, 0, 1);
        consumer.addVertex(pose, w, h, -90).setColor(progress,g,b,a).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(guiGraphics.pose().last(), 0, 0, 1);
        consumer.addVertex(pose, w, 0, -90).setColor(progress,g,b,a).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(guiGraphics.pose().last(), 0, 0, 1);
        consumer.addVertex(pose, 0, 0, -90).setColor(progress,g,b,a).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(guiGraphics.pose().last(), 0, 0, 1);

        bufferSource.endBatch(ModRenderTypes.DIGITAL_TELEPORT.apply(PORTAL_LOCATION));

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

}
