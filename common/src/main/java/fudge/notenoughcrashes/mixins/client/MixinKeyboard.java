package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
    /**
     * pollDebugCrash() keeps crashing the game when we display the crash screen, infinitely times over,
     * so we need to stop it from crashing after it has done its job just once.
     */
    @Inject(method = "pollDebugCrash()V", at = @At("HEAD"), cancellable = true)
    public void pollDebugCrashDontCrashInfinitely(CallbackInfo ci) {
        if (InGameCatcher.crashScreenActive) ci.cancel();
    }
}