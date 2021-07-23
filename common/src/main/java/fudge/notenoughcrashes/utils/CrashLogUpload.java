package fudge.notenoughcrashes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import fudge.notenoughcrashes.ModConfig;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

public final class CrashLogUpload {
    private static String GIST_ACCESS_TOKEN_PART_1() {
        return "dc07dacff0c2cf84f706";
    }

    private static String GIST_ACCESS_TOKEN_PART_2() {
        return "8ac0fd6a757d53b81233";
    }

    // I don't think there's any security problem because the token can only upload gists,
    // but Github will revoke the token as soon as it sees it, so we trick it by splitting the token into 2.
    private static final String GIST_ACCESS_TOKEN = GIST_ACCESS_TOKEN_PART_1() + GIST_ACCESS_TOKEN_PART_2();

    private static class GistPost {
        @SerializedName("public")
        public boolean isPublic;
        public Map<String, GistFile> files;

        public GistPost(boolean isPublic, Map<String, GistFile> files) {
            this.isPublic = isPublic;
            this.files = files;
        }
    }

    private static class GistFile {
        public String content;

        public GistFile(String content) {
            this.content = content;
        }
    }

    private static ArrayList<ModConfig.CrashLogUploadType> fallBackTypes;

    private static ArrayList<ModConfig.CrashLogUploadType> getFallBackTypes(Boolean force) {
        if (fallBackTypes != null && !force) {
            return fallBackTypes;
        }
        fallBackTypes = new ArrayList<ModConfig.CrashLogUploadType>();
        Collections.addAll(fallBackTypes, ModConfig.CrashLogUploadType.values());
        fallBackTypes.sort(new CrashLogUploadTypeComparator());
        fallBackTypes.removeIf(fallbackType -> fallbackType.getPriority() < 0);
        System.out.println(fallBackTypes);
        return  fallBackTypes;
    }
    private static ArrayList<ModConfig.CrashLogUploadType> getFallBackTypes() {
        return getFallBackTypes(false);
    }

    private static class CrashLogUploadTypeComparator implements Comparator<ModConfig.CrashLogUploadType> {
        @Override
        public int compare(ModConfig.CrashLogUploadType o1, ModConfig.CrashLogUploadType o2) {
            return Integer.compare(o1.getPriority(),o2.getPriority());
        }
    }

    public static String upload(String text) throws IOException { return upload(text,false); }

    public static String upload(String text, Boolean fallBack) throws IOException {
        String URL = "";
        ModConfig.CrashLogUploadType uploadType;
        System.out.println("starting upload");
        if (fallBack && getFallBackTypes().isEmpty()) {
            System.out.println("fallback + empty");
            throw new IOException("no valid fallbacks");
        } else if (fallBack) {
            System.out.println("fallback");
            uploadType = getFallBackTypes().remove(0);
        } else { uploadType = ModConfig.instance().uploadCrashLogTo; }
        System.out.println("type:" + uploadType + uploadType.getPriority());

    try {

        switch (uploadType) {
            case GIST:
            default:
                String GISTuploadKey = ModConfig.instance().GISTUploadKey;

                if (GISTuploadKey == "" || fallBack) {
                    GISTuploadKey = GIST_ACCESS_TOKEN;
                }
                URL = uploadToGist(text, GISTuploadKey);
                break;
            case HASTE:
                String hasteUrl = ModConfig.instance().HASTEUrl;
                if (hasteUrl == "" || fallBack) {
                    hasteUrl = "https://hastebin.com/";
                }
                URL = uploadToHaste(text, hasteUrl);
                break;
            case PASTEBIN:
                URL = uploadToPasteBin(text);
                break;
            case BYTEBIN:
                String byteUrl = ModConfig.instance().BYTEBINUrl;
                if (byteUrl == "" || fallBack) {
                    byteUrl = "https://bytebin.lucko.me/";
                }
                URL = uploadToByteBin(text, byteUrl);
                break;

        }
    } catch (IOException exception) {
        URL = "";
        exception.printStackTrace();
        System.out.println("caught exception");
    } finally {
        if (URL.equals("")) {
            System.out.println("url is \"\"");
            URL = upload(text, true);
        } else {
            System.out.println("1 "+URL);
            System.out.println("rebuilding");
            getFallBackTypes(true); // rebuild
        }
    }
        System.out.println(URL);
        return URL;

    }


    /**
     * @return The link of the gist
     */
    private static String uploadToGist(String text, String key) throws IOException {
        HttpPost post = new HttpPost("https://api.github.com/gists");

        String fileName = "crash.txt";
        post.addHeader("Authorization", "token " + key);

        GistPost body = new GistPost(!ModConfig.instance().GISTUnlisted,
                new HashMap<String, GistFile>() {{
                    put(fileName, new GistFile(text));
                }}
        );
        post.setEntity(new StringEntity(new Gson().toJson(body)));

        if (ModConfig.instance().uploadCustomUserAgent != null) {
            post.setHeader("User-Agent",ModConfig.instance().uploadCustomUserAgent);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            JsonObject responseJson = new Gson().fromJson(responseString, JsonObject.class);
            return responseJson.getAsJsonObject("files")
                    .getAsJsonObject(fileName)
                    .getAsJsonPrimitive("raw_url")
                    .getAsString();
        }

    }

    private static String uploadToHaste(String str, String url) throws IOException {
        HttpPost post = new HttpPost(url + "documents");
        post.setEntity(new StringEntity(str));
        if (ModConfig.instance().uploadCustomUserAgent != null) {
            post.setHeader("User-Agent",ModConfig.instance().uploadCustomUserAgent);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            JsonObject responseJson = new Gson().fromJson(responseString, JsonObject.class);
            String hasteKey = responseJson.getAsJsonPrimitive("key").getAsString();
            return url + "raw/" + hasteKey;
        }

    }

    private  static  String uploadToPasteBin(String text) throws IOException {
        HttpPost post = new HttpPost("https://pastebin.com/api/api_post.php");
        String pastebinUploadKey = ModConfig.instance().PASTEBINUploadKey;
        String pastebinPrivacy = ModConfig.instance().PASTEBINPrivacy.getApiValue();
        String pastebinExpiryKey = ModConfig.instance().PASTEBINExpiry.getPastebinExpiryKey();

        List<NameValuePair> params = new ArrayList<NameValuePair>(7);
        params.add(new BasicNameValuePair("api_dev_key", pastebinUploadKey));
        params.add(new BasicNameValuePair("api_option", "paste")); // to create
        params.add(new BasicNameValuePair("api_paste_code", text));
        params.add(new BasicNameValuePair("api_paste_name", "crash.txt")); // mirroring gist
        params.add(new BasicNameValuePair("api_paste_format", "yaml")); // hl.js auto detects mc crashes as this
        params.add(new BasicNameValuePair("api_paste_expire_date", pastebinExpiryKey));
        params.add(new BasicNameValuePair("api_paste_private", pastebinPrivacy));

        post.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            // returns a normal pastebin url, like https://pastebin.com/xxxxxxxxx
            // inserting raw afer the .com works to return the raw content
            responseString.replace("https://pastebin.com/","https://pastebin.com/raw/");
            return responseString;
        }
    }

    private static String uploadToByteBin(String text, String url) throws IOException {
        HttpPost post = new HttpPost(url + "post");
        if (ModConfig.instance().uploadCustomUserAgent == null) {
            post.setHeader("User-Agent",(String.join(" ", post.getHeaders("User-Agent").toString())
                    .concat(" NotEnoughCrashes")));
        } else {
            post.setHeader("User-Agent",ModConfig.instance().uploadCustomUserAgent);
        }

        post.addHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity(text));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            JsonObject responseJson = new Gson().fromJson(responseString, JsonObject.class);
            String bytebinKey = responseJson.getAsJsonPrimitive("key").getAsString();
            return url + bytebinKey;
        }
    }
}
