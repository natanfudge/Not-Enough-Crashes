package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.patches.PatchedClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.main.Main;
import net.minecraft.util.crash.CrashReport;

@Mixin(Main.class)
public class ClientMainMixin {

    @Redirect(method = "main",
                    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    private static void redirectPrintCrash(CrashReport crashReport) {
        ((PatchedClient) MinecraftClient.getInstance()).displayInitErrorScreen(crashReport);
    }

}
