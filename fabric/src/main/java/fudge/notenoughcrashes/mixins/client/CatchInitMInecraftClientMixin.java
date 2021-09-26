package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.ModLoaders;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class CatchInitMInecraftClientMixin {

    // require = 0 to support quilt
    @Redirect(method = "<init>", require = 0, at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/entrypoint/minecraft/hooks/EntrypointClient;start(Ljava/io/File;Ljava/lang/Object;)V", remap = false))
    private void catchFabricInit(File runDir, Object gameInstance) {
        if (NotEnoughCrashes.enableEntrypointCatching()) {
            try {
                ModLoaders.fabricEntrypoints(runDir, gameInstance);
            } catch (Throwable throwable) {
                EntryPointCatcher.handleEntryPointError(throwable);
            }
        } else {
            ModLoaders.fabricEntrypoints(runDir, gameInstance);
        }
    }


    @Redirect(method = "<init>", require = 0, at = @At(value = "INVOKE", target = "Lorg/quiltmc/loader/impl/entrypoint/minecraft/hooks/EntrypointClient;start(Ljava/io/File;Ljava/lang/Object;)V", remap = false))
    private void catchQuiltInit(File runDir, Object gameInstance) {
        if (NotEnoughCrashes.enableEntrypointCatching()) {
            try {
                ModLoaders.quiltEntrypoints(runDir, gameInstance);
            } catch (Throwable throwable) {
                EntryPointCatcher.handleEntryPointError(throwable);
            }
        } else {
            ModLoaders.quiltEntrypoints(runDir, gameInstance);
        }
    }
}

