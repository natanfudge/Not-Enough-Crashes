package org.dimdev.toomanycrashes;

import com.google.gson.Gson;
import net.fabricmc.loader.FabricLoader;

import java.io.*;

public class ModConfig {
    public static final File CONFIG_FILE = new File(FabricLoader.INSTANCE.getConfigDirectory(), "toomanycrashes.json");
    public static final Gson GSON = new Gson();
    private static ModConfig instance = new ModConfig();

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

        try {
            GSON.toJson(instance, new FileWriter(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instance;
    }
}
