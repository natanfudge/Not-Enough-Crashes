package fudge.notenoughcrashes;

import fudge.notenoughcrashes.platform.NecPlatform;
import fudge.notenoughcrashes.test.TestBlock;
import fudge.notenoughcrashes.utils.SystemExitBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;


public class NotEnoughCrashes {

    public static final Path DIRECTORY = NecPlatform.instance().getGameDirectory().resolve("not-enough-crashes");
    public static final String NAME = "Not Enough Crashes";
    public static final String MOD_ID = "notenoughcrashes";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static final boolean DEBUG_ENTRYPOINT = false;
    private static final boolean DEBUG_GAMELOOP = false;
    public static final boolean FILTER_ENTRYPOINT_CATCHER = true;


    public static final boolean ENABLE_ENTRYPOINT_CATCHING = !NecPlatform.instance().isDevelopmentEnvironment() || DEBUG_ENTRYPOINT;

    public static void ensureDirectoryExists() throws IOException {
        Files.createDirectories(DIRECTORY);
    }

    public static void initialize() {
        if (ModConfig.instance().forceCrashScreen) SystemExitBlock.forbidSystemExitCall();
        ModConfig.instance();

        if (DEBUG_GAMELOOP) TestBlock.init();
        if (DEBUG_ENTRYPOINT) throw new NullPointerException();
//        TestKeyBinding.init();
    }


}
