package org.dimdev.toomanycrashes;

import net.fabricmc.loader.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.SSLUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class TooManyCrashes {
    private static final Logger LOGGER = LogManager.getLogger("TooManyCrashes");
    private static final long MAPPINGS_CACHE_DURATION = 2 * 24 * 60 * 60 * 1000;

    public static void init() {
        ModConfig.instance();
        trustIdenTrust();
        initStacktraceDeobfuscator();
    }

    private static void trustIdenTrust() {
        // Trust the "IdenTrust DST Root CA X3" certificate (used by Let's Encrypt, which is used by paste.dimdev.org)
        try (InputStream keyStoreInputStream = TooManyCrashes.class.getResourceAsStream("/dst_root_ca_x3.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "password".toCharArray());
            SSLUtils.trustCertificates(keyStore);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initStacktraceDeobfuscator() {
        File modDir = new File(FabricLoader.INSTANCE.getConfigDirectory(), "toomanycrashes");
        modDir.mkdirs();

        LOGGER.info("Initializing StacktraceDeobfuscator");
        try {
            File mappings = new File(modDir, "mappings-" + System.currentTimeMillis() / MAPPINGS_CACHE_DURATION + ".csv");
            if (mappings.exists()) {
                LOGGER.info("Found mappings: " + mappings.getName());
            } else {
                LOGGER.info("Downloading latest mappings to: " + mappings.getName());
            }
            StacktraceDeobfuscator.init(mappings);
        } catch (Exception e) {
            LOGGER.error("Failed to get mappings!", e);
        }
        LOGGER.info("Done initializing StacktraceDeobfuscator");

        // Install the log exception deobfuscation rewrite policy
        DeobfuscatingRewritePolicy.install();
    }
}
