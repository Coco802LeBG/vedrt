package com.example.vedrt.gui;

import com.example.vedrt.Config;
import com.example.vedrt.module.Module;
import com.example.vedrt.settings.BoolSetting;
import com.example.vedrt.settings.ChoiceSetting;
import com.example.vedrt.settings.ListSetting;
import com.example.vedrt.settings.NumberSetting;
import com.example.vedrt.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

/** Per-module settings: toggles, sliders, choices and a typed id list with presets. */
public class SettingsScreen extends Screen {
    private static final int W = 260, ROW = 24;

    private final Module module;
    private final Screen parent;

    public SettingsScreen(Module module, Screen parent) {
        super(Component.literal(module.name + " settings"));
        this.module = module;
        this.parent = parent;
    }

    private static Component boolLabel(BoolSetting b) {
        return Component.literal(b.name + ": " + (b.value ? "ON" : "OFF"));
    }

    private static Component choiceLabel(ChoiceSetting c) {
        return Component.literal(c.name + ": " + c.get());
    }

    @Override
    protected void init() {
        int x = width / 2 - W / 2;
        int y = 40;

        for (Setting s : module.settings) {
            if (s instanceof BoolSetting b) {
                addRenderableWidget(Button.builder(boolLabel(b), btn -> {
                    b.value = !b.value;
                    btn.setMessage(boolLabel(b));
                }).bounds(x, y, W, 20).build());
                y += ROW;
            } else if (s instanceof NumberSetting n) {
                addRenderableWidget(new NumSlider(x, y, W, 20, n));
                y += ROW;
            } else if (s instanceof ChoiceSetting c) {
                addRenderableWidget(Button.builder(choiceLabel(c), btn -> {
                    c.next();
                    btn.setMessage(choiceLabel(c));
                }).bounds(x, y, W, 20).build());
                y += ROW;
            } else if (s instanceof ListSetting l) {
                EditBox box = new EditBox(font, x, y, W, 20, Component.literal(l.name));
                box.setMaxLength(2048);
                box.setHint(Component.literal(l.hint));
                box.setResponder(text -> l.text = text);
                box.setValue(l.text);
                addRenderableWidget(box);
                y += ROW;

                int n = l.presets.size();
                if (n > 0) {
                    int gap = 4;
                    int bw = (W - gap * (n - 1)) / n;
                    int bx = x;
                    for (Map.Entry<String, String> e : l.presets.entrySet()) {
                        addRenderableWidget(Button.builder(Component.literal(e.getKey()), btn -> box.setValue(e.getValue()))
                            .bounds(bx, y, bw, 20).build());
                        bx += bw + gap;
                    }
                    y += ROW;
                }
            }
        }

        addRenderableWidget(Button.builder(Component.literal("Back"), btn -> onClose())
            .bounds(width / 2 - 50, Math.max(y + 8, height - 28), 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, title, width / 2, 18, 0xFF55FFFF);
        if (module.settings.isEmpty()) {
            g.drawCenteredString(font, "No settings for this module.", width / 2, 60, 0xFFAAAAAA);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        Config.save();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static class NumSlider extends AbstractSliderButton {
        private final NumberSetting s;

        NumSlider(int x, int y, int w, int h, NumberSetting s) {
            super(x, y, w, h, Component.empty(), s.normalized());
            this.s = s;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(s.name + ": " + s.display()));
        }

        @Override
        protected void applyValue() {
            s.setNormalized(this.value);
        }
    }
}
