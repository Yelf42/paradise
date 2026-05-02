package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.entities.CrashBolt;
import com.yelf42.paradise.entities.DigitalFish;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModEntities {
    public static final LinkedHashMap<String, EntityType<?>> REGISTERED_ENTITIES = new LinkedHashMap<>();

    public static final EntityType<CrashBolt> CRASH_BOLT = registerBolt("crash_bolt", CrashBolt::new);

    public static final EntityType<DigitalFish> DIGITAL_FISH = registerMob("digital_fish", DigitalFish::new, 0.8f, 0.6f, 0.4f);

    public static <T extends Entity> EntityType<T> registerProjectile(String name, EntityType.EntityFactory<T> factory) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(10)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    public static <T extends Entity> EntityType<T> registerMob(String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(width, height)
                .eyeHeight(eyeHeight)
                .clientTrackingRange(10)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    public static <T extends Entity> EntityType<T> registerBolt(String name, EntityType.EntityFactory<T> factory) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .noSave()
                .sized(0.0F, 0.0F)
                .clientTrackingRange(16)
                .updateInterval(Integer.MAX_VALUE)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    /// BINDER
    public static void register(BiConsumer<EntityType<?>, ResourceLocation> consumer) {
        REGISTERED_ENTITIES.forEach((key, value) -> consumer.accept(value, Paradise.identifier(key)));
    }
}
