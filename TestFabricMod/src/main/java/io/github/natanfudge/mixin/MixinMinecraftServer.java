package io.github.natanfudge.mixin;

import io.github.natanfudge.NecTestCrash;
import io.github.natanfudge.NecTestMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    private static boolean crashed = false;

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;endMonitor()V"))
    private void testServerCrash(CallbackInfo ci) {
        if (!crashed && NecTestMod.getTestMode().equals("server_crash")) {
            crashed = true;
            throw new NecTestCrash("Test server crash");
        }
    }

}
