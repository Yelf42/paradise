package com.yelf42.paradise.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumSet;
import java.util.UUID;

public class DigitalFish extends Mob {

    private static final int FADE_LIFESPAN = 200;
    protected static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(DigitalFish.class, EntityDataSerializers.INT);

    private UUID attacker;

    public DigitalFish(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FishMoveControl(this);
        this.entityData.set(LIFESPAN, 2400);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, Double.MAX_VALUE);
    }

    public UUID getAttacker() {
        return attacker;
    }

    public void setAttacker(UUID attacker) {
        this.attacker = attacker;
    }

    public void setLifespan(int lifespan) {
        this.entityData.set(LIFESPAN, lifespan);
    }

    public void decLifespan() {
        this.entityData.set(LIFESPAN, this.getLifespan() - 1);
    }

    public int getLifespan() {
        return this.entityData.get(LIFESPAN);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LIFESPAN, 0);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Lifespan")) {
            this.setLifespan(compound.getInt("Lifespan"));
        }
        if (compound.contains("Attacker")) {
            this.setAttacker(compound.getUUID("Attacker"));
        } else {
            this.setAttacker(null);
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Lifespan", this.getLifespan());

        if (this.getAttacker() != null) {
            compound.putUUID("Attacker", this.getAttacker());
        }
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DigitalFishFlee(this));
        this.goalSelector.addGoal(1, new DigitalFishWander(this));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return super.canBeHitByProjectile();
    }

    @Override
    public boolean isPushable() {
        return super.isPushable();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public float getLifespanFade() {
        return Math.clamp(Math.min((2400 - (float) this.getLifespan()) / FADE_LIFESPAN, (float) this.getLifespan() / FADE_LIFESPAN), 0.f, 1.f);
    }

    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);

        if (!this.level().isClientSide()) {
            decLifespan();
            if (this.getLifespan() <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getY() <= -1.0) this.remove(RemovalReason.DISCARDED);
    }

    private static class DigitalFishWander extends Goal {
        private final DigitalFish fish;
        private final int interval;
        private Vec3 target;

        public DigitalFishWander(DigitalFish fish) {
            this.fish = fish;
            this.interval = 40;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return fish.getAttacker() == null && fish.getRandom().nextInt(interval) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null || fish.getAttacker() != null) return false;
            return fish.position().distanceTo(target) > 1.0;
        }

        @Override
        public void start() {

            int attempts = 5;
            Vec3 tempTarget = randomPos();
            while (attempts > 0 && collisionCheck(new BlockPos((int) tempTarget.x(), (int) tempTarget.y(), (int) tempTarget.z()))) {
                tempTarget = randomPos();
                attempts--;
            }

            target = tempTarget;
            fish.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.2 + fish.getRandom().nextFloat() * 0.3);
        }

        private Vec3 randomPos() {
            double a = fish.getRandom().nextFloat() * Math.PI * 2.0;
            double r = fish.getRandom().nextFloat() * 24.0;

            double x = Mth.clamp(fish.getX() + Math.sin(a) * r, -132, 132);
            double z = Mth.clamp(fish.getZ() + Math.cos(a) * r, -132, 132);

            int maxY = fish.level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            double y = Mth.clamp(fish.getY() + fish.getRandom().nextFloat() * 6.0 - 3.0, -4, maxY + 5);

            return new Vec3(x, y, z);
        }

        private boolean collisionCheck(BlockPos pos) {
            BlockState toCheck = fish.level().getBlockState(pos);
            if (toCheck.isAir()) return false;
            VoxelShape shape = toCheck.getCollisionShape(fish.level(), pos);
            return !(shape.isEmpty()) && !(shape.bounds().getSize() < 0.75);
        }

        @Override
        public void stop() {
            target = null;
        }
    }

    private static class DigitalFishFlee extends Goal {
        private final DigitalFish fish;
        private Vec3 target;

        public DigitalFishFlee(DigitalFish fish) {
            this.fish = fish;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return fish.getAttacker() != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null) return false;
            return fish.position().distanceTo(target) > 1.0;
        }

        @Override
        public void start() {
            if (fish.getAttacker() == null) {
                stop();
                return;
            }

            if (fish.level() instanceof ServerLevel serverLevel) {
                Entity attacker = serverLevel.getEntity(fish.getAttacker());
                if (attacker != null) {
                    target = fish.position().add(fish.position().subtract(attacker.position()).normalize().multiply(
                                            fish.getRandom().triangle(14f, 4f),
                                            fish.getRandom().triangle(14f, 4f),
                                            fish.getRandom().triangle(14f, 4f)));

                    fish.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.5 + fish.getRandom().nextFloat() * 0.6);
                } else {
                    stop();
                }
            }
        }

        @Override
        public void stop() {
            fish.setAttacker(null);
            target = null;
        }
    }

    static class FishMoveControl extends MoveControl {
        private final DigitalFish fish;
        private Vec3 smoothMotion = Vec3.ZERO;


        FishMoveControl(DigitalFish fish) {
            super(fish);
            this.fish = fish;
        }

        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                double d0 = this.wantedX - this.fish.getX();
                double d1 = this.wantedY - this.fish.getY();
                double d2 = this.wantedZ - this.fish.getZ();
                double dist = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                if (dist < 0.5) {
                    this.operation = Operation.WAIT;
                    return;
                }

                float speed = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                Vec3 desiredMotion = new Vec3(d0, d1, d2).normalize().scale(speed);

                smoothMotion = smoothMotion.lerp(desiredMotion, 0.05);
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().lerp(smoothMotion, 0.15));

                float yaw = (float)(Mth.atan2(d0, d2) * 180.0 / Math.PI) * -1.0f;
                this.fish.setYRot(this.rotlerp(this.fish.getYRot(), yaw, 5.0f));
                this.fish.yBodyRot = this.fish.getYRot();
            } else {
                smoothMotion = smoothMotion.lerp(Vec3.ZERO, 0.05);
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().lerp(smoothMotion, 0.15));
            }
        }
    }
}
