package com.example.vedrt.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Fullbright extends Module {
    public Fullbright() { super("Fullbright", "See in the dark (client-side night vision)"); }

    @Override
    public void onTick(Minecraft mc) {
        mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
