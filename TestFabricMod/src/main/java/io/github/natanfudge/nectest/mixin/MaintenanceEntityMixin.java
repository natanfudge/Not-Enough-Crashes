package io.github.natanfudge.nectest.mixin;

import io.github.natanfudge.nectest.MaintenanceScenario;
import io.github.natanfudge.nectest.NecTestCrash;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MaintenanceEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void entityCrash(CallbackInfo ci) {
        if (MaintenanceScenario.consumeCrash("maintenance_entity")) throw new NecTestCrash("NEC entity tick crash");
    }
}
