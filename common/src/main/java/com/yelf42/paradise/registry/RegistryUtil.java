package com.yelf42.paradise.registry;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.mixin.HolderSetNamedAccessor;
import com.yelf42.paradise.mixin.MappedRegistryAccessor;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryUtil {

    private RegistryUtil() {}
    public static <T> void unregister(@NotNull Registry<T> registry, @NotNull ResourceLocation id) {
        if (registry.containsKey(id)) {
            if (registry instanceof MappedRegistry<T>) {
                if (registry instanceof DefaultedRegistry<?> reg) {
                    if (reg.getDefaultKey().equals(id)) {
                        throw new IllegalArgumentException("Cannot remove default value in registry!");
                    }
                }

                MappedRegistryAccessor<T> accessor = ((MappedRegistryAccessor<T>) registry);
                ResourceKey<T> key = ResourceKey.create(registry.key(), id);
                T value = accessor.getByLocation().get(id).value();

                ObjectList<Holder.Reference<T>> byId = accessor.getById();
                int rawId = accessor.getToId().removeInt(value);
                if (byId.get(rawId).value() != value) {
                    Paradise.LOGGER.warn("ID mismatch in registry '{}'", registry.key());
                }

                // fixme: this is very unsafe !!!
                Holder.Reference<T> removed = byId.remove(rawId);
                assert removed.value() == value;
                accessor.getToId().replaceAll((t, i) -> i > rawId ? i - 1 : i);

                accessor.getByLocation().remove(id);
                accessor.getByKey().remove(key);
                accessor.getByValue().remove(value);
                accessor.getRegistrationInfos().remove(key);
                Lifecycle base = Lifecycle.stable();
                for (RegistrationInfo info : accessor.getRegistrationInfos().values()) {
                    base.add(info.lifecycle());
                }
                accessor.setRegistryLifecycle(base);
                for (HolderSet.Named<T> holderSet : accessor.tags().values()) {
                    HolderSetNamedAccessor<T> set = (HolderSetNamedAccessor<T>) holderSet;
                    ImmutableList.Builder<Holder<T>> list = ImmutableList.builder();
                    for (Holder<T> content : set.getContents()) {
                        if (!content.is(id)) list.add(content);
                    }
                    set.setContents(list.build());
                }
                if (accessor.getUnregisteredIntrusiveHolders() != null) {
                    accessor.getUnregisteredIntrusiveHolders().remove(value);
                }
            }
        } else {
            Paradise.LOGGER.warn("Tried to remove non-existent key {}", id);
        }
    }

    public static <T> Holder.@NotNull Reference<T> registerUnfreeze(@NotNull Registry<T> registry, ResourceLocation id, T value) {
        if (!registry.containsKey(id)) {
            if (registry.getClass() == MappedRegistry.class || registry.getClass() == DefaultedMappedRegistry.class) {
                MappedRegistry<T> mapped = (MappedRegistry<T>) registry;
                MappedRegistryAccessor<T> accessor = (MappedRegistryAccessor<T>) registry;
                boolean frozen = accessor.isFrozen();
                if (frozen) accessor.setFrozen(false);
                Holder.Reference<T> ref = mapped.register(ResourceKey.create(registry.key(), id), value, RegistrationInfo.BUILT_IN);
                if (frozen) registry.freeze();
                assert accessor.getById().get(accessor.getToId().getInt(value)) != null;
                return ref;
            } else {
                throw new IllegalStateException("Dynamic Dimensions: Non-vanilla '" + registry.key().location() + "' registry! " + registry.getClass().getName());
            }
        } else {
            Paradise.LOGGER.warn("Tried to add pre-existing key {}", id);
            return registry.getHolderOrThrow(ResourceKey.create(registry.key(), id));
        }
    }

    public static <T> Holder.@NotNull Reference<T> registerUnfreezeExact(@NotNull Registry<T> registry, ResourceLocation id, T value) {
        if (!registry.containsKey(id)) {
            if (registry.getClass() == MappedRegistry.class || registry.getClass() == DefaultedMappedRegistry.class) {
                MappedRegistry<T> mapped = (MappedRegistry<T>) registry;
                MappedRegistryAccessor<T> accessor = (MappedRegistryAccessor<T>) registry;
                boolean frozen = accessor.isFrozen();
                if (frozen) accessor.setFrozen(false);
                Holder.Reference<T> ref = mapped.register(ResourceKey.create(registry.key(), id), value, RegistrationInfo.BUILT_IN);
                if (frozen) registry.freeze();
                return ref;
            } else {
                throw new IllegalStateException("Dynamic Dimensions: Non-vanilla '" + registry.key().location() + "' registry! " + registry.getClass().getName());
            }
        } else {
            Paradise.LOGGER.warn("Tried to add pre-existing key {} (contains: {})", id, registry.getId(registry.get(id)));
            return registry.getHolderOrThrow(ResourceKey.create(registry.key(), id));
        }
    }
}
