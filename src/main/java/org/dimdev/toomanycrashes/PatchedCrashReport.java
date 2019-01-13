package org.dimdev.toomanycrashes;

import net.fabricmc.loader.ModInfo;

import java.util.Set;

public interface PatchedCrashReport {
    Set<ModInfo> getSuspectedMods();

    interface Element {
        String invokeGetName();

        String invokeGetDetail();
    }
}
