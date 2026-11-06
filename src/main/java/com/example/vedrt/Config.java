package com.example.vedrt;

import com.example.vedrt.module.Module;
import com.example.vedrt.module.ModuleManager;
import com.example.vedrt.settings.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Saves module settings to config/vedrt.json. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config() {}

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("vedrt.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : ModuleManager.all()) {
                JsonObject o = new JsonObject();
                for (Setting s : m.settings) o.addProperty(s.name, s.serialize());
                root.add(m.name, o);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            JsonObject root = GSON.fromJson(Files.readString(p), JsonObject.class);
            if (root == null) return;
            for (Module m : ModuleManager.all()) {
                JsonElement e = root.get(m.name);
                if (e == null || !e.isJsonObject()) continue;
                JsonObject o = e.getAsJsonObject();
                for (Setting s : m.settings) {
                    JsonElement v = o.get(s.name);
                    if (v != null && v.isJsonPrimitive()) s.deserialize(v.getAsString());
                }
            }
        } catch (Exception ignored) {}
    }
}
