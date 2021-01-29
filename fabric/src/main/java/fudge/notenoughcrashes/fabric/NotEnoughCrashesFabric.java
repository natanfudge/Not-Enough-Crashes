package fudge.notenoughcrashes.fabric;

import fudge.notenoughcrashes.ModConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.api.ModInitializer;

public class NotEnoughCrashesFabric implements ModInitializer {

    // No need to deobf in dev
    private static final boolean DEBUG_DEOBF = false;
    public static final boolean ENABLE_DEOBF = (!NecPlatform.instance().isDevelopmentEnvironment()
            && ModConfig.instance().deobfuscateStackTrace) || DEBUG_DEOBF;

    @Override
    public void onInitialize() {
        NotEnoughCrashes.initialize();
        initStacktraceDeobfuscator();
    }

    private static void initStacktraceDeobfuscator() {
        if (!ENABLE_DEOBF) return;
        NotEnoughCrashes.LOGGER.info("Initializing StacktraceDeobfuscator");
        try {
            StacktraceDeobfuscator.init();
        } catch (Exception e) {
            NotEnoughCrashes.LOGGER.error("Failed to load mappings!", e);
        }
        NotEnoughCrashes.LOGGER.info("Done initializing StacktraceDeobfuscator");

        // Install the log exception deobfuscation rewrite policy
        DeobfuscatingRewritePolicy.install();
    }
}
