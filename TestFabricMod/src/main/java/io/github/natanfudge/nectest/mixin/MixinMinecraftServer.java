package io.github.natanfudge.nectest.mixin;

import io.github.natanfudge.nectest.NecTestCrash;
import io.github.natanfudge.nectest.NecTestMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    private static boolean crashed = false;

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;endTickMetrics()V"))
    private void testServerCrash(CallbackInfo ci) {
        if (!crashed && NecTestMod.getTestMode().equals("server_crash")) {
            crashed = true;
            throw new NecTestCrash("Test server crash");
        }
    }

}
