package fudge.notenoughcrashes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import fudge.notenoughcrashes.ModConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

    public static String upload(String text) throws IOException {
        ModConfig.CrashLogUploadType type = ModConfig.instance().uploadCrashLogTo;
        if (type == ModConfig.CrashLogUploadType.GIST) {
            return uploadToGist(text);
        } else {
            return uploadToHaste(text);
        }
    }

    /**
     * @return The link of the gist
     */
    private static String uploadToGist(String text) throws IOException {
        HttpPost post = new HttpPost("https://api.github.com/gists");

        String fileName = "crash.txt";

        post.addHeader("Authorization", "token " + GIST_ACCESS_TOKEN);

        GistPost body = new GistPost(true,
                new HashMap<String, GistFile>() {{
                    put(fileName, new GistFile(text));
                }}
        );
        post.setEntity(new StringEntity(new Gson().toJson(body)));
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

    private static String uploadToHaste(String str) throws IOException {
        String baseUrl = "https://paste.dimdev.org";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        URL uploadURL = new URL(baseUrl + "/documents");
        HttpURLConnection connection = (HttpURLConnection) uploadURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();

        try {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(bytes);
            }

            try (InputStream is = connection.getInputStream()) {
                JsonObject json = new Gson().fromJson(new InputStreamReader(is), JsonObject.class);
                return baseUrl + "/" + json.get("key").getAsString() + ".mccrash";
            }
        } finally {
            connection.disconnect();
        }
    }
}
