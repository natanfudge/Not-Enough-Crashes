package fudge.notenoughcrashes;

import fudge.notenoughcrashes.config.MidnightConfig;
import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.config.NecMidnightConfig;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.NecPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NotEnoughCrashes {
    public static final Path DIRECTORY = NecPlatform.instance().getGameDirectory().resolve("not-enough-crashes");
    public static final String NAME = "Not Enough Crashes";
    public static final String MOD_ID = "notenoughcrashes";

    public static Logger getLogger() {
        // Create a new logger every time because Forge blocks loggers created in one context from working in another context.
        return LogManager.getLogger(NAME);
    }

    private static final boolean LOG_DEBUG = false;

    public static void logDebug(String message) {
        if (LOG_DEBUG) getLogger().error(message);
    }

    public static boolean enableGameloopCatching() {
        return NecConfig.getCurrent().catchGameloopCrashes() && !NecPlatform.instance().irisExists();
    }

    public static boolean enableEntrypointCatching() {
        return NecConfig.getCurrent().catchInitializationCrashes() && !NecPlatform.instance().irisExists();
    }

    public static CommonModMetadata getMetadata() {
        List<CommonModMetadata> mods = NecPlatform.instance().getModMetadatas(MOD_ID);
        if (mods.size() != 1) throw new IllegalStateException("NEC should have exactly one mod under its ID");
        return mods.get(0);
    }

    public static void ensureDirectoryExists() throws IOException {
        Files.createDirectories(DIRECTORY);
    }

    public static void initialize() {
//        if (NecConfig.getCurrent().forceCrashScreen()) SystemExitBlock.forbidSystemExitCall();
        MidnightConfig.init(NotEnoughCrashes.MOD_ID, NecMidnightConfig.class);
    }
}
