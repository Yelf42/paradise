package com.yelf42.paradise;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.yelf42.paradise.client.ModRenderTypes;
import com.yelf42.paradise.client.renderer.blockentity.DataCoreBlockEntityRenderer;
import com.yelf42.paradise.client.renderer.blockentity.DataReaderBlockEntityRenderer;
import com.yelf42.paradise.client.renderer.blockentity.DigitalSymbolRenderer;
import com.yelf42.paradise.client.renderer.entity.CrashBoltRenderer;
import com.yelf42.paradise.client.renderer.entity.DigitalFishRenderer;
import com.yelf42.paradise.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class ParadiseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Blocks
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_GRASS_BLOCK, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_GRASS_BARRIER, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_PILLAR, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.EMERGENCY_EXIT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_UPLOADER, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_DOWNLOADER, RenderType.cutout());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_VOLUME, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_VOLUME_BARRIER, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_CORE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_SERVER, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_SHIELD, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_READER, RenderType.solid());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0xB9E63D, ModBlocks.DIGITAL_GRASS_BLOCK, ModBlocks.DIGITAL_GRASS_BARRIER
        );

        ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
                        0xB9E63D,
                ModBlocks.DIGITAL_GRASS_BLOCK.asItem(), ModBlocks.DIGITAL_GRASS_BARRIER.asItem()
        );

        // Block Entities
        BlockEntityRenderers.register(ModBlockEntities.DATA_CORE, DataCoreBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DATA_READER, DataReaderBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.EMERGENCY_EXIT, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/exit_symbol.png"), 1.5));
        BlockEntityRenderers.register(ModBlockEntities.DIGITAL_UPLOADER, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/upload_symbol.png"), 1.5));
        BlockEntityRenderers.register(ModBlockEntities.DATA_DOWNLOADER, (context) -> new DigitalSymbolRenderer<>(context, Paradise.identifier("textures/entity/download_symbol.png"), 2.0));

        // Entities
        EntityRendererRegistry.register(ModEntities.CRASH_BOLT, CrashBoltRenderer::new);
        EntityRendererRegistry.register(ModEntities.DIGITAL_FISH, DigitalFishRenderer::new);

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
                    Paradise.identifier("digital_teleport"),
                    DefaultVertexFormat.NEW_ENTITY,
                    ModRenderTypes::setDigitalTeleportShader
            );
        });
    }

}
