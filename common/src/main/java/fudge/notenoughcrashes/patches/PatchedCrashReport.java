package fudge.notenoughcrashes.patches;

import fudge.notenoughcrashes.platform.CommonModMetadata;

import java.util.Set;

public interface PatchedCrashReport {

    Set<CommonModMetadata> getSuspectedMods();

    interface Element {

        String invokeGetName();

        String invokeGetDetail();
    }
}
