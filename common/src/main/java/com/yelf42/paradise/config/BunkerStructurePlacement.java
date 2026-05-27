package com.yelf42.paradise.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModStructurePlacementTypes;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class BunkerStructurePlacement extends ConcentricRingsStructurePlacement {

    public static final MapCodec<BunkerStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance).and(
                    RegistryCodecs.homogeneousList(Registries.BIOME)
                            .fieldOf("preferred_biomes")
                            .forGetter(ConcentricRingsStructurePlacement::preferredBiomes)
            ).apply(instance, BunkerStructurePlacement::new)
    );

    public BunkerStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod freqMethod, float freq,
                                    int salt, Optional<ExclusionZone> exclusionZone, HolderSet<Biome> preferredBiomes) {
        super(locateOffset, freqMethod, freq, salt, exclusionZone,
                Paradise.CONFIG.bunkerDistance, Paradise.CONFIG.bunkerSpread, Paradise.CONFIG.bunkerCount, preferredBiomes);
    }

    public StructurePlacementType<?> type() {
        return ModStructurePlacementTypes.BUNKERS;
    }
}
