package fudge.notenoughcrashes.patches;

import fudge.notenoughcrashes.platform.CommonModMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PatchedCrashReport {

    @NotNull
    Set<CommonModMetadata> getSuspectedMods();

    interface Element {

        String invokeGetName();

        String invokeGetDetail();
    }
}
