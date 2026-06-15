package com.yelf42.paradise.entities;

import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.registry.ModEntities;
import com.yelf42.paradise.registry.ModSounds;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DigitalWatcher extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> FACING =
            SynchedEntityData.defineId(DigitalWatcher.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Vector3f> FACE_TOWARDS =
            SynchedEntityData.defineId(DigitalWatcher.class, EntityDataSerializers.VECTOR3);

    private static final EntityDataAccessor<Integer> ALPHA =
            SynchedEntityData.defineId(DigitalWatcher.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> BLINK =
            SynchedEntityData.defineId(DigitalWatcher.class, EntityDataSerializers.INT);

    private final int MIN_ATTACK_COOLDOWN = 150;
    private final int MAX_ATTACK_COOLDOWN = 200;
    private final int BEAM_OFFSET = 5;
    private int attackCooldown = 150;
    private boolean attacking = false;

    public DigitalWatcher(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FACING, Optional.empty());
        builder.define(FACE_TOWARDS, new Vector3f(0, 0, 0));
        builder.define(ALPHA, 0);
        builder.define(BLINK, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setFacing(compoundTag.contains("facing") ? compoundTag.getUUID("facing") : null);

        if (compoundTag.contains("faceTowardsX") && compoundTag.contains("faceTowardsY") && compoundTag.contains("faceTowardsZ")) {
            setFaceTowards(new Vec3(
                    compoundTag.getDouble("faceTowardsX"),
                    compoundTag.getDouble("faceTowardsY"),
                    compoundTag.getDouble("faceTowardsZ")));
        } else {
            setFaceTowards(Vec3.ZERO);
        }

        setAlpha(compoundTag.getInt("alpha"));
        setBlink(compoundTag.getInt("blink"));

        this.attackCooldown = compoundTag.contains("attackCooldown") ? compoundTag.getInt("attackCooldown") : MIN_ATTACK_COOLDOWN;
        this.attacking = compoundTag.getBoolean("attacking");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        getFacing().ifPresent(uuid -> compoundTag.putUUID("facing", uuid));
        Vec3 faceTowards = getFaceTowardsVec3();
        compoundTag.putDouble("faceTowardsX", faceTowards.x());
        compoundTag.putDouble("faceTowardsY", faceTowards.y());
        compoundTag.putDouble("faceTowardsZ", faceTowards.z());

        compoundTag.putInt("alpha", getAlpha());
        compoundTag.putInt("blink", getBlink());

        compoundTag.putInt("attackCooldown", this.attackCooldown);
        compoundTag.putBoolean("attacking", this.attacking);
    }

    public int getAlpha() {
        return this.entityData.get(ALPHA);
    }
    public void setAlpha(int alpha) {
        this.entityData.set(ALPHA, Math.clamp(alpha, 0, 255));
    }
    public void incAlpha() {
        this.setAlpha(getAlpha() + 4);
    }
    public void decAlpha() {
        this.setAlpha(getAlpha() - 2);
    }

    public int getBlink() {
        return this.entityData.get(BLINK);
    }
    public void setBlink(int blink) {
        this.entityData.set(BLINK, Math.max(blink, 0));
    }
    public void decBlink() {
        this.setBlink(getBlink() - 1);
    }

    public Optional<UUID> getFacing() {
        return this.entityData.get(FACING);
    }
    public void setFacing(UUID uuid) {
        this.entityData.set(FACING, Optional.ofNullable(uuid));
    }

    public Vector3f getFaceTowards() {
        return this.entityData.get(FACE_TOWARDS);
    }
    public Vec3 getFaceTowardsVec3() {
        Vector3f v = getFaceTowards();
        return new Vec3(v.x, v.y, v.z);
    }
    public void setFaceTowards(Vec3 vec) {
        this.entityData.set(FACE_TOWARDS, new Vector3f((float) vec.x, (float) vec.y, (float) vec.z));
    }

    @Override
    public void tick() {
        Level level = this.level();

        long time = level.getGameTime();
        if (this.getAlpha() == 0 && time % 20 != 0) return;

        if (this.getBlink() == 0) {
            if (this.getRandom().nextInt(100) == 0) {
                this.setBlink(6);
            }
        } else {
            this.decBlink();
        }

        if (level instanceof ServerLevel serverLevel) {
            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(serverLevel);
            List<UUID> intruders = intrudersSavedData.getPresentIntruders(serverLevel);
            if (intruders.isEmpty()) {
                if (this.attacking) {
                    this.attacking = false;
                    this.attackCooldown = MAX_ATTACK_COOLDOWN;
                }
                setFaceTowards(getFaceTowardsVec3().lerp(Vec3.ZERO, 0.1));
                decAlpha();
            } else {
                UUID facing = getFacing().orElse(null);
                if (facing == null || serverLevel.getEntity(facing) == null) {
                    UUID target = intruders.get(this.getRandom().nextInt(intruders.size()));
                    setFacing(target);
                    facing = target;
                }

                Vec3 facePos = (facing != null) ? serverLevel.getEntity(facing).position() : Vec3.ZERO;
                setFaceTowards(getFaceTowardsVec3().lerp(facePos, 0.1));
                incAlpha();

                this.attacking = true;
                this.attackCooldown--;
                if (this.attackCooldown <= 0) {
                    this.attackCooldown = this.getRandom().nextInt(MIN_ATTACK_COOLDOWN, MAX_ATTACK_COOLDOWN);
                    triggerAttack(intruders, serverLevel);
                }
            }
        }
    }

    private void triggerAttack(List<UUID> intruders, ServerLevel serverLevel) {
        for (UUID uuid : intruders) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("LEAVE").withStyle(ChatFormatting.DARK_RED)
                ));

                BlockPos playerPos = player.getOnPos();
                Vec3 pos = player.getPosition(0);
                Holder<SoundEvent> holder = Holder.direct(ModSounds.WATCHER_ATTACK);
                player.connection.send(new ClientboundSoundPacket(holder, SoundSource.HOSTILE, pos.x(), pos.y(), pos.z(), 100.0f, 1.0f + 0.05f - serverLevel.getRandom().nextFloat() * 0.1f, serverLevel.getRandom().nextLong()));

                LongOpenHashSet usedPositions = new LongOpenHashSet();
                int bonusAttempts = 5;
                for (int i = 0; i < 5; i++) {
                    int offsetX = (this.getRandom().nextInt(BEAM_OFFSET * 2 + 1)) - BEAM_OFFSET;
                    int offsetZ = (this.getRandom().nextInt(BEAM_OFFSET * 2 + 1)) - BEAM_OFFSET;
                    int beamX = playerPos.getX() + offsetX;
                    int beamZ = playerPos.getZ() + offsetZ;

                    long key = BlockPos.asLong(beamX, 0, beamZ);
                    if (!usedPositions.add(key)) {
                        if (--bonusAttempts > 0) i--;
                        continue;
                    }

                    int blockingY = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, beamX, beamZ);
                    int surfaceY = (playerPos.getY() > blockingY) ? blockingY : findFloorY(serverLevel, beamX, playerPos.getY() + 2, beamZ);
                    if (surfaceY < 0) {
                        if (--bonusAttempts > 0) i--;
                        continue;
                    }

                    DigitalWatcherBeam beam = ModEntities.DIGITAL_WATCHER_BEAM.spawn(serverLevel, new BlockPos(beamX, 0, beamZ), MobSpawnType.TRIGGERED);
                    if (beam != null) {
                        beam.setIndicatorHeight(surfaceY);
                    }
                }
            }
        }
    }

    private int findFloorY(ServerLevel level, int x, int startY, int z) {
        for (int y = startY; y >= Math.max(startY - 6, 0); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isPathfindable(PathComputationType.LAND)) {
                if (level.getBlockState(pos.above()).isPathfindable(PathComputationType.LAND)) {
                    return y + 1;
                }
            }
        }
        return -1;
    }

    @Override
    protected void handlePortal() {
        // NOTHING
    }
    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }
}
