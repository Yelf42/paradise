package com.bonsai.pixelpets;

import com.bonsai.pixelpets.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(PixelPets.MOD_ID)
public class PixelPetsForge {

    public static IEventBus eventBus;

    public PixelPetsForge(IEventBus modEventBus, Dist dist) {
        PixelPetsForge.eventBus = modEventBus;

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

        if (dist.isClient()) {
            eventBus.addListener(PixelPetsForgeClient::registerBlocks);
            eventBus.addListener(PixelPetsForgeClient::registerEntityRenderers);
            eventBus.addListener(PixelPetsForgeClient::registerParticleFactories);
        }

        PixelPets.init();
    }

    public static <T> void bind(ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        eventBus.addListener((Consumer<RegisterEvent>) event -> {
            if (event.getRegistryKey().equals(registry)) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // TODO this is how I did entity attributes, might be an easier way in common?
    }
}