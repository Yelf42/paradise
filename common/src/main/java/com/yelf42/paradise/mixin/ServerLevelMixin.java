package com.yelf42.paradise.mixin;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.dimensions.WhitelistsSavedData;
import com.yelf42.paradise.entities.CrashBolt;
import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Final
    @Shadow
    private MinecraftServer server;

    @Inject(method = "addDuringTeleport", at = @At("HEAD"))
    public void verifyIntruderOnTeleport(Entity entity, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (!self.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) return;

        if (entity instanceof ServerPlayer serverplayer && !(serverplayer.isCreative() || serverplayer.isSpectator())) {
            WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());

            if (!whitelistsSavedData.isWhitelisted(self.dimension().location(), serverplayer.getName().getString())) {
                IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(self);
                intrudersSavedData.add(serverplayer.getUUID());
            }
        }
    }

    @Inject(method = "removePlayerImmediately", at = @At("HEAD"))
    private void removeIntruderOnLeavingDimension(ServerPlayer player, Entity.RemovalReason reason, CallbackInfo ci) {
        if (reason == Entity.RemovalReason.CHANGED_DIMENSION) {
            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(player.serverLevel());
            intrudersSavedData.remove(player.getUUID());
        }
    }

    @Inject(method = "addNewPlayer", at = @At("HEAD"))
    public void verifyIntruderOnLogin(ServerPlayer player, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (!self.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) return;

        ResourceLocation dimId = self.dimension().location();
        IntrudersSavedData intruders = IntrudersSavedData.getOrCreate(self);
        WhitelistsSavedData whitelists = WhitelistsSavedData.getOrCreate(server.overworld());
        if (intruders.isIntruder(player.getUUID()) && whitelists.isWhitelisted(dimId, player.getName().getString())) {
            intruders.remove(player.getUUID());
        }
    }


    @Unique
    private static final AABB DIGITAL_WORLD_AABB = new AABB(-132, 0, -132, 132, 112, 132);
    @Unique
    private static final int TARGET_FISH_POPULATION = 16;
    @Unique
    private int paradise$fishToSpawn = TARGET_FISH_POPULATION;

    @Inject(method = "tickChunk", at = @At("TAIL"))
    public void addFishToDigitalBiome(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        ChunkPos chunkPos = chunk.getPos();
        if (level.getBiome(chunkPos.getWorldPosition()).is(Paradise.identifier("digital"))) {
            if (this.paradise$fishToSpawn > 0 && level.random.nextInt(300) == 0) {
                double a = level.getRandom().nextFloat() * Math.PI * 2.0;
                double r = Math.sqrt(level.getRandom().nextFloat()) * 116.0;

                double x = Mth.clamp(Math.sin(a) * r, -116, 116);
                double z = Mth.clamp(Math.cos(a) * r, -116, 116);

                int maxY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
                double y = Mth.clamp(level.getRandom().nextFloat() * 6.0 - 3.0, 2, maxY + 5);

                DigitalFish digitalFish = ModEntities.DIGITAL_FISH.create(level);
                if (digitalFish != null) {
                    digitalFish.setPos(x, y, z);
                    level.addFreshEntity(digitalFish);
                    this.paradise$fishToSpawn--;
                }
            } else if (level.random.nextInt(10000) == 0) {
                int currentPop = level.getEntitiesOfClass(DigitalFish.class, DIGITAL_WORLD_AABB).size();
                this.paradise$fishToSpawn = Math.max(TARGET_FISH_POPULATION - currentPop, 0);
            }
        }
    }

    @Inject(method = "tickChunk", at = @At("TAIL"))
    public void addCrashBoltsToErrorBiome(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        ChunkPos chunkpos = chunk.getPos();
        if (level.random.nextInt(1000) == 0
                && level.getBiome(chunkpos.getWorldPosition()).is(Paradise.identifier("error"))) {

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
        Player player = level.players().get(level.getRandom().nextInt(level.players().size()));
        if (player != null && level.getRandom().nextInt(160) == 0) {
            blockpos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
        }

        if (blockpos.getY() == level.getMinBuildHeight() - 1) {
            blockpos = blockpos.above(2);
        }

        return blockpos;

    }


}
