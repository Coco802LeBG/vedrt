package com.example.vedrt.module;

import com.example.vedrt.settings.BoolSetting;
import com.example.vedrt.settings.ListSetting;
import com.example.vedrt.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

/** Highlights entities through walls with the vanilla glow outline. */
public class GlowEsp extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 16, 128, 8, 64));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile mobs", true));
    private final BoolSetting animals = add(new BoolSetting("Animals", true));
    private final BoolSetting other = add(new BoolSetting("Other living", false));
    private final BoolSetting items = add(new BoolSetting("Dropped items", false));
    private final ListSetting only = add(new ListSetting("Only these", "")
        .hint("only these entities, e.g. zombie, creeper (empty = use toggles)"));

    private final Set<Integer> ours = new HashSet<>();

    public GlowEsp() { super("GlowEsp", "Outline entities through walls"); }

    private boolean matches(Entity e, Set<String> ids) {
        if (!ids.isEmpty()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
            return ids.contains(id.getPath()) || ids.contains(id.toString());
        }
        if (e instanceof Player) return players.value;
        if (e instanceof Enemy) return hostile.value;
        if (e instanceof ItemEntity) return items.value;
        if (e instanceof LivingEntity) return e.getType().getCategory().isFriendly() ? animals.value : other.value;
        return false;
    }

    @Override
    public void onTick(Minecraft mc) {
        double r = range.get();
        double r2 = r * r;
        Set<String> ids = only.ids();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player) continue;
            boolean want = mc.player.distanceToSqr(e) <= r2 && matches(e, ids);
            if (want) {
                if (ours.add(e.getId())) e.setGlowingTag(true);
            } else if (ours.remove(e.getId())) {
                e.setGlowingTag(false);
            }
        }
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (Entity e : mc.level.entitiesForRendering()) {
                if (ours.contains(e.getId())) e.setGlowingTag(false);
            }
        }
        ours.clear();
    }
}
