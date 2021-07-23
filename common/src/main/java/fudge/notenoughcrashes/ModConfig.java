package fudge.notenoughcrashes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fudge.notenoughcrashes.platform.NecPlatform;

import java.io.*;

public class ModConfig {

    public enum CrashLogUploadType {
        GIST(5), // attempt last
        HASTE(2),
        PASTEBIN(), // requires configuration
        BYTEBIN(1);

        private final int defaultPriority;

        CrashLogUploadType(int defaultPriority) { this.defaultPriority = defaultPriority; }
        CrashLogUploadType() { this.defaultPriority = Integer.MIN_VALUE; }
        public int getPriority() { return this.defaultPriority; }
    }

    public enum PastebinPrivacy {
        PUBLIC("0"), //anyone can see it, appears in recently created
        UNLISTED("1"); // only people with the link can see it
//        PRIVATE(2) // only you can see it (doesn't make much sense). this doesn't allow for raw download, so disabling

        private final String apiValue;

        PastebinPrivacy(String apiValue) {
            this.apiValue = apiValue;
        }

        public String getApiValue() {
            return this.apiValue;
        }
    }

    public enum PastebinExpiry {
        NEVER("N"),
        TENMIN("10M"),
        ONEHOUR("1H"),
        ONEDAY("1D"),
        ONEWEEK("1W"),
        TWOWEEK("2W"),
        ONEMONTH("1M"),
        SIXMONTH("6M"),
        ONEYEAR("1Y");

        private final String pastebinExpiryKey;
        PastebinExpiry(String pastebinExpiry) {
            this.pastebinExpiryKey = pastebinExpiry;
        }

        public String getPastebinExpiryKey() {
            return pastebinExpiryKey;
        }
    }
    private static final File CONFIG_FILE = new File(NecPlatform.instance().getConfigDirectory().toFile(), NotEnoughCrashes.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance = null;



    public CrashLogUploadType uploadCrashLogTo = CrashLogUploadType.GIST;
    public String uploadCustomUserAgent = null;

    public String GISTUploadKey = "";
    public boolean GISTUnlisted = false;

    public String HASTEUrl = "https://hastebin.com/";

    public String PASTEBINUploadKey = "";
    public PastebinPrivacy PASTEBINPrivacy = PastebinPrivacy.PUBLIC;
    public PastebinExpiry PASTEBINExpiry = PastebinExpiry.NEVER;

    public String BYTEBINUrl = "https://bytebin.lucko.me/";

    public boolean disableReturnToMainMenu = false;
    public boolean deobfuscateStackTrace = true;
    public boolean debugModIdentification = false;
    public boolean forceCrashScreen = false;


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
