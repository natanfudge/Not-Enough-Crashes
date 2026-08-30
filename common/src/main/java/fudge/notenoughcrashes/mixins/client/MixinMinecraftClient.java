package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.patches.MinecraftClientAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.CrashReport;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO: infinite repeating crashes in different cases, need to go over the crash cases and solve them one by one.


@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient extends ReentrantBlockableEventLoop<Runnable> implements MinecraftClientAccess {
    @Shadow
    private MetricsRecorder metricsRecorder;

    @Override
    public MetricsRecorder getRecorder() {
        return metricsRecorder;
    }

    @Override
    public void setRecorder(MetricsRecorder recorder) {
        this.metricsRecorder = recorder;
    }

    public MixinMinecraftClient(String string_1) {
        super(string_1, true);
    }

    @Inject(method = "run()V", at = @At("HEAD"))
    private void beforeRun(CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup()) EntryPointCatcher.displayInitErrorScreen();
    }

    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;emergencySaveAndCrash(Lnet/minecraft/CrashReport;)V", ordinal = 0))
    private CrashReport atTheEndOfFirstCatchBeforePrintingCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.enableGameloopCatching()) return report;

        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in first catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report);
        return report;
    }

    // Can't capture arg in inject so captured here
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;emergencySaveAndCrash(Lnet/minecraft/CrashReport;)V", ordinal = 1))
    private CrashReport atTheEndOfSecondCatchBeforePrintingCrashReport(CrashReport report) {
        if (!NotEnoughCrashes.enableGameloopCatching()) return report;

        NotEnoughCrashes.logDebug("Handling client game loop try/catch crash in second catch block");
        // we MUST use the report passed as parameter, because the field one only gets assigned in integrated server crashes.
        InGameCatcher.handleClientCrash(report);
        return report;
    }

    @Inject(method = "emergencySaveAndCrash(Lnet/minecraft/CrashReport;)V", at = @At("HEAD"))
    private void beforeEmergencySaveAndCrash(CrashReport report, CallbackInfo info) {
        if (NotEnoughCrashes.enableGameloopCatching()) {
            InGameCatcher.cleanupBeforeMinecraft();
        }
    }
}
