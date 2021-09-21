package fudge.notenoughcrashes;

import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.NecPlatform;
import fudge.notenoughcrashes.utils.SystemExitBlock;
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

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final boolean LOG_DEBUG = false;

    public static void logDebug(String message) {
        if (LOG_DEBUG) LOGGER.info(message);
    }

    private static final boolean DEBUG_ENTRYPOINT = false;

    public static final boolean ENABLE_GAMELOOP_CATCHING = true;
    public static  boolean enableEntrypointCatching() {
        return (!NecPlatform.instance().isDevelopmentEnvironment() || DEBUG_ENTRYPOINT)
                && NecConfig.instance().catchInitializationCrashes;
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
        if (NecConfig.instance().forceCrashScreen) SystemExitBlock.forbidSystemExitCall();
        NecConfig.instance();

        if (DEBUG_ENTRYPOINT) throw new NullPointerException();
    }


}
