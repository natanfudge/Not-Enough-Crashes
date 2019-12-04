package org.dimdev.toomanycrashes;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.toomanycrashes.test.TestBlock;
import org.dimdev.utils.SSLUtils;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

public class TooManyCrashes implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("TooManyCrashes");
    public static final Block EXAMPLE_BLOCK = new TestBlock();

    @Override
    public void onInitialize() {
        ModConfig.instance();
        trustIdenTrust();

        Registry.register(Registry.BLOCK, new Identifier("tutorial", "example_block"), EXAMPLE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("tutorial", "example_block"),
                        new BlockItem(EXAMPLE_BLOCK, new Item.Settings().group(ItemGroup.MISC)));

//        throw new NullPointerException();
        //initStacktraceDeobfuscator();
    }

    private void trustIdenTrust() {
        // Trust the "IdenTrust DST Root CA X3" certificate (used by Let's Encrypt, which is used by paste.dimdev.org)
        try (InputStream keyStoreInputStream = TooManyCrashes.class.getResourceAsStream("/dst_root_ca_x3.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "password".toCharArray());
            SSLUtils.trustCertificates(keyStore);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void initStacktraceDeobfuscator() {
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
