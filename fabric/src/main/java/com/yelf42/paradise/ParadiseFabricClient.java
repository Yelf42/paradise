package com.yelf42.paradise;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.client.gui.screens.TransitLogScreen;
import com.yelf42.paradise.client.gui.screens.WhitelistScreen;
import com.yelf42.paradise.client.particle.DigitalParticle;
import com.yelf42.paradise.client.particle.RippleParticle;
import com.yelf42.paradise.client.renderer.blockentity.*;
import com.yelf42.paradise.client.renderer.entity.*;
import com.yelf42.paradise.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParadiseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Shaders
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                    Paradise.identifier("hologram"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setHologramShader
            );

            context.register(
                    Paradise.identifier("shimmer"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setShimmerShader
            );

            context.register(
                    Paradise.identifier("watcher"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setWatcherShader
            );

            context.register(
                    Paradise.identifier("unshaded_color"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setUnshadedColorShader
            );
            ModRenderTypes.initUnshadedColor();

            context.register(
                    Paradise.identifier("digital_teleport"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setDigitalTeleportShader
            );

            context.register(
                    Paradise.identifier("pixelize"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setPixelizeShader
            );
        });

        // Blocks
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_GRASS_BLOCK, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_GRASS_BARRIER, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_PILLAR, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_BULB, RenderType.translucent());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.EMERGENCY_EXIT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_UPLOADER, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_DOWNLOADER, RenderType.cutout());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_VOLUME, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_VOLUME_BARRIER, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_INTRUDER_DETECTOR, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_ASPARAGUS, RenderType.cutout());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WARNING_LIGHT, RenderType.translucent());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_CORE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_SERVER, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_SHIELD, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_READER, RenderType.solid());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0xB9E63D, ModBlocks.DIGITAL_GRASS_BLOCK, ModBlocks.DIGITAL_GRASS_BARRIER, ModBlocks.DIGITAL_GRASS_SLAB_BLOCK
        );

        ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
                        0xB9E63D,
                ModBlocks.DIGITAL_GRASS_BLOCK.asItem(), ModBlocks.DIGITAL_GRASS_BARRIER.asItem(), ModBlocks.DIGITAL_GRASS_SLAB_BLOCK.asItem()
        );

        // Block Entities
        BlockEntityRenderers.register(ModBlockEntities.DATA_CORE, DataCoreBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DATA_READER, DataReaderBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.EMERGENCY_EXIT, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/exit_symbol.png"), 1.5));
        BlockEntityRenderers.register(ModBlockEntities.DIGITAL_UPLOADER, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/upload_symbol.png"), 1.5));
        BlockEntityRenderers.register(ModBlockEntities.DATA_DOWNLOADER, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/download_symbol.png"), 2.0));
        BlockEntityRenderers.register(ModBlockEntities.DIGITAL_INTRUDER_DETECTOR, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/exclamation_symbol.png"), 0.5));
        BlockEntityRenderers.register(ModBlockEntities.DIGITAL_TRANSIT_RECORD, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/info_symbol.png"), 1.45));
        BlockEntityRenderers.register(ModBlockEntities.DIGITAL_WHITELISTER, DigitalWhitelisterRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.WARNING_LIGHT, WarningLightBlockEntityRenderer::new);

        // Entities
        EntityRendererRegistry.register(ModEntities.CRASH_BOLT, CrashBoltRenderer::new);
        EntityRendererRegistry.register(ModEntities.DIGITAL_FISH, DigitalFishRenderer::new);
        EntityRendererRegistry.register(ModEntities.DIGITAL_WATCHER, DigitalWatcherRenderer::new);
        EntityRendererRegistry.register(ModEntities.DIGITAL_WATCHER_BEAM, DigitalWatcherBeamRenderer::new);
        EntityRendererRegistry.register(ModEntities.DIGITAL_ARROW, DigitalArrowRenderer::new);

        // Particles
        ParticleFactoryRegistry.getInstance().register(ModParticles.DAY_RIPPLE, RippleParticle.DayFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.NIGHT_RIPPLE, RippleParticle.NightFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.ERROR_RIPPLE, RippleParticle.ErrorFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.BITS, DigitalParticle.BitFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.ASCENDING_BITS, DigitalParticle.AscendingBitFactory::new);

        // Packets
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.CreateDimensionPayload.ID, (payload, context) -> {
            ResourceLocation id = payload.id();
            DimensionType type = payload.dimensionType().value();

            Minecraft client = context.client();
            ClientPacketListener handler = client.getConnection();

            client.execute(() -> {
                RegistryUtil.registerUnfreezeExact(handler.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE), id, type);
                handler.levels().add(ResourceKey.create(Registries.DIMENSION, id));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.RemoveDimensionPayload.ID, (payload, context) -> {
            ResourceLocation id = payload.id();
            Minecraft client = context.client();

            ClientPacketListener handler = client.getConnection();

            client.execute(() -> {
                RegistryUtil.unregister(handler.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE), id);
                handler.levels().remove(ResourceKey.create(Registries.DIMENSION, id));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenTransitLogPayload.ID, (payload, context) -> {
            ResourceLocation dimId = payload.dimensionId();
            List<String> transitLog = payload.transitLog();

            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new TransitLogScreen(
                        dimId,
                        transitLog
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenWhitelistPayload.ID, (payload, context) -> {
            ResourceLocation dimId = payload.dimensionId();
            Map<String, Long> active = payload.active();
            Set<String> history = payload.history();
            BlockPos pos = payload.pos();
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new WhitelistScreen(
                        dimId,
                        active,
                        history,
                        pos
                ));
            });
        });

    }

}
