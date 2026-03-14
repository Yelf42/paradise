package com.bonsai.pixelpets;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class PixelPetsForgeClient {
    @SubscribeEvent
    public static void registerBlocks(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            //ItemBlockRenderTypes.setRenderLayer();
        });
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers
        //event.registerBlockEntityRenderer();

        // Entity renderers
        //event.registerEntityRenderer();

        // Projectile renderers
        //event.registerEntityRenderer();
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        //event.registerSpriteSet();
    }

    // Custom S2C payload handlers
    public static class ClientPayloadHandler {

    }
}
