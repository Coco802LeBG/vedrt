package com.example.vedrt.command;

import com.example.vedrt.Guard;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

/** /vedrt find fortress  ->  runs vanilla /locate structure (needs cheats enabled in your world). */
public class FindCommand {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("vedrt")
                .then(ClientCommandManager.literal("find")
                    .then(ClientCommandManager.argument("structure", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (!Guard.allowed(mc)) return 0;
                            String id = StringArgumentType.getString(ctx, "structure").trim();
                            if (!id.contains(":")) id = "minecraft:" + id;
                            mc.player.connection.sendCommand("locate structure " + id);
                            return 1;
                        })))));
    }
}
