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
        trustIdenTrust();
        initStacktraceDeobfuscator();

//        TestBlock.init();
//        throw new NullPointerException();
//        TestKeyBinding.init();
    }

    private void trustIdenTrust() {
        // Trust the "IdenTrust DST Root CA X3" certificate (used by Let's Encrypt, which is used by paste.dimdev.org)
        try (InputStream keyStoreInputStream = NotEnoughCrashes.class.getResourceAsStream("/dst_root_ca_x3.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "password".toCharArray());
            SSLUtils.trustCertificates(keyStore);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
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
