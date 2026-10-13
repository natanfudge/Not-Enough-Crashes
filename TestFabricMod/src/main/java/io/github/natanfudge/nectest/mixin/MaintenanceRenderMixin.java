package io.github.natanfudge.nectest.mixin;

import io.github.natanfudge.nectest.MaintenanceScenario;
import io.github.natanfudge.nectest.NecTestCrash;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MaintenanceRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void renderCrash(CallbackInfo ci) {
        if (MaintenanceScenario.consumeCrash("maintenance_render")) {
            throw new NecTestCrash("NEC injected render crash");
        }
    }
}
