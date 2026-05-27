package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.config.BunkerStructurePlacement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ModStructurePlacementTypes {

    public static final StructurePlacementType<BunkerStructurePlacement> BUNKERS =
            () -> BunkerStructurePlacement.CODEC;

    public static void register() {
        Registry.register(
                BuiltInRegistries.STRUCTURE_PLACEMENT,
                Paradise.identifier("bunkers"),
                BUNKERS
        );
    }
}
