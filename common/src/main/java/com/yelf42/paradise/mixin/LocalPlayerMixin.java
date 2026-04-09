package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.blocks.DataReaderBlock;
import com.yelf42.paradise.blocks.DigitalUploaderBlock;
import com.yelf42.paradise.blocks.EmergencyExitBlock;
import com.yelf42.paradise.dimensions.DigitalOverlay;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Portal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements DigitalOverlay {

    @Unique
    private float paradise$portalOverlayTime = 0.0f;

    @Override
    public float paradise$getPortalOverlayTime() {
        return paradise$portalOverlayTime;
    }

    @Override
    public void paradise$setPortalOverlayTime() {
        Player player = (Player)(Object)this;
        if (player.portalProcess == null) {
            if (this.paradise$portalOverlayTime  <= 0.0f) return;

            this.paradise$portalOverlayTime = (Math.max(this.paradise$portalOverlayTime - 0.06f, 0.0f));
            return;
        }

        Portal portal = ((PortalProcessorAccessor)player.portalProcess).paradise$getPortal();
        boolean inPortal = (portal instanceof DigitalUploaderBlock) || (portal instanceof DataReaderBlock) || (portal instanceof EmergencyExitBlock);
        if (inPortal && player.portalProcess.isInsidePortalThisTick()) {
            this.paradise$portalOverlayTime = (Math.min(this.paradise$portalOverlayTime + 0.025f, 1.0f));
        } else {
            this.paradise$portalOverlayTime = (Math.max(this.paradise$portalOverlayTime - 0.06f, 0.0f));
        }
    }

    @Override
    public void paradise$setPortalOverlayTimeRaw(float time) {
        this.paradise$portalOverlayTime = time;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        paradise$setPortalOverlayTime();
    }

}
