package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.main.Main;

@Mixin(Main.class)
public class ClientMainMixin {
    //    @Inject(method = "main")
//    @Inject(method = "main",
//                    at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/MinecraftClient;run()V"))
//    private static void afterRun(String[] args, CallbackInfo ci) {
//        if (EntryPointCatcher.crashedDuringStartup()) EntryPointCatcher.displayInitErrorScreen();
//    }

//    @Redirect(method = "main",
//                    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
//    private static void redirectPrintCrash(CrashReport crashReport) {
//        ((PatchedClient) MinecraftClient.getInstance()).displayInitErrorScreen(crashReport);
//    }

}
