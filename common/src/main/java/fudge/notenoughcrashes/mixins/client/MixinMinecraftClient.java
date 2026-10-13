package fudge.notenoughcrashes.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.blaze3d.systems.RenderSystem;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.patches.MinecraftClientAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient implements MinecraftClientAccess {
    @Shadow private MetricsRecorder metricsRecorder;
    @Shadow private GpuSurface windowSurface;
    @Shadow private com.mojang.blaze3d.systems.TimerQuery timerQuery;

    @Override public MetricsRecorder getRecorder() { return metricsRecorder; }
    @Override public void setRecorder(MetricsRecorder recorder) { metricsRecorder = recorder; }

    @Override public void releaseCrashedFrame() {
        if (timerQuery != null && timerQuery.getStatus() == com.mojang.blaze3d.systems.TimerQuery.Status.STARTED) {
            timerQuery.endProfile();
        }
        // renderFrame refuses to render again while an interrupted frame owns the surface.
        if (windowSurface.isAcquired()) {
            if (!((GpuSurfaceAccessor) windowSurface).nec$hasBlittedTexture()) {
                var color = ((Minecraft) (Object) this).gameRenderer.mainRenderTarget().getColorTextureView();
                if (color != null) windowSurface.blitFromTexture(RenderSystem.getDevice().createCommandEncoder(), color);
            }
            RenderSystem.getDevice().createCommandEncoder().submit();
            windowSurface.present();
            RenderSystem.getDynamicUniforms().reset();
        }
    }

    @Inject(method = "run()V", at = @At("HEAD"))
    private void beforeRun(CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup()) EntryPointCatcher.displayInitErrorScreen();
    }

    @WrapOperation(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick(Z)V"))
    private void recoverTick(Minecraft client, boolean advanceGameTime, Operation<Void> original) throws Throwable {
        try {
            original.call(client, advanceGameTime);
        } catch (Throwable failure) {
            // Preserve vanilla's memory recovery and configured fatal-crash behavior.
            if (failure instanceof OutOfMemoryError || !NotEnoughCrashes.enableGameloopCatching()) throw failure;
            CrashReport report = failure instanceof ReportedException reported
                    ? reported.getReport() : CrashReport.forThrowable(failure, "Unexpected error");
            try {
                InGameCatcher.handleClientCrash(client.fillReport(report));
            } catch (Throwable recoveryFailure) {
                NotEnoughCrashes.getLogger().error("Crash recovery failed", recoveryFailure);
                if (recoveryFailure != report.getException()) report.getException().addSuppressed(recoveryFailure);
                throw new ReportedException(report);
            }
        }
    }
}
