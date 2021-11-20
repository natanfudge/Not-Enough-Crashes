package fudge.notenoughcrashes.mixins.client;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

/**
 * Only applied to client side because we aim to change the functionality of the integrated server (and not the dedicated one)
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServerClientOnly {
    /**
     * We write the log anyway using CrashUtils.outputReport in
     * {@link fudge.notenoughcrashes.mixinhandlers.InGameCatcher#displayCrashScreen(CrashReport, int)}
     */
    @Redirect(method = "runServer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReport;writeToFile(Ljava/io/File;)Z"))
    private boolean disableIntegratedServerWriteToFileOnCrash(CrashReport instance, File file) {
        return true;
    }
}
