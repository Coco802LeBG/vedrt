package com.example.vedrt.hud;

import com.example.vedrt.module.BlockEsp;
import com.example.vedrt.module.Module;
import com.example.vedrt.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class Hud {
    public static void init() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("vedrt", "hud"), (graphics, tick) -> render(graphics));
    }

    private static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

        int near = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e != mc.player && mc.player.distanceToSqr(e) < 64 * 64) near++;
        }

        int y = 4;
        g.drawString(mc.font, "VEDRT", 4, y, 0xFF55FFFF);
        y += 11;
        g.drawString(mc.font, "Entities (64m): " + near, 4, y, 0xFFFFFFFF);
        y += 11;

        BlockEsp be = BlockEsp.INSTANCE;
        if (be != null && be.isEnabled()) {
            long ms10 = BlockEsp.lastScanMicros / 100;
            double perMs = BlockEsp.lastScanMicros > 0 ? BlockEsp.lastScanBlocks / (BlockEsp.lastScanMicros / 1000.0) : 0;
            g.drawString(mc.font, "Block scan: " + BlockEsp.results().length + " hits, "
                + (ms10 / 10.0) + " ms, " + Math.round(perMs) + " blocks/ms", 4, y, 0xFFFFFFFF);
            y += 11;
        }

        for (Module m : ModuleManager.all()) {
            if (m.isEnabled()) {
                g.drawString(mc.font, m.name, 4, y, 0xFF55FF55);
                y += 10;
            }
        }
    }
}
