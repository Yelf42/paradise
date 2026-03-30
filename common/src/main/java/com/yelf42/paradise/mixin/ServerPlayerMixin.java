package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void onChangeDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel newLevel = player.serverLevel();

        if (newLevel.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension")))) {
            player.connection.send(new ClientboundInitializeBorderPacket(newLevel.getWorldBorder()));
        }
    }
}
