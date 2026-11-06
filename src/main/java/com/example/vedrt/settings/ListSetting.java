package com.example.vedrt.settings;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Comma-separated list of ids typed by the user, with optional one-click presets. */
public class ListSetting extends Setting {
    public String text;
    public String hint = "ids, comma separated";
    public final Map<String, String> presets = new LinkedHashMap<>();

    public ListSetting(String name, String def) { super(name); this.text = def; }

    public ListSetting hint(String h) { this.hint = h; return this; }

    public ListSetting preset(String label, String csv) { presets.put(label, csv); return this; }

    public Set<String> ids() {
        Set<String> out = new LinkedHashSet<>();
        for (String t : text.split("[,;\\s]+")) {
            String s = t.trim().toLowerCase();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    @Override public String serialize() { return text; }
    @Override public void deserialize(String s) { text = s; }
}
