package fudge.notenoughcrashes;

import fudge.notenoughcrashes.platform.NecPlatform;
import fudge.notenoughcrashes.stacktrace.StacktraceDeobfuscator;
import fudge.notenoughcrashes.test.TestBlock;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class NotEnoughCrashes {

    public static final Path DIRECTORY = NecPlatform.instance().getGameDirectory().resolve("not-enough-crashes");
    public static final String NAME = "Not Enough Crashes";
    public static final String MOD_ID = "notenoughcrashes";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static final boolean DEBUG_DEOBF = false;
    private static final boolean DEBUG_ENTRYPOINT = true;
    private static final boolean DEBUG_GAMELOOP = false;
    public static final boolean FILTER_ENTRYPOINT_CATCHER = true;

    // No need to deobf in dev
    public static final boolean ENABLE_DEOBF = (!NecPlatform.instance().isDevelopmentEnvironment()
            && ModConfig.instance().deobfuscateStackTrace) || DEBUG_DEOBF;

    public static final boolean ENABLE_ENTRYPOINT_CATCHING = !NecPlatform.instance().isDevelopmentEnvironment() || DEBUG_ENTRYPOINT;

    public static void ensureDirectoryExists() throws IOException {
        Files.createDirectories(DIRECTORY);
    }

    public static void initialize() {
        ModConfig.instance();
        initStacktraceDeobfuscator();

//        if (DEBUG_GAMELOOP) TestBlock.init();
        if (DEBUG_ENTRYPOINT) throw new NullPointerException();
//        TestKeyBinding.init();
    }


    private static void initStacktraceDeobfuscator() {
        if (!ENABLE_DEOBF) return;
        LOGGER.info("Initializing StacktraceDeobfuscator");
        try {
            StacktraceDeobfuscator.init();
        } catch (Exception e) {
            LOGGER.error("Failed to load mappings!", e);
        }
        LOGGER.info("Done initializing StacktraceDeobfuscator");

        // Install the log exception deobfuscation rewrite policy
        DeobfuscatingRewritePolicy.install();
    }
}
