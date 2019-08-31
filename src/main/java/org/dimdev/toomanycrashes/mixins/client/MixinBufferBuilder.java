package org.dimdev.toomanycrashes.mixins.client;

import net.minecraft.client.render.BufferBuilder;
import org.dimdev.toomanycrashes.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements StateManager.IResettable {

    @Shadow private boolean building;

    @Shadow public abstract void end();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(int bufferSizeIn, CallbackInfo ci) {
        register();
    }

    @Override
    public void resetState() {
        if (building) {
            end();
        }
    }
}
