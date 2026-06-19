package com.yelf42.paradise.platform;

import com.yelf42.paradise.ParadiseFabric;
import com.yelf42.paradise.dimensions.DimensionAddedCallback;
import com.yelf42.paradise.dimensions.DimensionRegistry;
import com.yelf42.paradise.dimensions.DimensionRemovedCallback;
import com.yelf42.paradise.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.BiFunction;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public CreativeModeTab.Builder tabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks) {
        return BlockEntityType.Builder.of(function::apply, validBlocks).build();
    }

    @Override
    public SimpleParticleType simpleParticleType() {
        return FabricParticleTypes.simple();
    }

    @Override
    public Holder<MobEffect> registerEffectForHolder(ResourceLocation id, MobEffect t) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id, t);
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
