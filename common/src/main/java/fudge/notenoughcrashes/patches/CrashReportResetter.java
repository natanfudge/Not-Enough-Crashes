package fudge.notenoughcrashes.patches;

import net.minecraft.CrashReport;

public interface CrashReportResetter {
    void nec$clearHandledCrash(CrashReport report);
}
