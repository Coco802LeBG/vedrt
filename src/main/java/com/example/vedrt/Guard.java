package com.example.vedrt;

import net.minecraft.client.Minecraft;

/** VEDRT only runs in single player or on your own LAN host. It switches off on remote servers. */
public final class Guard {
    private Guard() {}

    public static boolean allowed(Minecraft mc) {
        return mc.level != null && mc.player != null && mc.getSingleplayerServer() != null;
    }
}
