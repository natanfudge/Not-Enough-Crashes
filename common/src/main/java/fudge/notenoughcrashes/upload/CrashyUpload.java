package fudge.notenoughcrashes.upload;

import com.google.gson.Gson;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.utils.Java11;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

public class CrashyUpload {
    private static final Path CRASH_CODES_PATH = NotEnoughCrashes.DIRECTORY.resolve("Uploaded Crash logs.txt");

    private static final boolean localTesting = false;

    public static CompletableFuture<String> uploadToCrashy(String text) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String prefix = localTesting ? "http://localhost:5001/crashy-9dd87/europe-west1" : "https://europe-west1-crashy-9dd87.cloudfunctions.net";
            HttpPost post = new HttpPost(prefix + "/uploadCrash");
            post.setHeader("content-type", "application/gzip");
            post.setEntity(new ByteArrayEntity(gzip(text)));
            CloseableHttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody;
            try {
                responseBody = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:
                    UploadCrashSuccess responseObject = new Gson().fromJson(responseBody, UploadCrashSuccess.class);
                    try {
                        rememberCrashCode(responseObject.crashId, responseObject.key);
                    } catch (IOException e) {
                        NotEnoughCrashes.getLogger().error("Could not remember crash code when uploading crash " + responseObject.crashId, e);
                    }
                    return CompletableFuture.supplyAsync(() -> localTesting ?
                            responseObject.crashUrl.replace("https://crashy.net", "http://localhost:3000") : responseObject.crashUrl);
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    throw new UploadToCrashyError.InvalidCrash();
                case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
                    throw new UploadToCrashyError.TooLarge();
                default:
                    throw new IllegalStateException("Unexpected status code when uploading to crashy: " + statusCode + " message: " + responseBody);
            }

        }
    }

    public static String uploadToCrashySync(String text) throws IOException, ExecutionException, InterruptedException {
        return uploadToCrashy(text).get();
    }

    private static byte[] gzip(String string) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(string.getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        }
    }

    private static void rememberCrashCode(String id, String code) throws IOException {
        NotEnoughCrashes.ensureDirectoryExists();
        final String oldCodes;
        if (Files.exists(CRASH_CODES_PATH)) {
            oldCodes = Java11.readString(CRASH_CODES_PATH);
        } else {
            oldCodes = "";
        }
        Java11.writeString(CRASH_CODES_PATH, oldCodes + id + ": " + code + "\n");
    }

    static class UploadCrashSuccess {
        String crashId;
        String key;
        String crashUrl;
    }

}
