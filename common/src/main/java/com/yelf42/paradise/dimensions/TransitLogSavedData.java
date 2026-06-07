package com.yelf42.paradise.dimensions;

import com.yelf42.paradise.Paradise;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class TransitLogSavedData extends SavedData {
    private static final String ID = "paradise_transit_log";

    private final Queue<String> transitLog = new LinkedList<>();

    public static TransitLogSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        TransitLogSavedData::new,
                        TransitLogSavedData::load,
                        DataFixTypes.LEVEL
                ),
                ID
        );
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag transitLogTag = new ListTag();
        for (String entry : transitLog) {
            transitLogTag.add(StringTag.valueOf(entry));
        }
        compoundTag.put("transitLog", transitLogTag);
        return compoundTag;
    }

    public static TransitLogSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TransitLogSavedData transitLogSavedData = new TransitLogSavedData();

        ListTag transitLogTag = tag.getList("transitLog", Tag.TAG_STRING);
        for (int i = 0; i < transitLogTag.size(); i++) {
            transitLogSavedData.transitLog.offer(transitLogTag.getString(i));
        }

        return transitLogSavedData;
    }

    ///  API Calls

    public void addLog(String log) {
        transitLog.offer(log);
        if (transitLog.size() > Paradise.CONFIG.transitLogMaxSize) transitLog.poll();
        this.setDirty();
    }

    public Queue<String> getTransitLog() {
        return transitLog;
    }

    public static String createLogEntry(boolean intruder, String playerName, String action) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (Z), dd/MM");
        return (intruder ? "INTRUDER" : playerName) + "$" + action + "$" + sdf.format(date);
    }
}
