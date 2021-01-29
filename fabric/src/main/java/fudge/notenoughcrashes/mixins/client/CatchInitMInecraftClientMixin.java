package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Queue;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class CatchInitMInecraftClientMixin  {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/entrypoint/minecraft/hooks/EntrypointClient;start(Ljava/io/File;Ljava/lang/Object;)V", remap = false))
    private void catchFabricInit(File runDir, Object gameInstance) {
        if(NotEnoughCrashes.ENABLE_ENTRYPOINT_CATCHING) {
            try {
                EntrypointClient.start(runDir, gameInstance);
            }catch (Throwable throwable) {
                EntryPointCatcher.handleEntryPointError(throwable);
            }
        } else{
            EntrypointClient.start(runDir, gameInstance);
        }
    }
}
