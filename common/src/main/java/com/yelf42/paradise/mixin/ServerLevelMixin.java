package com.yelf42.paradise.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.IntrudersSavedData;
import com.yelf42.paradise.dimensions.TransitLogSavedData;
import com.yelf42.paradise.dimensions.WhitelistsSavedData;
import com.yelf42.paradise.entities.CrashBolt;
import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.entities.DigitalWatcher;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModEffects;
import com.yelf42.paradise.registry.ModEntities;
import com.yelf42.paradise.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
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

import java.util.UUID;
import java.util.function.BooleanSupplier;

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
            IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(self);
            WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());
            TransitLogSavedData transitLogSavedData = TransitLogSavedData.getOrCreate(self);

            if (!whitelistsSavedData.isWhitelisted(self.dimension().location(), serverplayer.getName().getString())) {
                intrudersSavedData.add(serverplayer.getUUID());
                paradise$extraEjectTest(serverplayer, true);
            }
            transitLogSavedData.addLog(TransitLogSavedData.createLogEntry(intrudersSavedData.isIntruder(serverplayer.getUUID()), serverplayer.getName().getString(), "TELEPORTED IN"));
        }
    }

    @Inject(method = "removePlayerImmediately", at = @At("HEAD"))
    private void removeIntruderOnLeavingDimension(ServerPlayer player, Entity.RemovalReason reason, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (!self.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) return;

        UUID playerUUID = player.getUUID();
        IntrudersSavedData intrudersSavedData = IntrudersSavedData.getOrCreate(player.serverLevel());
        TransitLogSavedData transitLogSavedData = TransitLogSavedData.getOrCreate(self);

        boolean shouldLog = (!(player.isCreative() || player.isSpectator()));

        if (reason == Entity.RemovalReason.CHANGED_DIMENSION) {
            if (shouldLog) transitLogSavedData.addLog(TransitLogSavedData.createLogEntry(intrudersSavedData.isIntruder(playerUUID), player.getName().getString(), "TELEPORTED OUT"));
            intrudersSavedData.remove(playerUUID);
        } else if (reason == Entity.RemovalReason.KILLED) {
            if (shouldLog) transitLogSavedData.addLog(TransitLogSavedData.createLogEntry(intrudersSavedData.isIntruder(playerUUID), player.getName().getString(), "KILLED"));
        } else if (reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            if (shouldLog) transitLogSavedData.addLog(TransitLogSavedData.createLogEntry(intrudersSavedData.isIntruder(playerUUID), player.getName().getString(), "LOGGED OFF"));
        }

    }

    @Inject(method = "addNewPlayer", at = @At("HEAD"))
    public void verifyIntruderOnLogin(ServerPlayer player, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (!self.dimensionTypeRegistration().is(Paradise.PARADISE_DIMENSIONS)) return;

        ResourceLocation dimId = self.dimension().location();
        IntrudersSavedData intruders = IntrudersSavedData.getOrCreate(self);
        WhitelistsSavedData whitelists = WhitelistsSavedData.getOrCreate(server.overworld());
        UUID playerUUID = player.getUUID();
        if (intruders.isIntruder(playerUUID) && whitelists.isWhitelisted(dimId, player.getName().getString())) {
            intruders.remove(playerUUID);
        }

        paradise$extraEjectTest(player, intruders.isIntruder(playerUUID));

        if (!(player.isCreative() || player.isSpectator())) {
            TransitLogSavedData transitLogSavedData = TransitLogSavedData.getOrCreate(self);
            transitLogSavedData.addLog(TransitLogSavedData.createLogEntry(intruders.isIntruder(playerUUID), player.getName().getString(), "LOGGED ON"));
        }
    }

    @Unique
    private void paradise$extraEjectTest(ServerPlayer player, boolean intruder) {
        if (!intruder) return;

        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!mainhand.is(ModItems.SCRAMBLER) && !offhand.is(ModItems.SCRAMBLER) && !player.hasEffect(ModEffects.EJECT)) {
            player.displayClientMessage(Component.translatable("gui.paradise.scrambler.ejection").withStyle(ChatFormatting.RED), true);
            player.addEffect(ModEffects.ejectInstance());
        }
    }

    @Unique
    private boolean paradise$watcherChecked = false;
    @Unique
    private int paradise$watcherCheckDelay = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    public void addWatcherToDigitalDimension(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (paradise$watcherChecked) return;

        ServerLevel level = (ServerLevel) (Object) this;
        ResourceLocation dimId = level.dimension().location();
        if (!dimId.getNamespace().equals(Paradise.MOD_ID) || dimId.getPath().equals("nullspace")) return;

        if (paradise$watcherCheckDelay == -1) {
            level.setChunkForced(0, 0, true);
            paradise$watcherCheckDelay = 0;
            return;
        }

        if (++paradise$watcherCheckDelay < 20) return;

        paradise$watcherChecked = true;
        level.setChunkForced(0, 0, false);

        IntrudersSavedData data = IntrudersSavedData.getOrCreate(level);
        UUID watcherUUID = data.getWatcher();
        if (watcherUUID == null || level.getEntity(watcherUUID) == null) {
            DigitalWatcher watcher = ModEntities.DIGITAL_WATCHER.create(level);
            watcher.setPos(0, -20, 0);
            level.addFreshEntity(watcher);
            data.setWatcher(watcher.getUUID());
        }
    }


    @Unique
    private static final AABB DIGITAL_WORLD_AABB = new AABB(-132, 0, -132, 132, 112, 132);
    @Unique
    private static final int TARGET_FISH_POPULATION = 16;
    @Unique
    private int paradise$fishToSpawn = TARGET_FISH_POPULATION;
    @Unique
    private boolean paradise$tickDigitalFish = true;

    @Inject(method = "tickChunk", at = @At("TAIL"))
    public void addFishToDigitalBiome(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if (!this.paradise$tickDigitalFish) return;

        ServerLevel level = (ServerLevel) (Object) this;
        ResourceLocation dimId = level.dimension().location();
        if (!dimId.getNamespace().equals(Paradise.MOD_ID) || dimId.getPath().equals("nullspace")) {
            this.paradise$tickDigitalFish = false;
            return;
        }

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

    @Unique
    private boolean paradise$tickCrashBolts = true;

    @Inject(method = "tickChunk", at = @At("TAIL"))
    public void addCrashBoltsToErrorBiome(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if (!this.paradise$tickCrashBolts) return;

        ServerLevel level = (ServerLevel) (Object) this;
        ResourceLocation dimId = level.dimension().location();
        if (!dimId.getNamespace().equals(Paradise.MOD_ID) || !dimId.getPath().equals("nullspace")) {
            this.paradise$tickCrashBolts = false;
            return;
        }

        ChunkPos chunkpos = chunk.getPos();
        if (level.random.nextInt(1000) == 0) {

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


//    @ModifyExpressionValue(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
//    private boolean paradise$modifyWeatherCycleCondition(boolean weatherCycleGameRule) {
//        ServerLevel level = (ServerLevel) (Object) this;
//        ResourceLocation dimId = level.dimension().location();
//        if (dimId.getNamespace().equals(Paradise.MOD_ID)) {
//            return false;
//        }
//        return weatherCycleGameRule;
//    }


}
