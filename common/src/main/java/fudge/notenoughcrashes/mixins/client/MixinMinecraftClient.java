package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.patches.MinecraftClientAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.function.Supplier;
//TODO: infinite repeating crashes in different cases, need to go over the crash cases and solve them one by one.


@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> implements MinecraftClientAccess {
    @Shadow
    @Nullable
    private Supplier<CrashReport> crashReportSupplier;

    @Shadow
    @Final
    private Queue<Runnable> renderTaskQueue;

    @Shadow
    private Recorder recorder;

    @Shadow
    public void printCrashReport(CrashReport report) {
    }

    @Override
    public Recorder getRecorder() {
        return recorder;
    }

    @Override
    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    public MixinMinecraftClient(String string_1) {
        super(string_1);
    }

    /**
     * If the game has crashed, we set the screen to the init crash screen, but then Minecraft sets the screen back
     * to the title screen. We want to prevent that, to keep the screen to be the InitCrashScreen
     */
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void setScreenDontResetCrashScreen(Screen screen, CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup() && !(screen instanceof InitErrorScreen)) ci.cancel();
    }

    @Inject(method = "run()V", at = @At("HEAD"))
    private void beforeRun(CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup()) EntryPointCatcher.displayInitErrorScreen();
    }

//    @Inject(method = "run()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crashReportSupplier:Ljava/util/function/Supplier;"))
//    private void onRunLoop(CallbackInfo ci) {
//
//    }

    @Inject(method = "printCrashReport()V", at = @At("HEAD"))
    private void onCheckGameCrashed(CallbackInfo ci) {
        if (!NotEnoughCrashes.enableGameloopCatching()) return;

        if (this.crashReportSupplier != null) {
            NotEnoughCrashes.logDebug("Handling run loop crash");
            InGameCatcher.handleServerCrash(crashReportSupplier.get());

            // Causes the run loop to keep going
            crashReportSupplier = null;
        }
    }


    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", ordinal = 0))
    private CrashReport atTheEndOfFirstCatchBeforePrintingCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.enableGameloopCatching()) return report;

        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in first catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report);
        return report;
    }

    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", ordinal = 1))
    private CrashReport atTheEndOfSecondCatchBeforePrintingCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.enableGameloopCatching()) return report;

        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in second catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report);
        return report;
    }

//    // Prevent calling printCrashReport which is not needed
//    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"), cancellable = true)
//    private void cancelRunLoopAfterCrash(CallbackInfo ci) {
//        if (NotEnoughCrashes.enableGameloopCatching()) ci.cancel();
//    }

    @Inject(method = "cleanUpAfterCrash()V", at = @At("HEAD"))
    private void beforeCleanUpAfterCrash(CallbackInfo info) {
        if (NotEnoughCrashes.enableGameloopCatching()) {
            InGameCatcher.cleanupBeforeMinecraft(renderTaskQueue);
        }
    }
//String levelName, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader

    //TODO: test this: maybe we don't need this mixin anymore?

//    /**
//     * Prevent the integrated server from exiting in the case it crashed
//     */
//    @Redirect(method = "startIntegratedServer(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/server/SaveLoader;Z)V",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
//    private void redirectPrintCrashReport(CrashReport report) {
//        if (!NotEnoughCrashes.enableGameloopCatching()) printCrashReport(report);
//    }

    /**
     * Forge only: Prevent the integrated server from exiting in the case it crashed in another case
     */
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Redirect(method = "doLoadLevel(Ljava/lang/String;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"),
            require = 0)
    private void redirectForgePrintCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.enableGameloopCatching()) printCrashReport(report);
    }

}
