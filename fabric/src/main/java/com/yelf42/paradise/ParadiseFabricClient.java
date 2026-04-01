package com.yelf42.paradise;

import com.yelf42.paradise.client.renderer.blockentity.DataCoreBlockEntityRenderer;
import com.yelf42.paradise.registry.ModBlockEntities;
import com.yelf42.paradise.registry.ModBlocks;
import com.yelf42.paradise.registry.ModPackets;
import com.yelf42.paradise.registry.RegistryUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class ParadiseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Blocks
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_GRASS, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_GRASS_BARRIER, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_VOLUME, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIGITAL_VOLUME_BARRIER, RenderType.solid());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_CORE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_SERVER, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_SHIELD, RenderType.solid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DATA_READER, RenderType.solid());

        // Block Entities
        BlockEntityRenderers.register(ModBlockEntities.DATA_CORE, DataCoreBlockEntityRenderer::new);


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
    }

}
