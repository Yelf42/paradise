package com.yelf42.paradise.platform;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.DimensionAddedCallback;
import com.yelf42.paradise.dimensions.DimensionRegistry;
import com.yelf42.paradise.dimensions.DimensionRemovedCallback;
import com.yelf42.paradise.platform.services.IPlatformHelper;
import com.mojang.datafixers.DSL;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.BiFunction;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public CreativeModeTab.Builder tabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks) {
        return BlockEntityType.Builder.of(function::apply, validBlocks).build(DSL.remainderType());
    }

    @Override
    public SimpleParticleType simpleParticleType() {
        return new SimpleParticleType(false);
    }

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }

    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Paradise.MOD_ID);

    @Override
    public Holder<MobEffect> registerEffectForHolder(ResourceLocation id, MobEffect t) {
        return MOB_EFFECTS.register(id.getPath(), () -> t);
    }

    @Override
    public void registerAddedEvent(DimensionAddedCallback callback) {
        DimensionRegistry.DIMENSION_ADDED_EVENT.add(callback);
    }

    @Override
    public void registerRemovedEvent(DimensionRemovedCallback callback) {
        DimensionRegistry.DIMENSION_REMOVED_EVENT.add(callback);
    }

    @Override
    public void invokeRemovedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level) {
        for (DimensionRemovedCallback callback : DimensionRegistry.DIMENSION_REMOVED_EVENT) {
            callback.dimensionRemoved(key, level);
        }
    }

    @Override
    public void invokeAddedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level) {
        for (DimensionAddedCallback callback : DimensionRegistry.DIMENSION_ADDED_EVENT) {
            callback.dimensionAdded(key, level);
        }
    }
}