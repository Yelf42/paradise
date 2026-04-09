package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DigitalOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Unique
    private static float paradise$savedOverlayTime = 0.0f;

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void beforeRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.portalProcess != null) {
            Paradise.LOGGER.info("" + ((DigitalOverlay) mc.player).paradise$getPortalOverlayTime());
            paradise$savedOverlayTime = ((DigitalOverlay) mc.player).paradise$getPortalOverlayTime();
        }
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void afterRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ((DigitalOverlay) mc.player).paradise$setPortalOverlayTimeRaw(paradise$savedOverlayTime);
            paradise$savedOverlayTime = 0.0f;
        }
    }

}
