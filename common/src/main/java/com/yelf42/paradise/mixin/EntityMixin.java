package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    private boolean onGround;
    @Shadow
    private Vec3 position;
    @Shadow
    private BlockPos blockPosition;
    @Shadow
    private Level level;
    @Shadow
    public float moveDist;
    @Shadow
    public float walkDistO;

    @Shadow
    public abstract BlockState getBlockStateOn();

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void walkOnDigitalBarrier(CallbackInfo ci) {
        if (position.y > 1.5 || position.y < 1.0) return;

        long i = level.getGameTime() % 2;
        if (i == 0) return;

        if (moveDist == walkDistO) return;
        if (!onGround) return;
        if (!getBlockStateOn().is(ModBlocks.DIGITAL_BARRIER)) return;

        if (!level.getBiome(blockPosition).is(Paradise.identifier("digital"))) {
            if (!level.getBiome(blockPosition).is(Paradise.identifier("error"))) return;

            level.addParticle(
                    ModParticles.ERROR_RIPPLE,
                    position.x + (level.random.nextDouble() - 0.5) * 0.5,
                    1.01,
                    position.z + (level.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0
            );

            return;
        }

        boolean day = (level.dimensionTypeRegistration().is(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension"))));

        level.addParticle(
                day ? ModParticles.DAY_RIPPLE : ModParticles.NIGHT_RIPPLE,
                position.x + (level.random.nextDouble() - 0.5) * 0.5,
                1.01,
                position.z + (level.random.nextDouble() - 0.5) * 0.5,
                0, 0, 0
        );
    }

}
