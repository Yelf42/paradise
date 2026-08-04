package com.yelf42.paradise.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModStructurePlacementTypes;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.placement.*;

import java.util.Optional;

public class BunkerStructurePlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<BunkerStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance).and(
                    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.TRIANGULAR)
                            .forGetter(RandomSpreadStructurePlacement::spreadType)
            ).apply(instance, BunkerStructurePlacement::new)
    );

    public BunkerStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod freqMethod, float freq,
                                        int salt, Optional<ExclusionZone> exclusionZone,
                                        RandomSpreadType spreadType) {
        super(locateOffset, freqMethod, freq, salt, exclusionZone,
                Paradise.CONFIG.bunkerSpacing, Paradise.CONFIG.bunkerSeparation, spreadType);
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacementTypes.BUNKERS;
    }
}
