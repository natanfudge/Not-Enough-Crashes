package org.dimdev.toomanycrashes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.FabricLoader;

import java.io.*;

public class ModConfig {
    private static final File CONFIG_FILE = new File(FabricLoader.INSTANCE.getConfigDirectory(), "toomanycrashes.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance = null;

    public String hasteURL = "https://paste.dimdev.org";
    public boolean disableReturnToMainMenu = false;

    public static ModConfig instance() {
        if (instance != null) {
            return instance;
        }

        if (CONFIG_FILE.exists()) {
            try {
                return instance = new Gson().fromJson(new FileReader(CONFIG_FILE), ModConfig.class);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        instance = new ModConfig();

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return instance;
    }
}
