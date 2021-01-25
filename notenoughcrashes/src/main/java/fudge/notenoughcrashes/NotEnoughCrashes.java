package fudge.notenoughcrashes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import fudge.notenoughcrashes.stacktrace.StacktraceDeobfuscator;
import fudge.notenoughcrashes.test.TestBlock;
import fudge.notenoughcrashes.utils.SSLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;


public class NotEnoughCrashes implements ModInitializer {

    public static final Path DIRECTORY = Paths.get(FabricLoader.getInstance().getGameDirectory().getAbsolutePath(),
                    "not-enough-crashes");
    public static final String NAME = "Not Enough Crashes";
    public static final String MOD_ID = "notenoughcrashes";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static final boolean DEBUG_DEOBF = false;
    private static final boolean DEBUG_ENTRYPOINT = false;
    public static final boolean FILTER_ENTRYPOINT_CATCHER = true;

    // No need to deobf in dev
    public static final boolean ENABLE_DEOBF = (!FabricLoader.getInstance().isDevelopmentEnvironment()
                    && ModConfig.instance().deobfuscateStackTrace) || DEBUG_DEOBF;

    public static final boolean ENABLE_ENTRYPOINT_CATCHING = !FabricLoader.getInstance().isDevelopmentEnvironment() || DEBUG_ENTRYPOINT;

    public static void ensureDirectoryExists() throws IOException {
        Files.createDirectories(DIRECTORY);
    }


    @Override
    public void onInitialize() {
        ModConfig.instance();
        initStacktraceDeobfuscator();

//        TestBlock.init();
        if (DEBUG_ENTRYPOINT) throw new NullPointerException();
//        TestKeyBinding.init();
    }


    private void initStacktraceDeobfuscator() {
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
