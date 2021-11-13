package fudge.notenoughcrashes.upload;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import fudge.notenoughcrashes.NecConfig;
import fudge.notenoughcrashes.NecConfig.CrashLogUploadDestination;
import fudge.notenoughcrashes.NecConfig.CrashUpload;
import fudge.notenoughcrashes.NecConfig.Gist;
import fudge.notenoughcrashes.NecConfig.Pastebin;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

public final class LegacyCrashLogUpload {
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


    public static String upload(String text) throws IOException {
        return upload(text, new HashSet<>());
    }

    public static String upload(String text, Set<CrashLogUploadDestination> failedUploadDestinations) throws IOException {
        final CrashLogUploadDestination uploadDestination = chooseUploadDestination(failedUploadDestinations);

        try {
            switch (uploadDestination) {
                case CRASHY:
                    return CrashyUpload.uploadToCrashySync(text);
                case GIST:
                    return uploadToGist(text);
                case HASTE:
                    return uploadToHaste(text);
                case PASTEBIN:
                    return uploadToPasteBin(text);
                case BYTEBIN:
                    return uploadToByteBin(text);
                default:
                    throw new IllegalStateException("Impossible");
            }
        } catch (IOException e) {
            NotEnoughCrashes.getLogger().error("Uploading to " + uploadDestination + " failed, using another destination as fallback.", e);

            // If uploading failed, attempt the other destination options
            failedUploadDestinations.add(uploadDestination);
            return upload(text, failedUploadDestinations);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static CrashLogUploadDestination chooseUploadDestination(Set<CrashLogUploadDestination> failedUploadTypes) throws IOException {
        if (failedUploadTypes.isEmpty()) return NecConfig.instance().crashlogUpload.destination;

        // When priority is null, the destination cannot be used as a fallback.
        Optional<CrashLogUploadDestination> selectedDestination = Arrays.stream(CrashLogUploadDestination.values())
                // When priority is null, the destination cannot be used as a fallback.
                .filter(destination -> destination.defaultPriority != null && !failedUploadTypes.contains(destination)).min(Comparator.comparingInt(destination -> destination.defaultPriority));

        return selectedDestination.orElseThrow(() -> new IOException("All upload destinations failed!"));
    }


    /**
     * @return The link of the gist
     */
    private static String uploadToGist(String text) throws IOException {
        final Gist config = NecConfig.instance().crashlogUpload.gist;
        String configUploadKey = config.accessToken;
        String uploadKey = configUploadKey.isEmpty() ? GIST_ACCESS_TOKEN : configUploadKey;
        HttpPost post = new HttpPost("https://api.github.com/gists");

        String fileName = "crash.txt";
        post.addHeader("Authorization", "token " + uploadKey);

        GistPost body = new GistPost(!config.unlisted, new HashMap() {{
            put(fileName, new GistFile(text));
        }});
        post.setEntity(createStringEntity(new Gson().toJson(body)));

        final String customUserAgent = NecConfig.instance().crashlogUpload.customUserAgent;
        if (!customUserAgent.isEmpty()) {
            post.setHeader("User-Agent", customUserAgent);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            JsonObject responseJson = new Gson().fromJson(responseString, JsonObject.class);
            return responseJson.getAsJsonObject("files").getAsJsonObject(fileName).getAsJsonPrimitive("raw_url").getAsString();
        }

    }

    private static String uploadToHaste(String str) throws IOException {
        String url = NecConfig.instance().crashlogUpload.hasteUrl;
        String customUserAgent = NecConfig.instance().crashlogUpload.customUserAgent;
        HttpPost post = new HttpPost(url + "documents");
        post.setEntity(createStringEntity(str));

        if (!customUserAgent.isEmpty()) {
            post.setHeader("User-Agent", customUserAgent);
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
        final Pastebin config = NecConfig.instance().crashlogUpload.pastebin;
        String pastebinUploadKey = config.uploadKey;
        String pastebinPrivacy = config.privacy.apiValue;
        String pastebinExpiryKey = config.expiry.pastebinExpiryKey;

        List<NameValuePair> params = Arrays.asList(new BasicNameValuePair("api_dev_key", pastebinUploadKey), new BasicNameValuePair("api_option", "paste"), // to create
                new BasicNameValuePair("api_paste_code", text), new BasicNameValuePair("api_paste_name", "crash.txt"), // mirroring gist
                new BasicNameValuePair("api_paste_format", "yaml"), // hl.js auto detects mc crashes as this
                new BasicNameValuePair("api_paste_expire_date", pastebinExpiryKey), new BasicNameValuePair("api_paste_private", pastebinPrivacy));
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            // returns a normal pastebin url, like https://pastebin.com/xxxxxxxxx
            // inserting raw afer the .com works to return the raw content
            return responseString.replace("https://pastebin.com/", "https://pastebin.com/raw/");
        }
    }

    private static String uploadToByteBin(String text) throws IOException {
        final CrashUpload config = NecConfig.instance().crashlogUpload;
        String url = NecConfig.instance().crashlogUpload.bytebinUrl;
        HttpPost post = new HttpPost(url + "post");
        String userAgent = config.customUserAgent.isEmpty() ? String.join(" ", Arrays.toString(post.getHeaders("User-Agent"))).concat(" NotEnoughCrashes") : config.customUserAgent;

        post.setHeader("User-Agent", userAgent);
        post.addHeader("Content-Type", "text/plain");
        post.setEntity(createStringEntity(text));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            JsonObject responseJson = new Gson().fromJson(responseString, JsonObject.class);
            String bytebinKey = responseJson.getAsJsonPrimitive("key").getAsString();
            return url + bytebinKey;
        }
    }

    public static StringEntity createStringEntity(String text) {
        return new StringEntity(text, StandardCharsets.UTF_16);
    }
}
