package fudge.notenoughcrashes.patches;

import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.Set;

public interface PatchedCrashReport {

    Set<ModMetadata> getSuspectedMods();

    interface Element {

        String invokeGetName();

        String invokeGetDetail();
    }
}
