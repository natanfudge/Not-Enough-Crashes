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
     * KeyboardHandler.tick() keeps checking the debug-crash shortcut while we display the crash screen,
     * so we need to stop it from crashing after it has done its job just once.
     */
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    public void tickDontCrashInfinitely(CallbackInfo ci) {
        if (InGameCatcher.crashScreenActive) ci.cancel();
    }
}
