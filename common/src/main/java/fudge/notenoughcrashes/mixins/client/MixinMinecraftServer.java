package fudge.notenoughcrashes.mixins.client;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//
///**
// * Mixin to stop the integrated server from ticking when the game has crashed
// */
//@Mixin(MinecraftServer.class)
//public class MixinMinecraftServer {
//    @Inject(method = "Lnet/minecraft/server/MinecraftServer;shouldKeepTicking()Z", at = @At("HEAD"), cancellable = true)
//    public void shouldKeepTickingStopTickOnCrash() {
//
//    }
//}
