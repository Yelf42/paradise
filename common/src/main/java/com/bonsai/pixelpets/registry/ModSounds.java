package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModSounds {
    public static final LinkedHashMap<String, SoundEvent> REGISTERED_SOUNDS = new LinkedHashMap<>();


    private static SoundEvent register(String name) {
        var soundEvent = SoundEvent.createVariableRangeEvent(PixelPets.identifier(name));
        REGISTERED_SOUNDS.put(name, soundEvent);
        return soundEvent;
    }

    private static Holder<SoundEvent> registerHolder(String name) {
        var soundEvent = SoundEvent.createVariableRangeEvent(PixelPets.identifier(name));
        REGISTERED_SOUNDS.put(name, soundEvent);
        return Holder.direct(soundEvent);
    }

    /// BINDER
    public static void register(BiConsumer<SoundEvent, ResourceLocation> consumer) {
        REGISTERED_SOUNDS.forEach((key, value) -> consumer.accept(value, PixelPets.identifier(key)));
    }
}
