package com.example.vedrt.settings;

import java.util.Locale;

public class NumberSetting extends Setting {
    public final double min, max, step;
    private double value;

    public NumberSetting(String name, double min, double max, double step, double def) {
        super(name);
        this.min = min; this.max = max; this.step = step; this.value = def;
    }

    public double get() { return value; }
    public int getInt() { return (int) Math.round(value); }

    public double normalized() { return (value - min) / (max - min); }

    public void setNormalized(double n) {
        double v = min + n * (max - min);
        v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        return step >= 1 ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
    }

    @Override public String serialize() { return String.valueOf(value); }

    @Override public void deserialize(String s) {
        try { value = Math.max(min, Math.min(max, Double.parseDouble(s))); } catch (NumberFormatException ignored) {}
    }
}
