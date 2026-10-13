package io.github.natanfudge.nectest.mixin;
import io.github.natanfudge.nectest.MaintenanceScenario;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public class NeoMaintenanceClientMixin {
    @Inject(method="run", at=@At("HEAD"))
    private void start(CallbackInfo ci) throws java.io.IOException {
        MaintenanceScenario.register(java.nio.file.Files.readString(Minecraft.getInstance().gameDirectory.toPath().resolve("config/nec_test_mode.txt")).trim());
        MaintenanceScenario.recordStart();
    }
    @Inject(method="tick", at=@At("TAIL"))
    private void tick(CallbackInfo ci) { MaintenanceScenario.tick(Minecraft.getInstance()); }
}
