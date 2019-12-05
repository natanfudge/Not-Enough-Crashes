package fudge.notenoughcrashes;

import net.minecraft.util.crash.CrashReport;

public interface PatchedClient {

    void displayInitErrorScreen(CrashReport report);
}
