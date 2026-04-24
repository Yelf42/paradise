package com.yelf42.paradise.entities;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DataServerLocations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CrashBolt extends Entity {

    private int life;
    public long seed;
    private int flashes;

    public CrashBolt(EntityType<? extends CrashBolt> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.life = 2;
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(10) + 10;
    }

    public SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    public void tick() {
        super.tick();
        if (this.life >= 2) {
            if (this.level().isClientSide()) {
                // TODO sfx
            }
        }

        --this.life;
        if (this.life < 0) {
            if (this.flashes == 0) {
                this.discard();
            } else if (this.life < -this.random.nextInt(10)) {
                --this.flashes;
                this.life = 1;
                this.seed = this.random.nextLong();
            }
        }

        if (this.life >= 0) {
            if (!(this.level() instanceof ServerLevel serverLevel)) {
                this.level().setSkyFlashTime(2);
            } else {
                List<Entity> list1 = this.level().getEntities(this, new AABB(this.getX() - (double)3.0F, this.getY() - (double)3.0F, this.getZ() - (double)3.0F, this.getX() + (double)3.0F, this.getY() + (double)6.0F + (double)3.0F, this.getZ() + (double)3.0F), Entity::isAlive);

                for(Entity entity : list1) {
                    teleportOut(entity, serverLevel);
                }
            }
        }
    }

    private void teleportOut(Entity entity, ServerLevel serverLevel) {
        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);

        DataServerLocations dsl = DataServerLocations.getOrCreate(overworld);
        Pair<BlockPos, ResourceLocation> pair = dsl.get(Paradise.identifier("nullspace"));

        if (pair == null) {
            teleportToSpawn(entity, serverLevel);
        } else {
            ServerLevel destination = serverLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, pair.getRight()));
            if (destination == null || pair.getLeft() == null) {
                teleportToSpawn(entity, serverLevel);
            } else {
                teleportToDataServer(entity, pair.getLeft(), destination);
            }
        }
    }

    private void teleportToDataServer(Entity entity, BlockPos serverLocation, ServerLevel serverlevel) {
        // Clear target location:
        ChunkPos chunkPos = new ChunkPos(serverLocation);
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, true);
        for (int i = 0; i <= 1; i++) {
            BlockPos target = serverLocation.north().above(i);
            Block.dropResources(serverlevel.getBlockState(target), serverlevel, target, serverlevel.getBlockEntity(target));
            serverlevel.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        }
        serverlevel.setChunkForced(chunkPos.x, chunkPos.z, false);

        Vec3 vec3 = serverLocation.north().getBottomCenter();
        float f = Direction.NORTH.toYRot();
        entity.changeDimension(new DimensionTransition(serverlevel, vec3, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET)));
    }

    private void teleportToSpawn(Entity entity, ServerLevel serverLevel) {
        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
        BlockPos pos = overworld.getSharedSpawnPos();
        float f = entity.getYRot();
        Vec3 vec3 = entity.adjustSpawnLocation(overworld, pos).getBottomCenter();
        DimensionTransition transition = new DimensionTransition(overworld, vec3, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.DO_NOTHING);

        if (entity instanceof ServerPlayer serverplayer) {
            transition =  serverplayer.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
        }

        entity.changeDimension(transition);
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = (double)64.0F * getViewScale();
        return distance < d0 * d0;
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
    }

}