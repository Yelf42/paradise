package com.yelf42.paradise.dimensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class BunkerSavedData extends SavedData {

    private static final String FILE_ID = "paradise_bunkers";
    private static final String TAG_BUNKERS_GENERATED = "BunkersGenerated";

    public static final Factory<BunkerSavedData> FACTORY = new Factory<>(
            BunkerSavedData::new,
            BunkerSavedData::load,
            null
    );

    private int bunkersGenerated;

    public BunkerSavedData() {
        this.bunkersGenerated = 0;
    }

    public static BunkerSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        BunkerSavedData data = new BunkerSavedData();
        data.bunkersGenerated = tag.getInt(TAG_BUNKERS_GENERATED);
        //Paradise.LOGGER.info("InitializedBunkers: " + data.bunkersGenerated);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_BUNKERS_GENERATED, this.bunkersGenerated);
        return tag;
    }

    public int getBunkersGenerated() {
        return this.bunkersGenerated;
    }

    public void incrementBunkersGenerated() {
        this.bunkersGenerated++;
        //Paradise.LOGGER.info("CurrentBunkers: " + this.bunkersGenerated);
        this.setDirty();
    }

    public static BunkerSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }
}