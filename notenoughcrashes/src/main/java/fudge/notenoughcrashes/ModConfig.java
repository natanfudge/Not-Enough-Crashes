package fudge.notenoughcrashes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class ModConfig {

    public enum CrashLogUploadType {
        DIMDEV_HASTE,
        GIST
    }

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDirectory(), NotEnoughCrashes.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance = null;

    public CrashLogUploadType uploadCrashLogTo = CrashLogUploadType.GIST;
    public boolean disableReturnToMainMenu = false;
    public boolean deobfuscateStackTrace = true;

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
