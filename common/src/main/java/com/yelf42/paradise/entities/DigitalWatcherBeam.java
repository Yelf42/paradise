package com.yelf42.paradise.entities;

import com.yelf42.paradise.dimensions.IntrudersSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DigitalWatcherBeam extends Entity {

    protected static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(DigitalWatcherBeam.class, EntityDataSerializers.INT);
    public static final int INDICATE_THRESHOLD = 40;
    private static final int MAX_LIFESPAN = INDICATE_THRESHOLD + 40;

    protected static final EntityDataAccessor<Integer> INDICATOR_HEIGHT = SynchedEntityData.defineId(DigitalWatcherBeam.class, EntityDataSerializers.INT);

    public DigitalWatcherBeam(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setLifespan(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFESPAN, 0);
        builder.define(INDICATOR_HEIGHT, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Lifespan")) {
            this.setLifespan(compound.getInt("Lifespan"));
        }

        if (compound.contains("IndicatorHeight")) {
            this.setIndicatorHeight(compound.getInt("IndicatorHeight"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Lifespan", this.getLifespan());
        compound.putInt("IndicatorHeight", this.getIndicatorHeight());
    }

    public void setLifespan(int lifespan) {
        this.entityData.set(LIFESPAN, lifespan);
    }
    public void incLifespan() {
        this.entityData.set(LIFESPAN, this.getLifespan() + 1);
    }
    public int getLifespan() {
        return this.entityData.get(LIFESPAN);
    }


    public int getIndicatorHeight() {
        return this.entityData.get(INDICATOR_HEIGHT);
    }
    public void setIndicatorHeight(int height) {
        this.entityData.set(INDICATOR_HEIGHT, height);
    }

    @Override
    public void tick() {
        this.incLifespan();
        if (this.getLifespan() >= MAX_LIFESPAN) this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void playerTouch(Player player) {
        if (this.getLifespan() < INDICATE_THRESHOLD) return;

        if (this.level() instanceof ServerLevel serverLevel) {
            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(serverLevel);
            if (intrudersSavedData.isIntruder(player.getUUID())) {
                player.hurt(this.level().damageSources().fellOutOfWorld(), 4.0f);
            }
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }
}
