package com.yelf42.paradise.platform;

import com.yelf42.paradise.ParadiseFabric;
import com.yelf42.paradise.dimensions.DimensionAddedCallback;
import com.yelf42.paradise.dimensions.DimensionRemovedCallback;
import com.yelf42.paradise.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

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
    public void registerAddedEvent(DimensionAddedCallback callback) {
        ParadiseFabric.DIMENSION_ADDED_EVENT.register(callback);
    }
    @Override
    public void registerRemovedEvent(DimensionRemovedCallback callback) {
        ParadiseFabric.DIMENSION_REMOVED_EVENT.register(callback);
    }
    @Override
    public void invokeRemovedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level) {
        ParadiseFabric.DIMENSION_REMOVED_EVENT.invoker().dimensionRemoved(key, level);
    }
    @Override
    public void invokeAddedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level) {
        ParadiseFabric.DIMENSION_ADDED_EVENT.invoker().dimensionAdded(key, level);
    }
}
