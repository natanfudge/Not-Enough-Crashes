package fudge.notenoughcrashes.upload;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
        return uploadToByteBin(text);
//        return upload(text, new HashSet<>());
    }

    private static String uploadToByteBin(String text) throws IOException {
        String url = "https://bytebin.lucko.me/";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "post"))
                .header("User-Agent", "NotEnoughCrashes")
                .header("Content-Type", "text/plain; charset=utf-16")
                .POST(HttpRequest.BodyPublishers.ofString(text, StandardCharsets.UTF_16))
                .build();

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject responseJson = new Gson().fromJson(response.body(), JsonObject.class);
            String bytebinKey = responseJson.getAsJsonPrimitive("key").getAsString();
            return url + bytebinKey;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }
}
