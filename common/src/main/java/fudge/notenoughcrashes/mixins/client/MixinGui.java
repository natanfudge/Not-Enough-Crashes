package fudge.notenoughcrashes.mixins.client;

import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    /**
     * If the game has crashed, we set the screen to the init crash screen, but then Minecraft sets the screen back
     * to the title screen. We want to prevent that, to keep the screen set to the InitErrorScreen.
     */
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void setScreenDontResetCrashScreen(Screen screen, CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup() && !(screen instanceof InitErrorScreen)) ci.cancel();
    }
}
