package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.config.BunkerStructurePlacement;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.function.BiConsumer;

public class ModStructurePlacementTypes {

    public static final StructurePlacementType<BunkerStructurePlacement> BUNKERS =
            () -> BunkerStructurePlacement.CODEC;

    /// BINDER
    public static void register(BiConsumer<StructurePlacementType<?>, ResourceLocation> consumer) {
        consumer.accept(BUNKERS, Paradise.identifier("bunkers"));
    }
}
