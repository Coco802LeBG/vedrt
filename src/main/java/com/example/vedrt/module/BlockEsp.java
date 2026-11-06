package com.example.vedrt.module;

import com.example.vedrt.settings.ChoiceSetting;
import com.example.vedrt.settings.ListSetting;
import com.example.vedrt.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Highlights chosen block types through walls. Scanning runs on a worker thread. */
public class BlockEsp extends Module {
    public static BlockEsp INSTANCE;

    private static final String CHESTS = "chest,trapped_chest,barrel,ender_chest,shulker_box";
    private static final String ORES = "coal_ore,deepslate_coal_ore,iron_ore,deepslate_iron_ore,copper_ore,deepslate_copper_ore,"
        + "gold_ore,deepslate_gold_ore,redstone_ore,deepslate_redstone_ore,lapis_ore,deepslate_lapis_ore,"
        + "diamond_ore,deepslate_diamond_ore,emerald_ore,deepslate_emerald_ore,nether_gold_ore,nether_quartz_ore,ancient_debris";
    private static final String SPAWNERS = "spawner,trial_spawner,vault";

    private static final float[][] COLORS = {
        {1f, .2f, .2f}, {.2f, 1f, .3f}, {.3f, .5f, 1f}, {1f, 1f, .2f},
        {.2f, 1f, 1f}, {1f, .3f, 1f}, {1f, 1f, 1f}, {1f, .6f, .1f}};

    private final NumberSetting range = add(new NumberSetting("Range", 16, 128, 16, 64));
    private final NumberSetting maxBoxes = add(new NumberSetting("Max boxes", 100, 3000, 100, 1000));
    private final NumberSetting opacity = add(new NumberSetting("Opacity", 0.1, 0.8, 0.05, 0.35));
    private final ChoiceSetting color = add(new ChoiceSetting("Color", 0,
        "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "White", "Orange"));
    private final ListSetting blocks = add(new ListSetting("Blocks", "chest,trapped_chest,barrel,ender_chest")
        .hint("block ids, e.g. dirt, diamond_ore, chest")
        .preset("Chests", CHESTS).preset("Ores", ORES).preset("Spawners", SPAWNERS).preset("Dirt", "dirt"));

    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "vedrt-scan");
        t.setDaemon(true);
        return t;
    });

    private static volatile long[] results = new long[0];
    public static volatile long lastScanMicros;
    public static volatile long lastScanBlocks;

    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private int ticks;

    public BlockEsp() {
        super("BlockEsp", "Highlight chosen blocks through walls");
        INSTANCE = this;
    }

    public static long[] results() { return results; }
    public float[] color() { return COLORS[color.index()]; }
    public float opacity() { return (float) opacity.get(); }
    public int maxBoxes() { return maxBoxes.getInt(); }

    private Set<Block> resolveTargets() {
        Set<Block> out = new HashSet<>();
        for (String s : blocks.ids()) {
            Identifier id = Identifier.tryParse(s);
            if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
                Block b = BuiltInRegistries.BLOCK.getValue(id);
                if (b != Blocks.AIR) out.add(b);
            }
        }
        return out;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (++ticks % 20 != 0 || scanning.get()) return;
        Set<Block> targets = resolveTargets();
        if (targets.isEmpty()) { results = new long[0]; return; }

        ClientLevel level = mc.level;
        double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        double r = range.get();
        int rad = (int) Math.ceil(r / 16.0);
        int max = maxBoxes.getInt();

        scanning.set(true);
        POOL.submit(() -> {
            try {
                scan(level, targets, pcx, pcz, rad, r, px, py, pz, max);
            } catch (Throwable ignored) {
            } finally {
                scanning.set(false);
            }
        });
    }

    private record Hit(long pos, double dist) {}

    private static void scan(ClientLevel level, Set<Block> targets, int pcx, int pcz, int rad,
                             double range, double px, double py, double pz, int max) {
        long t0 = System.nanoTime();
        long scanned = 0;
        double r2 = range * range;
        int minY = level.getMinY();
        List<Hit> hits = new ArrayList<>();

        outer:
        for (int dx = -rad; dx <= rad; dx++) {
            for (int dz = -rad; dz <= rad; dz++) {
                ChunkAccess ca = level.getChunk(pcx + dx, pcz + dz, ChunkStatus.FULL, false);
                if (!(ca instanceof LevelChunk chunk)) continue;
                LevelChunkSection[] secs = chunk.getSections();
                int cx = chunk.getPos().x << 4, cz = chunk.getPos().z << 4;
                for (int i = 0; i < secs.length; i++) {
                    LevelChunkSection s = secs[i];
                    if (s == null || s.hasOnlyAir()) continue;
                    if (!s.maybeHas(st -> targets.contains(st.getBlock()))) continue;
                    scanned += 4096;
                    int baseY = minY + (i << 4);
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                BlockState st = s.getBlockState(x, y, z);
                                if (!targets.contains(st.getBlock())) continue;
                                int wx = cx + x, wy = baseY + y, wz = cz + z;
                                double ddx = wx + 0.5 - px, ddy = wy + 0.5 - py, ddz = wz + 0.5 - pz;
                                double d = ddx * ddx + ddy * ddy + ddz * ddz;
                                if (d <= r2) hits.add(new Hit(BlockPos.asLong(wx, wy, wz), d));
                            }
                        }
                    }
                    if (hits.size() >= 30000) break outer;
                }
            }
        }

        hits.sort((a, b) -> Double.compare(a.dist, b.dist));
        int n = Math.min(hits.size(), max);
        long[] arr = new long[n];
        for (int i = 0; i < n; i++) arr[i] = hits.get(i).pos;
        results = arr;
        lastScanBlocks = scanned;
        lastScanMicros = (System.nanoTime() - t0) / 1000;
    }

    @Override
    protected void onDisable() {
        results = new long[0];
    }
}
