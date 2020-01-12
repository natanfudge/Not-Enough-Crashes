package fudge.notenoughcrashes.compat;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import io.github.giantnuker.fabric.loadcatcher.EntrypointHandler;
import io.github.giantnuker.fabric.loadcatcher.InitializationKind;

//import net.minecraft.test.TestFunctions;

public class NecEntryPointHandler implements EntrypointHandler {
    /**
     * @return foo
     */
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
