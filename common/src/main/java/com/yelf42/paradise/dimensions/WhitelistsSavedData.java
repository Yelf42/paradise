package com.yelf42.paradise.dimensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WhitelistsSavedData extends SavedData {
    private static final String ID = "paradise_whitelists";

    // TODO replace with config value
    private static final long MAX_AGE = 1000L * 60 * 60 * 24 * 3;

    private final Map<ResourceLocation, Whitelist> whitelists = new ConcurrentHashMap<>();

    public static class Whitelist {
        private final Map<String, Long> active = new LinkedHashMap<>();
        private final Set<String> history = new LinkedHashSet<>();

        public Whitelist() {
        }

        private boolean isExpired(long timestamp) {
            return System.currentTimeMillis() - timestamp > MAX_AGE;
        }

        private void purgeExpired(Runnable markDirty) {
            List<String> expired = active.entrySet().stream()
                    .filter(e -> isExpired(e.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();
            if (!expired.isEmpty()) {
                expired.forEach(name -> {
                    active.remove(name);
                    history.add(name);
                });
                markDirty.run();
            }
        }

        public boolean isWhitelisted(String name) {
            return active.containsKey(name) || active.isEmpty();
        }

        public Map<String, Long> getActive() {
            return Collections.unmodifiableMap(active);
        }

        public Set<String> getHistory() {
            return Collections.unmodifiableSet(history);
        }
    }

    public static WhitelistsSavedData getOrCreate(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        WhitelistsSavedData::new,
                        WhitelistsSavedData::load,
                        DataFixTypes.LEVEL
                ),
                ID
        );
    }

    public static WhitelistsSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        WhitelistsSavedData data = new WhitelistsSavedData();
        CompoundTag whitelistsTag = tag.getCompound("whitelists");
        for (String key : whitelistsTag.getAllKeys()) {
            ResourceLocation dimId = ResourceLocation.parse(key);
            CompoundTag wTag = whitelistsTag.getCompound(key);
            Whitelist whitelist = new Whitelist();

            CompoundTag activeTag = wTag.getCompound("active");
            for (String name : activeTag.getAllKeys()) {
                whitelist.active.put(name, activeTag.getLong(name));
            }

            ListTag historyTag = wTag.getList("history", Tag.TAG_STRING);
            for (int i = 0; i < historyTag.size(); i++) {
                whitelist.history.add(historyTag.getString(i));
            }

            data.whitelists.put(dimId, whitelist);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag whitelistsTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Whitelist> entry : whitelists.entrySet()) {
            Whitelist whitelist = entry.getValue();
            CompoundTag wTag = new CompoundTag();

            CompoundTag activeTag = new CompoundTag();
            for (Map.Entry<String, Long> active : whitelist.active.entrySet()) {
                activeTag.putLong(active.getKey(), active.getValue());
            }
            wTag.put("active", activeTag);

            ListTag historyTag = new ListTag();
            for (String name : whitelist.history) {
                historyTag.add(StringTag.valueOf(name));
            }
            wTag.put("history", historyTag);

            whitelistsTag.put(entry.getKey().toString(), wTag);
        }
        tag.put("whitelists", whitelistsTag);
        return tag;
    }

    private Whitelist getOrCreateWhitelist(ResourceLocation dimId) {
        return whitelists.computeIfAbsent(dimId, k -> new Whitelist());
    }

    private boolean invalidDimension(ResourceLocation dimId) {
        return  !(dimId.getNamespace().equals("paradise")) || (dimId.getPath().equals("nullspace"));
    }

    /// APIs

    public void addPlayer(ResourceLocation dimId, String name) {
        if (invalidDimension(dimId)) return;
        Whitelist w = getOrCreateWhitelist(dimId);
        w.history.remove(name);
        w.active.put(name, System.currentTimeMillis());
        setDirty();
    }

    public boolean reactivatePlayer(ResourceLocation dimId, String name) {
        if (invalidDimension(dimId)) return false;
        Whitelist w = getOrCreateWhitelist(dimId);
        if (w.history.remove(name)) {
            w.active.put(name, System.currentTimeMillis());
            setDirty();
            return true;
        } else if (w.active.containsKey(name)) {
            w.active.put(name, System.currentTimeMillis());
            setDirty();
            return true;
        }
        return false;
    }


    // Return if is whitelisted
    public boolean flipPlayer(ResourceLocation dimId, String name) {
        if (invalidDimension(dimId)) return true;

        Whitelist w = getOrCreateWhitelist(dimId);
        if (w.active.remove(name) != null) {
            w.history.add(name);
            setDirty();
        } else if (w.history.remove(name)) {
            w.active.put(name, System.currentTimeMillis());
            setDirty();
        }

        return isWhitelisted(dimId, name);
    }

    public void removePlayer(ResourceLocation dimId, String name) {
        if (invalidDimension(dimId)) return;
        Whitelist w = getOrCreateWhitelist(dimId);
        if (w.history.remove(name)) {
            setDirty();
        }
        if (w.active.remove(name) != null) {
            setDirty();
        }
    }

    // Consider whitelisted if no players are whitelisted or a whitelist doesn't exist for the dimension
    public boolean isWhitelisted(ResourceLocation dimId, String name) {
        Whitelist w = whitelists.get(dimId);
        if (w == null) return true;
        w.purgeExpired(this::setDirty);
        return w.isWhitelisted(name);
    }

    public boolean hasWhitelist(ResourceLocation dimId) {
        return !invalidDimension(dimId);
    }

    public Map<String, Long> getActive(ResourceLocation dimId) {
        Whitelist w = whitelists.get(dimId);
        if (w == null) return Map.of();
        w.purgeExpired(this::setDirty);
        return w.getActive();
    }

    public Set<String> getHistory(ResourceLocation dimId) {
        Whitelist w = whitelists.get(dimId);
        if (w == null) return Set.of();
        return w.getHistory();
    }

    public void deleteWhitelist(ResourceLocation dimId) {
        if (whitelists.remove(dimId) != null) {
            setDirty();
        }
    }
}
