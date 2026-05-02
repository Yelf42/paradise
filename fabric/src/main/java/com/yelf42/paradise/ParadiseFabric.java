package com.yelf42.paradise;

import com.yelf42.paradise.dimensions.DimensionAddedCallback;
import com.yelf42.paradise.dimensions.DimensionRemovedCallback;
import com.yelf42.paradise.dimensions.ParadiseChunkGenerator;
import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO change icon.png
public class ParadiseFabric implements ModInitializer {

    public static final Event<DimensionAddedCallback> DIMENSION_ADDED_EVENT = EventFactory.createArrayBacked(DimensionAddedCallback.class, t -> (key, level) -> {
        for (DimensionAddedCallback callback : t) {
            callback.dimensionAdded(key, level);
        }
    });
    public static final Event<DimensionRemovedCallback> DIMENSION_REMOVED_EVENT = EventFactory.createArrayBacked(DimensionRemovedCallback.class, t -> (key, level) -> {
        for (DimensionRemovedCallback callback : t) {
            callback.dimensionRemoved(key, level);
        }
    });

    @Override
    public void onInitialize() {

        bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);

        bind(BuiltInRegistries.BLOCK, ModBlocks::registerBlocks);
        bind(BuiltInRegistries.ITEM, ModBlocks::registerItems);

        bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(BuiltInRegistries.ITEM, ModItems::registerItems);
        bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(BuiltInRegistries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(BuiltInRegistries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
        registerEntityAttributes();

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR,
                Paradise.identifier("paradise_generator"),
                ParadiseChunkGenerator.CODEC);

        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            registerFabricEventListeners();
        }

        PayloadTypeRegistry.playS2C().register(ModPackets.CreateDimensionPayload.ID, ModPackets.CreateDimensionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPackets.RemoveDimensionPayload.ID, ModPackets.RemoveDimensionPayload.CODEC);

        if (FabricLoader.getInstance().isModLoaded("fabric-command-api-v2")) {
            CommandRegistrationCallback.EVENT.register(ModCommands::register);
        }

        Paradise.init();
    }

    public static <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        source.accept((t, rl) -> Registry.register(registry, rl, t));
    }

    private static void registerFabricEventListeners() {
        DIMENSION_ADDED_EVENT.register((key, level) -> ServerWorldEvents.LOAD.invoker().onWorldLoad(level.getServer(), level));
        DIMENSION_REMOVED_EVENT.register((key, level) -> ServerWorldEvents.UNLOAD.invoker().onWorldUnload(level.getServer(), level));
    }

    private void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(ModEntities.DIGITAL_FISH, DigitalFish.createAttributes());
    }
}
