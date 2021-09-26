package fudge.notenoughcrashes.mixinhandlers;

import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointClient;

import java.io.File;

public class ModLoaders {
    public static void fabricEntrypoints(File runDir, Object gameInstance) {
        EntrypointClient.start(runDir, gameInstance);
    }
    public static void quiltEntrypoints(File runDir, Object gameInstance) {
        org.quiltmc.loader.impl.entrypoint.minecraft.hooks.EntrypointClient.start(runDir, gameInstance);
    }
}
