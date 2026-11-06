package com.example.vedrt.settings;

public abstract class Setting {
    public final String name;

    protected Setting(String name) { this.name = name; }

    public abstract String serialize();

    public abstract void deserialize(String s);
}
