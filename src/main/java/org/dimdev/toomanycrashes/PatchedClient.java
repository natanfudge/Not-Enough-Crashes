package org.dimdev.toomanycrashes;

import net.minecraft.util.crash.CrashReport;

public interface PatchedClient {

    void displayInitErrorScreen(CrashReport report);
}
