package io.github.natanfudge.nectest;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;
import java.util.function.Supplier;

public final class MaintenanceDelayedCrash {
    /** Run at the title screen before a server exists, so temporary reports cannot escape. */
    public static void verifyMatchingReport(Minecraft client) {
        try {
            var pending = BlockableEventLoop.class.getDeclaredField("delayedCrash");
            pending.setAccessible(true);
            if (pending.get(null) != null) throw new AssertionError("Unexpected pending crash before test");
            var clear = client.getClass().getMethod("nec$clearHandledCrash", CrashReport.class);
            var handled = CrashReport.forThrowable(new IllegalStateException("Handled test crash"), "Handled");
            var newer = CrashReport.forThrowable(new IllegalStateException("Newer test crash"), "Newer");
            client.delayCrash(newer);
            clear.invoke(client, handled);
            if (!(pending.get(null) instanceof Supplier<?> supplier) || supplier.get() != newer) {
                throw new AssertionError("Clearing an older crash discarded a different pending report");
            }
            clear.invoke(client, newer);
            if (pending.get(null) != null) throw new AssertionError("Handled crash was not cleared");
            clear.invoke(client, newer);
            System.out.println("NEC_TEST PASS matching report reset; preserves unrelated report; repeated reset safe");
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect delayed crash recovery", exception);
        }
    }
}
