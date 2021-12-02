package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.patches.MinecraftClientAccess;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.minecraft.client.MinecraftClient;
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

    @Inject(method = "run()V", at = @At("HEAD"))
    private void beforeRun(CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup()) EntryPointCatcher.displayInitErrorScreen();
    }

    @Inject(method = "run()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crashReportSupplier:Ljava/util/function/Supplier;"))
    private void onRunLoop(CallbackInfo ci) {
        if (!NotEnoughCrashes.ENABLE_GAMELOOP_CATCHING) return;

        if (this.crashReportSupplier != null) {
            NotEnoughCrashes.logDebug("Handling run loop crash");
            InGameCatcher.handleServerCrash(crashReportSupplier.get());

            // Causes the run loop to keep going
            crashReportSupplier = null;
        }
    }


    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", ordinal = 1))
    private CrashReport atTheEndOfFirstCatchBeforePrintingCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.ENABLE_GAMELOOP_CATCHING) return report;

        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in first catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report);
        return report;
    }

    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", ordinal = 2))
    private CrashReport atTheEndOfSecondCatchBeforePrintingCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.ENABLE_GAMELOOP_CATCHING) return report;

        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in second catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report);
        return report;
    }

    // Prevent calling printCrashReport which is not needed
    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"), cancellable = true)
    private void cancelRunLoopAfterCrash(CallbackInfo ci) {
        if (NotEnoughCrashes.ENABLE_GAMELOOP_CATCHING) ci.cancel();
    }

    @Inject(method = "cleanUpAfterCrash()V", at = @At("HEAD"))
    private void beforeCleanUpAfterCrash(CallbackInfo info) {
        InGameCatcher.cleanupBeforeMinecraft(renderTaskQueue);
    }

    /**
     * Prevent the integrated server from exiting in the case it crashed
     */
    @Redirect(method = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    private void redirectPrintCrashReport(CrashReport report) {
//        CrashUtils.outputReport(report);
    }
    /**
     * Forge only: Prevent the integrated server from exiting in the case it crashed in another case
     */
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Redirect(method = "doLoadLevel(Ljava/lang/String;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"),
            require = 0)
    private void redirectForgePrintCrashReport(CrashReport report) {
    }

}
