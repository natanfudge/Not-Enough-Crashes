package fudge.notenoughcrashes.mixins.client;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.util.Identifier;

@Mixin(SplashScreen.class)
public interface SplashScreenMixin {
    @Accessor("LOGO")
    static void setLogo(Identifier logo) {
        throw new UnsupportedOperationException(); // Doesn't get run
    }
}
