package fudge.notenoughcrashes.fabric.mixins.client;

import fudge.notenoughcrashes.platform.NecPlatformStorage;
import fudge.notenoughcrashes.fabric.platform.FabricPlatform;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinMain {
    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void asSoonAsPossible(String[] args, CallbackInfo ci){
        NecPlatformStorage.INSTANCE_SET_ONLY_BY_SPECIFIC_PLATFORMS_VERY_EARLY = new FabricPlatform();
    }
}
