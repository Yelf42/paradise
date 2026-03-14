package com.bonsai.pixelpets;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class PixelPetsNeoforgeClient {
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
