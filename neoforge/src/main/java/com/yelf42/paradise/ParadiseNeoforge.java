package com.yelf42.paradise;


import com.yelf42.paradise.blocks.DigitalWhitelistControllerBlockEntity;
import com.yelf42.paradise.client.renderer.ModClientModels;
import com.yelf42.paradise.dimensions.*;
import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.platform.NeoForgePlatformHelper;
import com.yelf42.paradise.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(Paradise.MOD_ID)
public class ParadiseNeoforge {

    public static IEventBus eventBus;

    public ParadiseNeoforge(IEventBus eventBus, Dist dist) {

        ParadiseNeoforge.eventBus = eventBus;

        NeoForgePlatformHelper.register(eventBus);

        bind(Registries.PARTICLE_TYPE, ModParticles::register);

        bind(Registries.BLOCK, ModBlocks::registerBlocks);
        bind(Registries.ITEM, ModBlocks::registerItems);

        bind(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(Registries.ITEM, ModItems::registerItems);
        bind(Registries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(Registries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(Registries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(Registries.ENTITY_TYPE, ModEntities::register);
        eventBus.addListener(this::registerEntityAttributes);

        bind(Registries.CHUNK_GENERATOR, register ->
                register.accept(ParadiseChunkGenerator.CODEC, Paradise.identifier("paradise_generator"))
        );

        ModEffects.init();

        eventBus.addListener(this::setupDispenserBehaviors);
        
        if (dist.isClient()) {
            eventBus.addListener(ParadiseNeoforgeClient::registerBlocks);
            eventBus.addListener(ParadiseNeoforgeClient::registerBlockColors);
            eventBus.addListener(ParadiseNeoforgeClient::registerItemColors);
            eventBus.addListener(ParadiseNeoforgeClient::registerEntityRenderers);
            eventBus.addListener(ParadiseNeoforgeClient::registerParticleFactories);
            eventBus.addListener(ParadiseNeoforgeClient::registerShaders);
        }

        eventBus.addListener(this::registerPayloadHandlers);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        NeoForge.EVENT_BUS.addListener(this::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(this::onLevelUnload);

        Paradise.init();

    }

    public static <T> void bind(ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        eventBus.addListener((Consumer<RegisterEvent>) event -> {
            if (registry.equals(event.getRegistryKey())) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    public void setupDispenserBehaviors(FMLCommonSetupEvent event) {
        event.enqueueWork(ModDispenserBehaviours::registerDispenserBehavior);
    }

    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ResourceKey<Level> key = level.dimension();
            for (DimensionAddedCallback callback : DimensionRegistry.DIMENSION_ADDED_EVENT) {
                callback.dimensionAdded(key, level);
            }
        }
    }

    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ResourceKey<Level> key = level.dimension();
            for (DimensionRemovedCallback callback : DimensionRegistry.DIMENSION_REMOVED_EVENT) {
                callback.dimensionRemoved(key, level);
            }
        }
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DIGITAL_FISH, DigitalFish.createAttributes().build());
    }

    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    public void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Paradise.MOD_ID);

        registrar.playToClient(ModPackets.CreateDimensionPayload.ID, ModPackets.CreateDimensionPayload.CODEC, ParadiseNeoforgeClient.ClientPayloadHandler::handleCreateDimension);
        registrar.playToClient(ModPackets.RemoveDimensionPayload.ID, ModPackets.RemoveDimensionPayload.CODEC, ParadiseNeoforgeClient.ClientPayloadHandler::handleRemoveDimension);

        registrar.playToClient(ModPackets.OpenTransitLogPayload.ID, ModPackets.OpenTransitLogPayload.CODEC, ParadiseNeoforgeClient.ClientPayloadHandler::handleOpenTransitLog);

        registrar.playToClient(ModPackets.OpenWhitelistPayload.ID, ModPackets.OpenWhitelistPayload.CODEC, ParadiseNeoforgeClient.ClientPayloadHandler::handleOpenWhitelist);

        registrar.playToServer(ModPackets.MutateWhitelistPayload.ID, ModPackets.MutateWhitelistPayload.CODEC, ServerPayloadHandler::handleMutateWhitelist);
        registrar.playToServer(ModPackets.CloseWhitelistPayload.ID, ModPackets.CloseWhitelistPayload.CODEC, ServerPayloadHandler::handleCloseWhitelist);
    }

    public static class ServerPayloadHandler {
        public static void handleMutateWhitelist(ModPackets.MutateWhitelistPayload payload, IPayloadContext context) {
            ResourceLocation dimId = payload.dimensionId();
            ModPackets.MutateWhitelistPayload.Action mutation = payload.action();
            String playerName = payload.playerName();

            context.enqueueWork(() -> {
                MinecraftServer server = context.player().getServer();
                WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(server.overworld());
                switch (mutation) {
                    case ADD:
                        whitelistsSavedData.addPlayer(dimId, playerName);
                        removeIntruderIfPresent(server, dimId, playerName);
                        break;
                    case REMOVE:
                        whitelistsSavedData.removePlayer(dimId, playerName);
                        if (whitelistsSavedData.getActive(dimId).isEmpty()) {
                            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimId));
                            if (level == null) return;
                            IntrudersSavedData intruders = IntrudersSavedData.getOrCreate(level);
                            intruders.clear();
                        }
                        break;
                    case FLIP:
                        if (whitelistsSavedData.flipPlayer(dimId, playerName)) {
                            removeIntruderIfPresent(server, dimId, playerName);
                        }
                        break;
                    default:
                        Paradise.LOGGER.warn("Illegal MutateWhitelist Action");
                }
            });
        }

        public static void handleCloseWhitelist(ModPackets.CloseWhitelistPayload payload, IPayloadContext context) {
            BlockPos pos = payload.pos();

            context.enqueueWork(() -> {
                BlockEntity blockEntity = context.player().level().getBlockEntity(pos);
                if (blockEntity instanceof DigitalWhitelistControllerBlockEntity controller) {
                    controller.setAllowedPlayerEditor(null);
                }
            });
        }

        private static void removeIntruderIfPresent(MinecraftServer server, ResourceLocation dimId, String playerName) {
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimId));
            if (level == null) return;
            IntrudersSavedData intruders = IntrudersSavedData.getOrCreate(level);
            server.getPlayerList().getPlayers().stream()
                    .filter(p -> p.getName().getString().equals(playerName))
                    .findFirst()
                    .ifPresent(p -> intruders.remove(p.getUUID()));
        }
    }
}