package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import fudge.notenoughcrashes.utils.GlUtil;
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

import java.util.Queue;

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
            NotEnoughCrashes.logDebug("Handling run loop crash");
            InGameCatcher.handleServerCrash(crashReport);

            // Causes the run loop to keep going
            crashReport = null;
        }
    }


    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", ordinal = 1))
    private CrashReport atTheEndOfFirstCatchBeforePrintingCrashReport(CrashReport report) {
        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in first catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report, renderTaskQueue);
        return report;
    }

    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", ordinal = 2))
    private CrashReport atTheEndOfSecondCatchBeforePrintingCrashReport(CrashReport report) {
        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in second catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report, renderTaskQueue);
        return report;
    }
//
//    @Inject(method = "run()V",
//            at = @At(value = "INVOKE_ASSIGN",
//                    target = "Lnet/minecraft/client/MinecraftClient;addDetailsToCrashReport(Lnet/minecraft/util/crash/CrashReport;)Lnet/minecraft/util/crash/CrashReport;"),
//            cancellable = true)
//    private void afterCrashHandled(CallbackInfo ci) {
//        // Can't cancel in modifyarg so canceled here
//        ci.cancel();
//        // Continue game loop
//        MinecraftClient.getInstance().run();
//    }
//
//    @ModifyArg(method = "run()V",
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/client/MinecraftClient;addDetailsToCrashReport(Lnet/minecraft/util/crash/CrashReport;)Lnet/minecraft/util/crash/CrashReport;"))
//    private CrashReport onCrash(CrashReport report) {
//        // Can't capture arg in inject so captured here
//        InGameCatcher.handleClientCrash(report, renderTaskQueue);
//        return report;
//    }

    /**
     * Prevent the integrated server from exiting in the case it crashed
     */
    @Redirect(method = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    private void redirectPrintCrashReport(CrashReport report) {
        CrashUtils.outputReport(report);
    }

//
//    /**
//     * @author runemoro
//     * @reason Disconnect from the current world and free memory, using a memory reserve
//     * to make sure that an OutOfMemory doesn't happen while doing this.
//     * <p>
//     * Bugs Fixed:
//     * - https://bugs.mojang.com/browse/MC-128953
//     * - Memory reserve not recreated after out-of memory
//     */
//    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
//    // can be replaced by 2-4 injection/redirections
//    public void cleanUpAfterCrash(CallbackInfo ci) {
//        GlUtil.resetState();
////        InGameCatcher.resetGameState(renderTaskQueue);
//    }
}
