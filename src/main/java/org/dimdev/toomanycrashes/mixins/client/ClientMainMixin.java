package org.dimdev.toomanycrashes.mixins.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.main.Main;
import net.minecraft.util.crash.CrashReport;
import org.dimdev.toomanycrashes.PatchedClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class ClientMainMixin {

    @Redirect(method = "main([Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    private static void redirectPrintCrash(MinecraftClient client, CrashReport crashReport) {
        ((PatchedClient) client).displayInitErrorScreen(crashReport);
    }
}
