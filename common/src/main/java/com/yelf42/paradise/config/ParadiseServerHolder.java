package com.yelf42.paradise.config;

import net.minecraft.server.MinecraftServer;

public final class ParadiseServerHolder {
    private static MinecraftServer server;

    private ParadiseServerHolder() {}

    public static void set(MinecraftServer server) {
        ParadiseServerHolder.server = server;
    }

    public static void clear() {
        server = null;
    }

    public static MinecraftServer get() {
        return server;
    }
}
