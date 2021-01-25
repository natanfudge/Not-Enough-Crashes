package fudge.notenoughcrashes.fabric;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.stacktrace.StacktraceDeobfuscator;
import net.fabricmc.api.ModInitializer;

public class NotEnoughCrashesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NotEnoughCrashes.initialize();
//        StacktraceDeobfuscator
    }
}
