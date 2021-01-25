package fudge.notenoughcrashes.mixins.client;

import java.io.File;
import java.util.Queue;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.thread.ReentrantThreadExecutor;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> {
    @Shadow
    private CrashReport crashReport;

    @Shadow
    @Final
    private Queue<Runnable> renderTaskQueue;

    public MixinMinecraftClient(String string_1) {
        super(string_1);
    }

    @Inject(method = "run()V", at = @At("HEAD"))
    private void beforeRun(CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup()) EntryPointCatcher.displayInitErrorScreen();
    }

    @Inject(method = "run()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crashReport:Lnet/minecraft/util/crash/CrashReport;"))
    private void onRunLoop(CallbackInfo ci) {
        if (this.crashReport != null) {
            InGameCatcher.handleServerCrash(crashReport);
            crashReport = null;
        }
    }

    @Inject(method = "run()V",
                    at = @At(value = "INVOKE_ASSIGN",
                                    target = "Lnet/minecraft/client/MinecraftClient;addDetailsToCrashReport(Lnet/minecraft/util/crash/CrashReport;)Lnet/minecraft/util/crash/CrashReport;"),
    cancellable = true)
    private void afterCrashHandled(CallbackInfo ci) {
        // Can't cancel in modifyarg so canceled here
        ci.cancel();
        // Continue game loop
        MinecraftClient.getInstance().run();
    }

    @ModifyArg(method = "run()V",
                    at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/MinecraftClient;addDetailsToCrashReport(Lnet/minecraft/util/crash/CrashReport;)Lnet/minecraft/util/crash/CrashReport;"))
    private CrashReport onCrash(CrashReport report) {
        // Can't capture arg in inject so captured here
        InGameCatcher.handleClientCrash(report, renderTaskQueue);
        return report;
    }

    /**
     * Prevent the integrated server from exiting in the case it crashed
     */
    @Redirect(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    private void redirectPrintCrashReport(CrashReport report) {
        CrashUtils.outputReport(report);
    }


    /**
     * @author runemoro
     * @reason Disconnect from the current world and free memory, using a memory reserve
     * to make sure that an OutOfMemory doesn't happen while doing this.
     * <p>
     * Bugs Fixed:
     * - https://bugs.mojang.com/browse/MC-128953
     * - Memory reserve not recreated after out-of memory
     */
    @Overwrite
    //TODO: can be replaced by 2-4 injection/redirections
    public void cleanUpAfterCrash() {
        InGameCatcher.resetGameState(renderTaskQueue);
    }

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
