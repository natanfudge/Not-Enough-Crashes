package fudge.notenoughcrashes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fudge.notenoughcrashes.platform.NecPlatform;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class NecConfig {
    /******************************
     *  CONFIG
     *******************************/
    public boolean disableReturnToMainMenu = false;
    public boolean deobfuscateStackTrace = true;
    public boolean catchInitializationCrashes = true;
    public boolean debugModIdentification = false;
    public boolean forceCrashScreen = false;
    public int crashLimit = 20;
    public CrashUpload crashlogUpload = new CrashUpload();

    public static class CrashUpload {
        public CrashLogUploadDestination destination = CrashLogUploadDestination.BYTEBIN;
        public String hasteUrl = "https://hastebin.com/";
        public String bytebinUrl = "https://bytebin.lucko.me/";
        public Gist gist = new Gist();
        public Pastebin pastebin = new Pastebin();
        public String customUserAgent = "";
    }


    public enum CrashLogUploadDestination {
        GIST(3), // attempt last
        HASTE(2),
        PASTEBIN(null), // requires configuration
        BYTEBIN(1),
        CRASHY(0);

        public final Integer defaultPriority;

        CrashLogUploadDestination(@Nullable Integer defaultPriority) {
            this.defaultPriority = defaultPriority;
        }
    }

    public static class Gist {
        public String accessToken = "";
        public boolean unlisted = false;
    }

    public static class Pastebin {
        public enum Privacy {
            PUBLIC("0"), //anyone can see it, appears in recently created
            UNLISTED("1"); // only people with the link can see it
            //        PRIVATE(2) // only you can see it (doesn't make much sense). this doesn't allow for raw download, so disabling
            public final String apiValue;

            Privacy(String apiValue) {
                this.apiValue = apiValue;
            }
        }

        public enum Expiry {
            NEVER("N"),
            TENMIN("10M"),
            ONEHOUR("1H"),
            ONEDAY("1D"),
            ONEWEEK("1W"),
            TWOWEEK("2W"),
            ONEMONTH("1M"),
            SIXMONTH("6M"),
            ONEYEAR("1Y");

            public final String pastebinExpiryKey;

            Expiry(String pastebinExpiry) {
                this.pastebinExpiryKey = pastebinExpiry;
            }
        }

        public String uploadKey = "";
        public Privacy privacy = Privacy.PUBLIC;
        public Expiry expiry = Expiry.NEVER;
    }

    public static NecConfig instance() {
        if (instance != null) {
            return instance;
        }

        if (CONFIG_FILE.exists()) {
            try {
                return instance = new Gson().fromJson(new FileReader(CONFIG_FILE), NecConfig.class);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        instance = new NecConfig();

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return instance;
    }

    private static final File CONFIG_FILE = new File(NecPlatform.instance().getConfigDirectory().toFile(), NotEnoughCrashes.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NecConfig instance = null;
}
