package com.example.vedrt.settings;

public class ChoiceSetting extends Setting {
    public final String[] options;
    private int index;

    public ChoiceSetting(String name, int def, String... options) {
        super(name);
        this.options = options;
        this.index = def;
    }

    public String get() { return options[index]; }
    public int index() { return index; }
    public void next() { index = (index + 1) % options.length; }

    @Override public String serialize() { return get(); }

    @Override public void deserialize(String s) {
        for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(s)) index = i;
    }
}
