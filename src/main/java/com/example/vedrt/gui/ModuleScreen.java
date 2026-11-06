package com.example.vedrt.gui;

import com.example.vedrt.module.Module;
import com.example.vedrt.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Main menu: left-click a module to toggle it, right-click to open its settings. */
public class ModuleScreen extends Screen {
    private static final int W = 190, H = 24, GAP = 4, TOP = 52;

    private final List<Module> modules = new ArrayList<>(ModuleManager.all());

    public ModuleScreen() { super(Component.literal("VEDRT")); }

    private int left() { return width / 2 - W / 2; }
    private int rowY(int i) { return TOP + i * (H + GAP); }

    private int indexAt(double mx, double my) {
        if (mx < left() || mx > left() + W) return -1;
        for (int i = 0; i < modules.size(); i++) {
            int y = rowY(i);
            if (my >= y && my <= y + H) return i;
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(font, "VEDRT", width / 2, 20, 0xFF55FFFF);
        g.drawCenteredString(font, "Left-click: toggle   Right-click: settings", width / 2, 34, 0xFFAAAAAA);

        int hover = indexAt(mouseX, mouseY);
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int x = left(), y = rowY(i);
            boolean on = m.isEnabled();
            int bg = on ? 0xDD1F7A3A : 0xDD202020;
            if (i == hover) bg = on ? 0xDD2A9A4C : 0xDD383838;
            g.fill(x, y, x + W, y + H, bg);
            g.fill(x, y, x + 3, y + H, on ? 0xFF55FF55 : 0xFF777777);
            g.drawString(font, m.name, x + 10, y + 4, 0xFFFFFFFF);
            g.drawString(font, m.description, x + 10, y + 14, 0xFFB0B0B0);
            g.drawString(font, on ? "ON" : "OFF", x + W - 24, y + 8, on ? 0xFFFFFFFF : 0xFF999999);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int i = indexAt(event.x(), event.y());
        if (i >= 0) {
            Module m = modules.get(i);
            if (event.button() == 0) {
                m.toggle();
            } else if (event.button() == 1) {
                minecraft.setScreen(new SettingsScreen(m, this));
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
