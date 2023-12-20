package fudge.notenoughcrashes.fabric.mixins.iris;

import fudge.notenoughcrashes.fabric.platform.FabricPlatform;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Courtesy of @TBiscuit
 */
@Mixin(Iris.class)
public class SilentNEC {

	@Inject(at = @At("TAIL"), method = "onEarlyInitialize", remap = false)
	private void onEarlyInitialize(CallbackInfo info) {
		var platform = (FabricPlatform)NecPlatform.instance();
		platform.setIrisExists();
	}
}