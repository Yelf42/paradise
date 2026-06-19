package com.yelf42.paradise;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.client.gui.screens.TransitLogScreen;
import com.yelf42.paradise.client.gui.screens.WhitelistScreen;
import com.yelf42.paradise.client.particle.DigitalParticle;
import com.yelf42.paradise.client.particle.RippleParticle;
import com.yelf42.paradise.client.renderer.ModClientModels;
import com.yelf42.paradise.client.renderer.blockentity.*;
import com.yelf42.paradise.client.renderer.entity.*;
import com.yelf42.paradise.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParadiseNeoforgeClient {
    @SubscribeEvent
    public static void registerBlocks(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_GRASS_BLOCK, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_GRASS_BARRIER, RenderType.solid());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_PILLAR, RenderType.solid());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_BULB, RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.EMERGENCY_EXIT, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_UPLOADER, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATA_DOWNLOADER, RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_VOLUME, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_VOLUME_BARRIER, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_INTRUDER_DETECTOR, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_ASPARAGUS, RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WARNING_LIGHT, RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATA_CORE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATA_SERVER, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATA_SHIELD, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DATA_READER, RenderType.solid());
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0xB9E63D,
                ModBlocks.DIGITAL_GRASS_BLOCK, ModBlocks.DIGITAL_GRASS_BARRIER, ModBlocks.DIGITAL_GRASS_SLAB_BLOCK
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((state, i) -> 0xB9E63D,
                ModBlocks.DIGITAL_GRASS_BLOCK, ModBlocks.DIGITAL_GRASS_BARRIER, ModBlocks.DIGITAL_GRASS_SLAB_BLOCK
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers
        event.registerBlockEntityRenderer(ModBlockEntities.DATA_CORE, DataCoreBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DATA_READER, DataReaderBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EMERGENCY_EXIT, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/exit_symbol.png"), 1.5));
        event.registerBlockEntityRenderer(ModBlockEntities.DIGITAL_UPLOADER, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/upload_symbol.png"), 1.5));
        event.registerBlockEntityRenderer(ModBlockEntities.DATA_DOWNLOADER, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/download_symbol.png"), 2.0));
        event.registerBlockEntityRenderer(ModBlockEntities.DIGITAL_INTRUDER_DETECTOR, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/exclamation_symbol.png"), 0.5));
        event.registerBlockEntityRenderer(ModBlockEntities.DIGITAL_TRANSIT_RECORD, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/info_symbol.png"), 1.45));
        event.registerBlockEntityRenderer(ModBlockEntities.DIGITAL_WHITELISTER, DigitalWhitelisterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WARNING_LIGHT, WarningLightBlockEntityRenderer::new);

        // Entity renderers
        event.registerEntityRenderer(ModEntities.CRASH_BOLT, CrashBoltRenderer::new);
        event.registerEntityRenderer(ModEntities.DIGITAL_FISH, DigitalFishRenderer::new);
        event.registerEntityRenderer(ModEntities.DIGITAL_WATCHER, DigitalWatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.DIGITAL_WATCHER_BEAM, DigitalWatcherBeamRenderer::new);
        event.registerEntityRenderer(ModEntities.DIGITAL_ARROW, DigitalArrowRenderer::new);

    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DAY_RIPPLE, RippleParticle.DayFactory::new);
        event.registerSpriteSet(ModParticles.NIGHT_RIPPLE, RippleParticle.NightFactory::new);
        event.registerSpriteSet(ModParticles.ERROR_RIPPLE, RippleParticle.ErrorFactory::new);
        event.registerSpriteSet(ModParticles.BITS, DigitalParticle.BitFactory::new);
        event.registerSpriteSet(ModParticles.ASCENDING_BITS, DigitalParticle.AscendingBitFactory::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), Paradise.identifier("hologram"), DefaultVertexFormat.NEW_ENTITY),
                    ModRenderTypes::setHologramShader
            );

            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), Paradise.identifier("shimmer"), DefaultVertexFormat.NEW_ENTITY),
                    ModRenderTypes::setShimmerShader
            );

            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), Paradise.identifier("watcher"), DefaultVertexFormat.NEW_ENTITY),
                    ModRenderTypes::setWatcherShader
            );

            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), Paradise.identifier("unshaded_color"), DefaultVertexFormat.NEW_ENTITY),
                    shader -> {
                        ModRenderTypes.setUnshadedColorShader(shader);
                        ModRenderTypes.initUnshadedColor();
                    }
            );

            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), Paradise.identifier("digital_teleport"), DefaultVertexFormat.NEW_ENTITY),
                    ModRenderTypes::setDigitalTeleportShader
            );

            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), Paradise.identifier("pixelize"), DefaultVertexFormat.NEW_ENTITY),
                    ModRenderTypes::setPixelizeShader
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to register Paradise shaders", e);
        }
    }

    // Custom S2C payload handlers
    public static class ClientPayloadHandler {
        public static void handleCreateDimension(ModPackets.CreateDimensionPayload payload, IPayloadContext context) {
            ResourceLocation id = payload.id();
            DimensionType type = payload.dimensionType().value();

            context.enqueueWork(() -> {
                Minecraft client = Minecraft.getInstance();
                ClientPacketListener handler = client.getConnection();

                RegistryUtil.registerUnfreezeExact(handler.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE), id, type);
                handler.levels().add(ResourceKey.create(Registries.DIMENSION, id));
            });
        }

        public static void handleRemoveDimension(ModPackets.RemoveDimensionPayload payload, IPayloadContext context) {
            ResourceLocation id = payload.id();

            context.enqueueWork(() -> {
                Minecraft client = Minecraft.getInstance();
                ClientPacketListener handler = client.getConnection();

                RegistryUtil.unregister(handler.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE), id);
                handler.levels().remove(ResourceKey.create(Registries.DIMENSION, id));
            });
        }

        public static void handleOpenTransitLog(ModPackets.OpenTransitLogPayload payload, IPayloadContext context) {
            ResourceLocation dimId = payload.dimensionId();
            List<String> transitLog = payload.transitLog();

            context.enqueueWork(() -> {
                Minecraft.getInstance().setScreen(new TransitLogScreen(dimId, transitLog));
            });
        }

        public static void handleOpenWhitelist(ModPackets.OpenWhitelistPayload payload, IPayloadContext context) {
            ResourceLocation dimId = payload.dimensionId();
            Map<String, Long> active = payload.active();
            Set<String> history = payload.history();
            BlockPos pos = payload.pos();

            context.enqueueWork(() -> {
                Minecraft.getInstance().setScreen(new WhitelistScreen(dimId, active, history, pos));
            });
        }

    }
}
