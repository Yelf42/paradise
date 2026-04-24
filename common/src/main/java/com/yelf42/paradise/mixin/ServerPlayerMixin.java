package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Final
    @Shadow
    public MinecraftServer server;

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at  = @At("HEAD"), cancellable = true)
    public void digitalWorldRespawn(boolean keepInventory, DimensionTransition.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = (ServerLevel) player.level();

        if (level.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) {

            // Respawn normally if died in nullspace
            if (level.dimensionTypeRegistration().is(
                    ResourceKey.create(Registries.DIMENSION_TYPE,
                            Paradise.identifier("paradise_dimension_error")))) {
                return;
            }

            BlockPos spawnPos = new BlockPos(56, 6, 0);

            cir.setReturnValue(new DimensionTransition(
                    level,
                    Vec3.atBottomCenterOf(spawnPos),
                    Vec3.ZERO,
                    Direction.WEST.toYRot(),
                    player.getXRot(),
                    postDimensionTransition
            ));
        }
    }
}
