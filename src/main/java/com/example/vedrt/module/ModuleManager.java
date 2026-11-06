package com.example.vedrt.module;

import com.example.vedrt.Guard;
import com.example.vedrt.gui.ModuleScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleManager {
    private static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("vedrt", "main"));
    private static final Map<Module, KeyMapping> MODULES = new LinkedHashMap<>();
    private static KeyMapping guiKey;

    public static Collection<Module> all() { return MODULES.keySet(); }

    private static void register(Module m) {
        KeyMapping kb = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.vedrt." + m.name.toLowerCase(), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
        MODULES.put(m, kb);
    }

    public static void init() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.vedrt.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        register(new GlowEsp());
        register(new BlockEsp());
        register(new Fullbright());
        register(new Freecam());

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            boolean ok = Guard.allowed(mc);
            while (guiKey.consumeClick()) mc.setScreen(new ModuleScreen());
            MODULES.forEach((m, kb) -> {
                while (kb.consumeClick()) m.toggle();
                if (!ok) m.forceDisable();
                else if (m.isEnabled()) m.onTick(mc);
            });
        });
    }
}
