package com.yelf42.paradise.dimensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/// Cases
/// 1. Unwhitelisted player enters dimension                        -> add
///     -> ServerLevelMixin
/// 2. Unwhitelisted player leaves dimension                        -> remove
///     -> ServerLevelMixin
/// 3. Intruder logs-in to dimension and hasn't been whitelisted    -> no change
/// 4. Intruder logs-in to dimension and has been whitelisted       -> remove
///     -> ServerLevelMixin
/// 5. Intruder is whitelisted during intrusion                     -> remove
///     -> GUI packet handling
///     -> Whitelisting commands
/// 6. Player is unwhitelisted while in dimension                   -> no change
/// 7. Player logs-in to dimension and whitelist has expired        -> no change

public class IntrudersSavedData extends SavedData {
    private static final String ID = "paradise_intruders";

    private final Set<UUID> intruders = new HashSet<>();

    public static IntrudersSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        IntrudersSavedData::new,
                        IntrudersSavedData::load,
                        DataFixTypes.LEVEL
                ),
                ID
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag intrudersTag = new ListTag();
        for (UUID player : intruders) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("uuid", player);
            intrudersTag.add(uuidTag);
        }
        tag.put("intruders", intrudersTag);
        return tag;
    }

    public static IntrudersSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        IntrudersSavedData intrudersSavedData = new IntrudersSavedData();

        ListTag intrudersTag = tag.getList("intruders", Tag.TAG_COMPOUND);
        for (int i = 0; i < intrudersTag.size(); i++) {
            CompoundTag uuidTag = intrudersTag.getCompound(i);
            intrudersSavedData.intruders.add(uuidTag.getUUID("uuid"));
        }

        return intrudersSavedData;
    }

    ///  API

    public boolean isIntruder(UUID uuid) {
        return intruders.contains(uuid);
    }

    public boolean intrudersPresent(ServerLevel level) {
        if (intruders.isEmpty()) return false;
        for (UUID id : intruders) {
            if (level.getEntity(id) != null) return true;
        }
        return false;
    }

    public Set<UUID> getIntruders() {
        return Collections.unmodifiableSet(intruders);
    }

    public int totalIntruders() {
        return intruders.size();
    }

    public void add(UUID uuid) {
        if (intruders.add(uuid)) {
            setDirty();
        }
    }

    public void remove(UUID uuid) {
        if (intruders.remove(uuid)) {
            setDirty();
        }
    }
}
