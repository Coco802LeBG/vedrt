package com.example.vedrt.module;

import com.example.vedrt.Guard;
import com.example.vedrt.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }

    public void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (!enabled && !Guard.allowed(mc)) {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("VEDRT works in single player / your own LAN world only."), true);
            }
            return;
        }
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    /** Used when leaving the world or joining a remote server. */
    public void forceDisable() {
        if (enabled) { enabled = false; onDisable(); }
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(Minecraft mc) {}
}
