package org.dimdev.toomanycrashes.mixins.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.main.Main;
import net.minecraft.util.crash.CrashReport;
import org.dimdev.toomanycrashes.PatchedClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Main.class)
public class ClientMainMixin {

    @Redirect(method = "main",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    private static void redirectPrintCrash(CrashReport crashReport) {
        ((PatchedClient) MinecraftClient.getInstance()).displayInitErrorScreen(crashReport);
    }


//    @Inject(method = "main",
//                    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V")
//    , cancellable = true, locals = LocalCapture.PRINT
//    )
//    private static void beforePrintingCrashReport(String[] mainArgs, CallbackInfo ci) {
////        ((PatchedClient) MinecraftClient.getInstance()).displayInitErrorScreen(crashReport);
//        ci.cancel();
//    }

//    @Inject(method = "main",
//                    at= @At(value = "INVOKE",
//                                    target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V")))
//    protected void  beforePrintingCrashReport(CrashReport crashReport, CallbackInfo ci) {
//
//    }
}
