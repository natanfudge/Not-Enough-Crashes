package fudge.notenoughcrashes.mixins.client;

import org.spongepowered.asm.mixin.Mixin;

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
