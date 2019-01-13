package org.dimdev.toomanycrashes.mixins.client;

import net.minecraft.client.main.Main;
import org.dimdev.toomanycrashes.TooManyCrashes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Main.class)
public class MixinMain {
    static {
        TooManyCrashes.init();
    }
}
