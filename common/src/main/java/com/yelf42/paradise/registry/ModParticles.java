package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.platform.Services;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModParticles {

    public static final LinkedHashMap<String, ParticleType<?>> REGISTERED_PARTICLES = new LinkedHashMap<>();

    private static SimpleParticleType registerSimple(String name) {
        var simpleParticleType = Services.PLATFORM.simpleParticleType();
        REGISTERED_PARTICLES.put(name, simpleParticleType);
        return simpleParticleType;
    }

    private static ParticleType<ColorParticleOption> registerTinted(String name) {
        var colorParticleType = new ParticleType<ColorParticleOption>(false) {
            @Override
            public MapCodec<ColorParticleOption> codec() {
                return ColorParticleOption.codec(this);
            }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                return ColorParticleOption.streamCodec(this);
            }
        };

        REGISTERED_PARTICLES.put(name, colorParticleType);
        return colorParticleType;
    }

    /// BINDER
    public static void register(BiConsumer<ParticleType<?>, ResourceLocation> consumer) {
        REGISTERED_PARTICLES.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
