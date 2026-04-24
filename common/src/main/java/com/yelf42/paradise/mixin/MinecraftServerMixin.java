package com.yelf42.paradise.mixin;

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

import com.mojang.datafixers.DataFixer;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.dimensions.*;
import com.yelf42.paradise.registry.ModPackets;
import com.yelf42.paradise.registry.RegistryUtil;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements DimensionProvider {
    @Shadow
    @Final
    protected LevelStorageSource.LevelStorageAccess storageSource;
    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    public abstract LayeredRegistryAccess<RegistryLayer> registries();

    @Shadow
    @Nullable
    private String motd;
    @Unique
    private final @NotNull List<ServerLevel> pendingLevels = new ArrayList<>();
    @Unique
    private final @NotNull List<DimensionRemovalTicket> pendingDeletions = new ArrayList<>();
    @Unique
    private DimensionRegistry dynamicDimensions;
    @Unique
    private boolean tickingLevels = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initDynamicDimensions(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        this.dynamicDimensions = new DimensionRegistry((MinecraftServer) (Object) this);
    }

    /**
     * Load dynamic dimensions AFTER all normal levels have been loaded,
     * but still during server load (so that it's not too late).
     * Hopefully hints that these dimension may be removed later.
     */
    @Inject(method = "prepareLevels", at = @At("RETURN"))
    private void loadDynamicDimensions(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        this.dynamicDimensions.loadDynamicDimensions();
        DataServerLocations.getOrCreate(levels.get(Level.OVERWORLD));
        DownloaderLocations.getOrCreate(levels.get(Level.OVERWORLD));
    }

    public ResourceLocation paradise$createIfAbsent(DimensionRegistry.ParadiseType type) {
        ResourceLocation out = Paradise.identifier(paradise$generateDimensionName(type));
        while (this.dynamicDimensions.anyDimensionExists(out)) {
            out = Paradise.identifier(paradise$generateDimensionName(type));
        }
        if (type != DimensionRegistry.ParadiseType.ERROR) {
            this.dynamicDimensions.createIfAbsent(out, type);
        }

        return out;
    }

    @Unique
    private static String paradise$generateRandomChars(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder ();
        Random random = new Random ();
        for (int i = 0; i < length; i ++) {
            sb.append (candidateChars.charAt (random.nextInt (candidateChars
                    .length ())));
        }

        return sb.toString ();
    }
    @Unique
    private static String paradise$generateDimensionName(DimensionRegistry.ParadiseType type) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        switch (type) {
            case DAY -> sb.append(ADJECTIVES_DAY.get(random.nextInt(ADJECTIVES_DAY.size())));
            case NIGHT -> sb.append(ADJECTIVES_NIGHT.get(random.nextInt(ADJECTIVES_NIGHT.size())));
            default -> sb.append(paradise$generateRandomChars("abcdefghijklmnopqrstuvwxyz0123456789", random.nextInt(6, 12)));
        }

        sb.append('_');
        sb.append(PLACES_ALL.get(random.nextInt(PLACES_ALL.size())));

        for (int i = sb.length() - 1; i >=0 ; i--) {
            if (random.nextBoolean()) continue;
            char c = sb.charAt(i);
            if (LEET_MAP.containsKey(c)) sb.setCharAt(i,LEET_MAP.get(c));
        }

        return sb.toString().toLowerCase();
    }
    @Unique
    private static final List<String> PLACES_ALL = List.of(
            "island",
            "isle",
            "islet",
            "enclave",
            "haven",
            "paradise",
            "eden",
            "shelter",
            "sanctuary",
            "refuge",
            "retreat",
            "sanctum",
            "asylum",
            "enclave",
            "reserve",
            "temple",
            "oasis",
            "escape"
    );
    @Unique
    private static final List<String> ADJECTIVES_DAY = List.of(
            "perfect",
            "cozy",
            "wonderful",
            "idyllic",
            "beautiful",
            "dreamy",
            "warm",
            "brilliant",
            "protective",
            "shielding",
            "restful",
            "relaxing",
            "silly",
            "gorgeous",
            "serene",
            "joyous",
            "cheerful",
            "merry",
            "jubilant",
            "blessed"
    );
    @Unique
    private static final List<String> ADJECTIVES_NIGHT = List.of(
            "safe",
            "enduring",
            "gentle",
            "quiet",
            "quaint",
            "peaceful",
            "silent",
            "calm",
            "soft",
            "mellow",
            "still",
            "halcyon",
            "tranquil",
            "starry"
    );
    @Unique
    private static final Map<Character, Character> LEET_MAP = Map.of(
            'a', '4',
            'b', '8',
            'e', '3',
            'g', '9',
            'i', '1',
            'o', '0',
            's', '5',
            't', '7'
    );

    public void paradise$removeLevel(ResourceKey<Level> key, @Nullable PlayerRemover removalMode, boolean removeFiles) {
        if (this.tickingLevels) {
            this.pendingDeletions.add(new DimensionRemovalTicket(key, removalMode, removeFiles));
        } else {
            this.unloadLevel(key, removalMode);
            if (removeFiles) this.paradise$deleteLevelData(key);
        }
    }

    public void paradise$deleteLevelData(ResourceKey<Level> key) {
        Path dimensionPath = this.storageSource.getDimensionPath(key);
        if (dimensionPath.toFile().exists()) {
            try {
                FileUtils.deleteDirectory(dimensionPath.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete deleted level directory!", e);
            }
        }
    }

    @Override
    public boolean paradise$isIdPendingCreation(@NotNull ResourceKey<Level> key) {
        for (ServerLevel pendingLevel : this.pendingLevels) {
            if (pendingLevel.dimension().equals(key)) return true;
        }
        return false;
    }

    @Override
    public void paradise$registerLevel(ServerLevel level) {
        if (this.tickingLevels) {
            this.pendingLevels.add(level); //prevent co-modification
        } else {
            this.registerLevel(level);
        }
    }

    public @NotNull DimensionRegistry paradise$registry() {
        return this.dynamicDimensions;
    }

    @Inject(method = "tickChildren", at = @At(value = "HEAD"))
    private void addLevels(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!this.pendingLevels.isEmpty()) {
            for (ServerLevel level : this.pendingLevels) {
                this.registerLevel(level);
            }
            this.pendingLevels.clear();
        }

        if (!this.pendingDeletions.isEmpty()) {
            for (DimensionRemovalTicket ticket : this.pendingDeletions) {
                this.unloadLevel(ticket.key(), ticket.removalMode());
                if (ticket.removeFiles()) {
                    this.paradise$deleteLevelData(ticket.key());
                }
            }
            this.pendingDeletions.clear();
        }
    }

    @Unique
    private void unloadLevel(ResourceKey<Level> key, PlayerRemover playerRemover) {
        ResourceLocation dimType = null;

        try (ServerLevel level = this.levels.get(key)) {
            if (level == null) {
                Paradise.LOGGER.error("Attempted to unload non-existent level {}", key);
                return;
            }
            DimensionRemovedCallback.invoke(key, level);

            List<ServerPlayer> players = new ArrayList<>(level.players()); // prevent co-modification
            for (ServerPlayer player : players) {
                playerRemover.removePlayer((MinecraftServer) (Object) this, player);
            }

            level.save(null, true, level.noSave);
            dimType = level.dimensionTypeRegistration().unwrapKey().get().location();
        } catch (IOException e) {
            Paradise.LOGGER.error("Failed to close level upon removal! Memory may have been leaked.", e);
        } finally {
            this.levels.remove(key);
        }
        assert dimType != null;

        RegistryUtil.unregister(this.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM), key.location());
        RegistryUtil.unregister(this.registries().compositeAccess().registryOrThrow(Registries.DIMENSION_TYPE), dimType);
        this.dynamicDimensions.remove(key);

        ModPackets.RemoveDimensionPayload payload = new ModPackets.RemoveDimensionPayload(key.location());
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);

        for (ServerPlayer player : this.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }

    @Unique
    private void registerLevel(ServerLevel level) {
        DimensionAddedCallback.invoke(level.dimension(), level);
        this.levels.put(level.dimension(), level);
        this.dynamicDimensions.add(level.dimension());
        level.tick(() -> true);
    }

    @Inject(method = "tickChildren", at = @At(value = "HEAD"))
    private void markTickingLevels(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        this.tickingLevels = true;
    }

    @Inject(method = "tickChildren", at = @At(value = "RETURN"))
    private void markNotTickingLevels(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        this.tickingLevels = false;
    }
}
