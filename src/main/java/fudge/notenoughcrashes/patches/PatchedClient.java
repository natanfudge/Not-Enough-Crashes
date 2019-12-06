package fudge.notenoughcrashes.patches;

import net.minecraft.util.crash.CrashReport;

public interface PatchedClient {

    void displayInitErrorScreen(CrashReport report);
}
