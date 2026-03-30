package com.yelf42.paradise.platform;

import com.yelf42.paradise.dimensions.DimensionAddedCallback;
import com.yelf42.paradise.dimensions.DimensionRemovedCallback;
import com.yelf42.paradise.platform.services.IPlatformHelper;
import com.mojang.datafixers.DSL;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void registerAddedEvent(DimensionAddedCallback listener) {

    }
    @Override
    public void registerRemovedEvent(DimensionRemovedCallback listener) {

    }
    @Override
    public void invokeRemovedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level) {

    }
    @Override
    public void invokeAddedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level) {

    }
}