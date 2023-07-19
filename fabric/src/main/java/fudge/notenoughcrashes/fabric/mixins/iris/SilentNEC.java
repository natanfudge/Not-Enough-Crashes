package fudge.notenoughcrashes.fabric.mixins.iris;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.fabric.platform.FabricPlatform;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Courtesy of @TBiscuit
 */
@Mixin(Iris.class)
public class SilentNEC {
	@Shadow(remap = false)
	private static boolean hasNEC;

	@Inject(at = @At("TAIL"), method = "onEarlyInitialize", remap = false)
	private void onEarlyInitialize(CallbackInfo info) {
		var platform = (FabricPlatform)NecPlatform.instance();
		platform.setIrisExists();
		hasNEC = false; // NEC doesn't exist Iris, it doesn't exist...
	}
}