package fudge.notenoughcrashes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fudge.notenoughcrashes.platform.NecPlatform;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class NecConfig {

    private static final File CONFIG_FILE = new File(NecPlatform.instance().getConfigDirectory().toFile(), NotEnoughCrashes.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NecConfig instance = null;

    public enum CrashLogUploadDestination {
        GIST(3), // attempt last
        HASTE(2),
        PASTEBIN(null), // requires configuration
        BYTEBIN(1);

        final Integer defaultPriority;

        CrashLogUploadDestination(@Nullable Integer defaultPriority) { this.defaultPriority = defaultPriority; }
    }

    public enum PastebinPrivacy {
        PUBLIC("0"), //anyone can see it, appears in recently created
        UNLISTED("1"); // only people with the link can see it
//        PRIVATE(2) // only you can see it (doesn't make much sense). this doesn't allow for raw download, so disabling
        final String apiValue;
        PastebinPrivacy(String apiValue) {
            this.apiValue = apiValue;
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

         final String pastebinExpiryKey;
        PastebinExpiry(String pastebinExpiry) {
            this.pastebinExpiryKey = pastebinExpiry;
        }

    }




    public CrashLogUploadDestination uploadCrashLogTo = CrashLogUploadDestination.GIST;
    public String uploadCustomUserAgent = null;

    //TODO: first do without records, then try to make it records.
    static record GistConfig(String uploadKey, boolean unlisted){}
    public GistConfig gist;
    public String HasteUrl = "https://hastebin.com/";

    static record PastebinConfig(String uploadKey, PastebinPrivacy)
    public String PASTEBINUploadKey = "";
    public PastebinPrivacy PASTEBINPrivacy = PastebinPrivacy.PUBLIC;
    public PastebinExpiry PASTEBINExpiry = PastebinExpiry.NEVER;

    public String BYTEBINUrl = "https://bytebin.lucko.me/";

    public boolean disableReturnToMainMenu = false;
    public boolean deobfuscateStackTrace = true;
    public boolean debugModIdentification = false;
    public boolean forceCrashScreen = false;


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
}
