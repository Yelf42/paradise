package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.entities.CrashBolt;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "tickChunk", at = @At("TAIL"))
    public void addCrashBoltsToNullspace(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (level.random.nextInt(1000) == 0
                && level.dimensionTypeRegistration().is(
                        ResourceKey.create(Registries.DIMENSION_TYPE, Paradise.identifier("paradise_dimension_error")))) {

            ChunkPos chunkpos = chunk.getPos();
            int i = chunkpos.getMinBlockX();
            int j = chunkpos.getMinBlockZ();

            BlockPos blockpos = this.paradise$findBoltTargetAround(level.getBlockRandomPos(i, 0, j, 15), level);

            CrashBolt crashBolt = ModEntities.CRASH_BOLT.create(level);
            if (crashBolt != null) {
                crashBolt.moveTo(Vec3.atBottomCenterOf(blockpos));
                level.addFreshEntity(crashBolt);

                blockpos = blockpos.below();
                BlockState state = level.getBlockState(blockpos);
                if (!state.isAir() && !state.is(Paradise.DIGITAL_BLOCKS)) {
                    level.setBlock(blockpos, ModBlocks.DIGITAL_VOLUME.defaultBlockState(), 3);
                }
            }
        }
    }

    @Unique
    private BlockPos paradise$findBoltTargetAround(BlockPos pos, ServerLevel level) {
        BlockPos blockpos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        Player player = level.getNearestPlayer(0.0, 0.0, 0.0, 256, false);
        if (player != null && level.getRandom().nextInt(40) == 0) {
            blockpos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
        }

        if (blockpos.getY() == level.getMinBuildHeight() - 1) {
            blockpos = blockpos.above(2);
        }

        return blockpos;

    }


}
