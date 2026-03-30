package com.yelf42.paradise.platform.services;

import com.yelf42.paradise.dimensions.DimensionAddedCallback;
import com.yelf42.paradise.dimensions.DimensionRemovedCallback;
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

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    CreativeModeTab.Builder tabBuilder();
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks);
    SimpleParticleType simpleParticleType();

    void registerAddedEvent(DimensionAddedCallback listener);

    void registerRemovedEvent(DimensionRemovedCallback listener);

    void invokeRemovedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level);

    void invokeAddedEvent(@NotNull ResourceKey<Level> key, @NotNull ServerLevel level);
}