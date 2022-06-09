package fudge.notenoughcrashes.fabric.mixinhandlers;

import net.fabricmc.loader.impl.game.minecraft.Hooks;

import java.io.File;

public class ModLoaders {
    public static void fabricEntrypoints(File runDir, Object gameInstance) {
        Hooks.startClient(runDir, gameInstance);
    }
    public static void quiltEntrypoints(File runDir, Object gameInstance) {
        org.quiltmc.loader.impl.game.minecraft.Hooks.startClient(runDir, gameInstance);
    }
}
