package fudge.notenoughcrashes.mixins.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fudge.notenoughcrashes.patches.CrashReportResetter;
import net.minecraft.CrashReport;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(BlockableEventLoop.class)
public abstract class MixinBlockableEventLoop implements CrashReportResetter {
    @Shadow private static volatile Supplier<CrashReport> delayedCrash;

    @Override
    public void nec$clearHandledCrash(CrashReport report) {
        // Vanilla leaves this shared report installed because it expects the process to exit.
        // Disconnecting drains tasks, which otherwise immediately throws the same report again.
        synchronized (BlockableEventLoop.class) {
            if (delayedCrash != null && delayedCrash.get() == report) delayedCrash = null;
        }
    }

    @WrapMethod(method = "delayCrash(Lnet/minecraft/CrashReport;)V")
    private void synchronizeDelayedCrash(CrashReport report, Operation<Void> original) {
        // relayDelayCrash already uses this monitor. Coordinate the other writer as well,
        // so clearing a handled report cannot erase a newly submitted one.
        synchronized (BlockableEventLoop.class) {
            original.call(report);
        }
    }
}
