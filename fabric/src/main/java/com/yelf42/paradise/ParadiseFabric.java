package com.yelf42.paradise;

import com.yelf42.paradise.blocks.DigitalWhitelistControllerBlockEntity;
import com.yelf42.paradise.dimensions.*;
import com.yelf42.paradise.entities.DigitalFish;
import com.yelf42.paradise.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

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

        ModEffects.init();

        ModDispenserBehaviours.registerDispenserBehavior();

        if (FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1")) {
            registerFabricEventListeners();
        }

        PayloadTypeRegistry.playS2C().register(ModPackets.CreateDimensionPayload.ID, ModPackets.CreateDimensionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPackets.RemoveDimensionPayload.ID, ModPackets.RemoveDimensionPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ModPackets.OpenWhitelistPayload.ID, ModPackets.OpenWhitelistPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPackets.MutateWhitelistPayload.ID, ModPackets.MutateWhitelistPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPackets.CloseWhitelistPayload.ID, ModPackets.CloseWhitelistPayload.CODEC);
        registerC2SPackets();

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

    private void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.MutateWhitelistPayload.ID, (payload, context) -> {
            ResourceLocation dimId = payload.dimensionId();
            ModPackets.MutateWhitelistPayload.Action mutation = payload.action();
            String playerName = payload.playerName();
            context.server().execute(() -> {
                WhitelistsSavedData whitelistsSavedData = WhitelistsSavedData.getOrCreate(context.server().overworld());
                switch(mutation) {
                    case ADD:
                        whitelistsSavedData.addPlayer(dimId, playerName);
                        removeIntruderIfPresent(context.server(), dimId, playerName);
                        break;
                    case REMOVE:
                        whitelistsSavedData.removePlayer(dimId, playerName);
                        break;
                    case FLIP:
                        if (whitelistsSavedData.flipPlayer(dimId, playerName)) {
                            removeIntruderIfPresent(context.server(), dimId, playerName);
                        }
                        break;
                    default:
                        Paradise.LOGGER.warn("Illegal MutateWhitelist Action");
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.CloseWhitelistPayload.ID, (payload, context) -> {
            BlockPos pos = payload.pos();
            context.server().execute(() -> {
                BlockEntity blockEntity = context.player().serverLevel().getBlockEntity(pos);
                if (blockEntity instanceof DigitalWhitelistControllerBlockEntity controller) {
                    controller.setAllowedPlayerEditor(null);
                }
            });
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
