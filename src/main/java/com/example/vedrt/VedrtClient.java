package com.example.vedrt;

import com.example.vedrt.command.FindCommand;
import com.example.vedrt.hud.Hud;
import com.example.vedrt.module.ModuleManager;
import com.example.vedrt.render.BlockEspRenderer;
import net.fabricmc.api.ClientModInitializer;

public class VedrtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        Config.load();
        Hud.init();
        FindCommand.init();
        BlockEspRenderer.init();
    }
}
