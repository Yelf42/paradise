package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModSounds {
    public static final LinkedHashMap<String, SoundEvent> REGISTERED_SOUNDS = new LinkedHashMap<>();

    public static final SoundEvent SERVER_LOCATOR_PING = register("item.server_locator_ping");

    private static SoundEvent register(String name) {
        var soundEvent = SoundEvent.createVariableRangeEvent(Paradise.identifier(name));
        REGISTERED_SOUNDS.put(name, soundEvent);
        return soundEvent;
    }

    private static Holder<SoundEvent> registerHolder(String name) {
        var soundEvent = SoundEvent.createVariableRangeEvent(Paradise.identifier(name));
        REGISTERED_SOUNDS.put(name, soundEvent);
        return Holder.direct(soundEvent);
    }

    /// BINDER
    public static void register(BiConsumer<SoundEvent, ResourceLocation> consumer) {
        REGISTERED_SOUNDS.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
