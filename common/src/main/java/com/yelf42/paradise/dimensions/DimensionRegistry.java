package com.yelf42.paradise.dimensions;

/*
 * Copyright (c) 2021-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.google.common.collect.ImmutableList;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.mixin.*;
import com.yelf42.paradise.registry.ModPackets;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DimensionRegistry {
    private final @NotNull List<ResourceKey<Level>> dynamicDimensions;
    private final MinecraftServer server;
    private final Holder<DimensionType> typeHolderDay;
    private final Holder<DimensionType> typeHolderNight;
    private final Holder<DimensionType> typeHolderError;

    private ParadiseDimensionSavedData savedData;

    public DimensionRegistry(MinecraftServer server) {
        this.server = server;
        this.dynamicDimensions = ((PrimaryLevelDataAccessor) server.getWorldData()).getDynamicDimensions();

        this.typeHolderDay = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension"))
        );

        this.typeHolderNight = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension_night"))
        );

        this.typeHolderError = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(
                ResourceKey.create(Registries.DIMENSION_TYPE,
                        Paradise.identifier("paradise_dimension_error"))
        );

    }

    public void loadDynamicDimensions() {
        this.savedData = ParadiseDimensionSavedData.getOrCreate(server.overworld());

        for (Map.Entry<ResourceLocation, ParadiseType> pair : this.savedData.getDimensions().entrySet()) {
            Paradise.LOGGER.debug("Loading dynamic dimension {}", pair.getKey());
            ServerLevel level = this.createDynamicLevel(ResourceKey.create(Registries.DIMENSION, pair.getKey()), pair.getValue());
            applyWorldBorder(level);
        }

        // Static dimensions:
        ServerLevel level = this.createDynamicLevel(ResourceKey.create(Registries.DIMENSION, Paradise.identifier("nullspace")), ParadiseType.ERROR);
        applyWorldBorder(level);

        Paradise.LOGGER.info("Loaded {} dynamic dimensions", this.dynamicDimensions.size());
    }

    public void createIfAbsent(ResourceLocation id, ParadiseType type) {
        if (!this.savedData.containsDimension(id)) {
            ServerLevel level = this.createDynamicLevel(id, false, type);
            applyWorldBorder(level);
            this.savedData.addDimension(id, type);
            Paradise.LOGGER.debug("Created new dimension {}", id);
        }
    }

    public boolean dynamicDimensionExists(@NotNull ResourceKey<Level> key) {
        return this.dynamicDimensions.contains(key) || ((DimensionProvider) this.server).paradise$isIdPendingCreation(key);
    }

    public boolean anyDimensionExists(@NotNull ResourceLocation id) {
        return this.server.levelKeys().contains(ResourceKey.create(Registries.DIMENSION, id));
    }


    public boolean canDeleteDimension(@NotNull ResourceKey<Level> key) {
        return this.dynamicDimensionExists(key);
    }


    public boolean canCreateDimension(@NotNull ResourceLocation id) {
        return !this.anyDimensionExists(id) && !this.dynamicDimensionExists(ResourceKey.create(Registries.DIMENSION, id));
    }


    public boolean deleteDynamicDimension(@NotNull ResourceLocation id, @Nullable PlayerRemover remover) {
        if (remover == null) remover = PlayerRemover.DEFAULT;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
        if (!this.canDeleteDimension(key)) return false;

        ((DimensionProvider) this.server).paradise$removeLevel(key, remover, true);
        this.savedData.deleteDimension(id);

        return true;
    }

    public @Nullable ServerLevel createDynamicLevel(@NotNull ResourceLocation id, boolean deleteData,  ParadiseType type) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
        if (!this.canCreateDimension(id)) return null;
        Paradise.LOGGER.debug("Attempting to create Paradise dimension '{}'", id);

        if (deleteData) ((DimensionProvider) this.server).paradise$deleteLevelData(key);
        return this.createDynamicLevel(key, type);
    }

    private @NotNull ServerLevel createDynamicLevel(ResourceKey<Level> key, ParadiseType type) {
        LevelStem stem = new LevelStem(getTypeHolder(type),  getChunkGenerator(type, key));
        return this.createDynamicLevel(key, this.server.getWorldData(), stem, this.server.overworld());
    }

    private @NotNull ServerLevel createDynamicLevel(ResourceKey<Level> key, WorldData worldData, LevelStem stem, ServerLevel overworld) {
        // -- start createLevels --
        final DerivedLevelData data = new DerivedLevelData(worldData, worldData.overworldData());
        final ServerLevel level = new ServerLevel(
                this.server,
                ((MinecraftServerAccessor) this.server).getExecutor(),
                ((MinecraftServerAccessor) this.server).getStorageSource(),
                data,
                key,
                stem,
                ((MinecraftServerAccessor) this.server).getProgressListenerFactory().create(10),
                worldData.isDebugWorld(),
                BiomeManager.obfuscateSeed(worldData.worldGenOptions().seed()),
                ImmutableList.of(),
                false,
                null
        );
        // -- end createLevels --

        // see PlayerList
        level.getChunkSource().setSimulationDistance(((DistanceManagerAccessor) ((ServerChunkCacheAccessor) overworld.getChunkSource()).getDistanceManager()).getSimulationDistance());
        level.getChunkSource().setViewDistance(((ChunkMapAccessor) overworld.getChunkSource().chunkMap).getViewDistance());

        // -- start prepareLevels --
        ForcedChunksSavedData forcedChunksSavedData = level.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
        if (forcedChunksSavedData != null) {
            LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();

            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                ChunkPos chunkPos = new ChunkPos(l);
                level.getChunkSource().updateChunkForced(chunkPos, true);
            }
        }

        level.setSpawnSettings(false, false);

        // -- end prepareLevels --

        ((DimensionProvider) this.server).paradise$registerLevel(level);

        ModPackets.CreateDimensionPayload payload = new ModPackets.CreateDimensionPayload(key.location(), stem.type());
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);

        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }

        return level;
    }

    public void remove(ResourceKey<Level> key) {
        this.dynamicDimensions.remove(key);
    }

    public void add(ResourceKey<Level> key) {
        this.dynamicDimensions.add(key);
    }

    private void applyWorldBorder(ServerLevel level) {
        if (level == null) return;

        WorldBorder border = level.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(256);
        border.setAbsoluteMaxSize(256);
        border.setWarningBlocks(0);
        border.setWarningTime(0);
        border.setDamageSafeZone(0);
        border.setDamagePerBlock(0);

        ((ServerLevelData) level.getLevelData()).setWorldBorder(border.createSettings());
    }

    @Contract(value = "_ -> param1", pure = true)
    public static @NotNull DimensionRegistry from(@NotNull MinecraftServer server) {
        return ((DimensionProvider) server).paradise$registry();
    }

    public enum ParadiseType {
        DAY,
        NIGHT,
        ERROR
    }

    private Holder<DimensionType> getTypeHolder(ParadiseType type) {
        return switch (type) {
            case DAY -> this.typeHolderDay;
            case NIGHT -> this.typeHolderNight;
            default -> this.typeHolderError;
        };
    }

    private ChunkGenerator getChunkGenerator(ParadiseType type, ResourceKey<Level> key) {
        return switch (type) {
            case ERROR -> new ErrorChunkGenerator(server.registryAccess(), key);
            default -> new ParadiseChunkGenerator(server.registryAccess(), key);
        };
    }
}
