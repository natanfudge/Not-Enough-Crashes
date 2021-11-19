package io.github.natanfudge.nectest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NecTestMod implements ModInitializer {
    public static String getTestMode() {
        Path configDig = FabricLoader.getInstance().getConfigDir();
        Path testModePath = configDig.resolve("nec_test_mode.txt");
        try {
            Files.createDirectories(testModePath.getParent());
            if (!Files.exists(testModePath)) {
                Files.createFile(testModePath);
            }
            return new String(Files.readAllBytes(testModePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void onInitialize() {
        if (getTestMode().equals("init_crash")) {
            throw new NecTestCrash("Test Init Crash");
        }
    }
}

