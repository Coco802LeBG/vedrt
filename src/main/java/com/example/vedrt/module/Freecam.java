package com.example.vedrt.module;

import com.example.vedrt.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only detached camera. The real player stays put (input is zeroed by KeyboardInputMixin);
 * mouse look steers the camera.
 */
public class Freecam extends Module {
    public static boolean active;

    private final NumberSetting speed = add(new NumberSetting("Speed", 0.2, 3.0, 0.1, 0.6));
    private RemotePlayer cam;

    public Freecam() { super("Freecam", "Fly the camera without moving your player"); }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        cam = new RemotePlayer(mc.level, mc.player.getGameProfile());
        cam.copyPosition(mc.player);
        mc.setCameraEntity(cam);
        active = true;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (cam == null) return;
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        double rad = Math.toRadians(yaw);

        Vec3 forward = Vec3.directionFromRotation(pitch, yaw);
        Vec3 right = new Vec3(-Math.cos(rad), 0, -Math.sin(rad));

        double f = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double r = (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0);
        double u = (mc.options.keyJump.isDown() ? 1 : 0) - (mc.options.keyShift.isDown() ? 1 : 0);

        double s = mc.options.keySprint.isDown() ? speed.get() * 2 : speed.get();
        Vec3 move = forward.scale(f).add(right.scale(r)).add(0, u, 0);
        if (move.lengthSqr() > 0) move = move.normalize().scale(s);

        cam.xo = cam.getX(); cam.yo = cam.getY(); cam.zo = cam.getZ();
        cam.xRotO = cam.getXRot(); cam.yRotO = cam.getYRot();
        cam.setPos(cam.getX() + move.x, cam.getY() + move.y, cam.getZ() + move.z);
        cam.setYRot(yaw);
        cam.setXRot(pitch);
    }

    @Override
    protected void onDisable() {
        active = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.setCameraEntity(mc.player);
        cam = null;
    }
}
