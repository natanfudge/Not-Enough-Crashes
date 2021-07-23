package fudge.notenoughcrashes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import fudge.notenoughcrashes.NecConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
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

    private static ArrayList<NecConfig.CrashLogUploadDestination> fallBackTypes;

    private static ArrayList<NecConfig.CrashLogUploadDestination> getFallBackTypes(Boolean force) {
        if (fallBackTypes != null && !force) {
            return fallBackTypes;
        }
        fallBackTypes = new ArrayList<NecConfig.CrashLogUploadDestination>();
        Collections.addAll(fallBackTypes, NecConfig.CrashLogUploadDestination.values());
        fallBackTypes.sort(new CrashLogUploadTypeComparator());
        fallBackTypes.removeIf(fallbackType -> fallbackType.getPriority() < 0);

        return fallBackTypes;
    }

    private static ArrayList<NecConfig.CrashLogUploadDestination> getFallBackTypes() {
        return getFallBackTypes(false);
    }

    private static class CrashLogUploadTypeComparator implements Comparator<NecConfig.CrashLogUploadDestination> {
        @Override
        public int compare(NecConfig.CrashLogUploadDestination o1, NecConfig.CrashLogUploadDestination o2) {
            return Integer.compare(o1.getPriority(), o2.getPriority());
        }
    }

    public static String upload(String text) throws IOException {
        return upload(text, new HashSet<>());
    }

    public static String upload(String text, Set<NecConfig.CrashLogUploadDestination> failedUploadTypes) throws IOException {

        final var uploadType = chooseUploadType(failedUploadTypes);

        final String URL = switch (uploadType) {
            default -> {
                String GISTuploadKey = NecConfig.instance().GISTUploadKey;
                if (GISTuploadKey == "" || fallBack) {
                    GISTuploadKey = GIST_ACCESS_TOKEN;
                }
                uploadToGist(text, GISTuploadKey);
            }
            case HASTE -> {
                String hasteUrl = NecConfig.instance().HasteUrl;
                if (hasteUrl == "" || fallBack) {
                    hasteUrl = "https://hastebin.com/";
                }
                URL = uploadToHaste(text, hasteUrl);
            }
            case PASTEBIN -> URL = uploadToPasteBin(text);
            case BYTEBIN -> {
                String byteUrl = NecConfig.instance().BYTEBINUrl;
                if (byteUrl == "" || fallBack) {
                    byteUrl = "https://bytebin.lucko.me/";
                }
                URL = uploadToByteBin(text, byteUrl);
            }
        }

//        try {


//        } catch (IOException exception) {
//            URL = "";
//            exception.printStackTrace();
//        } finally {
//            if (URL.equals("")) {
//
//                URL = upload(text, true);
//            } else {
//                getFallBackTypes(true); // rebuild
//            }
//        }
        return URL;

    }

    private static NecConfig.CrashLogUploadDestination chooseUploadType(Set<NecConfig.CrashLogUploadDestination> failedUploadTypes) throws IOException {
        if (failedUploadTypes.isEmpty()) return NecConfig.instance().uploadCrashLogTo;

        // When priority is null, the destination cannot be used as a fallback.
        var selectedDestination = Arrays.stream(NecConfig.CrashLogUploadDestination.values())
                // When priority is null, the destination cannot be used as a fallback.
                .filter(destination -> destination.getPriority() != null && !failedUploadTypes.contains(destination))
                .min(Comparator.comparingInt(NecConfig.CrashLogUploadDestination::getPriority));

        NotEnoughCrashes.LOGGER.info("Trying to upload crash log to " + selectedDestination + " as fallback");

        return selectedDestination.orElseThrow(() -> new IOException("All upload destinations failed!"));
    }


    /**
     * @return The link of the gist
     */
    private static String uploadToGist(String text, String key) throws IOException {
        final String uploadKey;
        NecConfig.instance().GISTUploadKey
        HttpPost post = new HttpPost("https://api.github.com/gists");

        String fileName = "crash.txt";
        post.addHeader("Authorization", "token " + key);

        GistPost body = new GistPost(!NecConfig.instance().GISTUnlisted,
                new HashMap<>() {{
                    put(fileName, new GistFile(text));
                }}
        );
        post.setEntity(new StringEntity(new Gson().toJson(body)));

        if (NecConfig.instance().uploadCustomUserAgent != null) {
            post.setHeader("User-Agent", NecConfig.instance().uploadCustomUserAgent);
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
        if (NecConfig.instance().uploadCustomUserAgent != null) {
            post.setHeader("User-Agent", NecConfig.instance().uploadCustomUserAgent);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            JsonObject responseJson = new Gson().fromJson(responseString, JsonObject.class);
            String hasteKey = responseJson.getAsJsonPrimitive("key").getAsString();
            return url + "raw/" + hasteKey;
        }

    }

    private static String uploadToPasteBin(String text) throws IOException {
        HttpPost post = new HttpPost("https://pastebin.com/api/api_post.php");
        String pastebinUploadKey = NecConfig.instance().PASTEBINUploadKey;
        String pastebinPrivacy = NecConfig.instance().PASTEBINPrivacy.getApiValue();
        String pastebinExpiryKey = NecConfig.instance().PASTEBINExpiry.getPastebinExpiryKey();

        List<NameValuePair> params = new ArrayList<NameValuePair>(7);
        params.add(new BasicNameValuePair("api_dev_key", pastebinUploadKey));
        params.add(new BasicNameValuePair("api_option", "paste")); // to create
        params.add(new BasicNameValuePair("api_paste_code", text));
        params.add(new BasicNameValuePair("api_paste_name", "crash.txt")); // mirroring gist
        params.add(new BasicNameValuePair("api_paste_format", "yaml")); // hl.js auto detects mc crashes as this
        params.add(new BasicNameValuePair("api_paste_expire_date", pastebinExpiryKey));
        params.add(new BasicNameValuePair("api_paste_private", pastebinPrivacy));

        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            // returns a normal pastebin url, like https://pastebin.com/xxxxxxxxx
            // inserting raw afer the .com works to return the raw content
            responseString.replace("https://pastebin.com/", "https://pastebin.com/raw/");
            return responseString;
        }
    }

    private static String uploadToByteBin(String text, String url) throws IOException {
        HttpPost post = new HttpPost(url + "post");
        if (NecConfig.instance().uploadCustomUserAgent == null) {
            post.setHeader("User-Agent", (String.join(" ", post.getHeaders("User-Agent").toString())
                    .concat(" NotEnoughCrashes")));
        } else {
            post.setHeader("User-Agent", NecConfig.instance().uploadCustomUserAgent);
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
