package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "sendLevelInfo", at = @At("RETURN"))
    private void onSendLevelInfo(ServerPlayer player, ServerLevel level, CallbackInfo ci) {
        if (level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) {
            player.connection.send(new ClientboundInitializeBorderPacket(level.getWorldBorder()));
        }
    }
}
