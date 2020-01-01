package fudge.notenoughcrashes.compat;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import io.github.giantnuker.fabric.loadcatcher.EntrypointHandler;
import io.github.giantnuker.fabric.loadcatcher.InitializationKind;

public class NecEntryPointHandler implements EntrypointHandler {
    @Override
    public boolean onModInitializationThrowed(Throwable throwable, InitializationKind initializationKind) {
        if (initializationKind != InitializationKind.ALL_CLIENT_ENTRIES && initializationKind != InitializationKind.ALL_COMMON_ENTRIES) {
            return false;
        }
        if (!NotEnoughCrashes.ENABLE_ENTRYPOINT_CATCHING) return false;

        EntryPointCatcher.handleEntryPointError(throwable);
        return true;
    }
}
