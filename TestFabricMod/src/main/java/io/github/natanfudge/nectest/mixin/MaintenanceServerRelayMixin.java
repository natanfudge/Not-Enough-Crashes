package io.github.natanfudge.nectest.mixin;

import io.github.natanfudge.nectest.MaintenanceScenario;
import net.minecraft.CrashReport;
import net.minecraft.server.MinecraftServer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Exercise vanilla's shared crash relay as well as NEC's direct server callback. */
@Mixin(MinecraftServer.class)
public class MaintenanceServerRelayMixin {
    @WrapOperation(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerCrash(Lnet/minecraft/CrashReport;)V"))
    private void relayUpdateOverflow(MinecraftServer server, CrashReport report, Operation<Void> original) {
        if (!MaintenanceScenario.consumeCrash("maintenance_updates_delayed")) {
            original.call(server, report);
            return;
        }
        if (!(report.getException() instanceof StackOverflowError)
                || !report.getTitle().equals("Exception while updating neighbours")) {
            throw new AssertionError("Expected actual neighbor-update stack exhaustion");
        }
        System.out.println("NEC_TEST actual neighbor-update stack overflow; using vanilla delayed relay");
        MaintenanceScenario.delayedUpdateRelayed = true;
        BlockableEventLoop.relayDelayCrash(report);
    }
}
