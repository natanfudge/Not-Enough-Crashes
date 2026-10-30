package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.mixinhandlers.RecoveryExclusions;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer {
    @Inject(method = "onServerCrash(Lnet/minecraft/CrashReport;)V", at = @At("HEAD"), cancellable = true)
    private void handleIntegratedServerCrash(CrashReport report, CallbackInfo ci) {
        IntegratedServer server = (IntegratedServer) (Object) this;
        if (!NotEnoughCrashes.enableGameloopCatching() || !server.isReady()
                || RecoveryExclusions.isExcluded(report.getException())) return;

        Minecraft.getInstance().execute(() -> InGameCatcher.handleServerCrash(report));
        ci.cancel();
    }
}
