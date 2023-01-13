package fudge.notenoughcrashes.upload;

import com.google.gson.Gson;
import fudge.notenoughcrashes.NotEnoughCrashes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("ConstantValue")
public class CrashyUpload {

    private enum CrashyMode {
        LOCAL, BETA, RELEASE
    }

    private static final CrashyMode CRASHY_MODE = CrashyMode.BETA;
    private static final String crashyDomain = CRASHY_MODE == CrashyMode.RELEASE ? "crashy.net" :
            CRASHY_MODE == CrashyMode.BETA ? "beta.crashy.net" :
                    "localhost:80";

    private static final String http = CRASHY_MODE == CrashyMode.LOCAL ? "http" :"https";

    private static final Path CRASH_CODES_PATH = NotEnoughCrashes.DIRECTORY.resolve("Uploaded Crash logs.txt");


    public static CompletableFuture<String> uploadToCrashy(String text) throws IOException {
        try {
            var prefix = http + "://" + crashyDomain;

            var promise = java11PostAsync(prefix + "/uploadCrash", gzip(text)).thenApplyAsync(response -> {
                int statusCode = response.statusCode();
                String responseBody = response.body();
                return switch (statusCode) {
                    case HttpURLConnection.HTTP_OK -> {
                        UploadCrashSuccess responseObject = new Gson().fromJson(responseBody, UploadCrashSuccess.class);
                        try {
                            rememberCrashCode(responseObject.crashId, responseObject.deletionKey);
                        } catch (IOException e) {
                            NotEnoughCrashes.getLogger().error("Could not remember crash code when uploading crash " + responseObject.crashId, e);
                        }
                        yield responseObject.crashyUrl;
                    }
                    case HttpURLConnection.HTTP_BAD_REQUEST -> throw new UploadToCrashyError.InvalidCrash();
                    case HttpURLConnection.HTTP_ENTITY_TOO_LARGE -> throw new UploadToCrashyError.TooLarge();
                    default -> throw new IllegalStateException("Unexpected status code when uploading to crashy: " + statusCode + " message: " + responseBody);
                };
            });

//            promise.get();
            return promise;

        } catch (InterruptedException/* | ExecutionException*/ e) {
            throw new RuntimeException(e);
        }
    }
    public static String uploadToCrashySync(String text) throws IOException, ExecutionException, InterruptedException {
        return uploadToCrashy(text).get();
    }

    private static CompletableFuture<HttpResponse<String>> java11PostAsync(String url, byte[] body) throws InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(url))
                .setHeader("content-type", "text/plain")
                .setHeader("content-encoding", "gzip")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private static byte[] gzip(String string) throws IOException {
        try (var baos = new ByteArrayOutputStream()) {
            try (var gzos = new GZIPOutputStream(baos)) {
                gzos.write(string.getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        }
    }

    private static void rememberCrashCode(String id, String code) throws IOException {
        NotEnoughCrashes.ensureDirectoryExists();
        final String oldCodes;
        if (Files.exists(CRASH_CODES_PATH)) {
            oldCodes = Files.readString(CRASH_CODES_PATH);
        } else {
            oldCodes = "";
        }
        Files.writeString(CRASH_CODES_PATH, oldCodes + id + ": " + code + "\n");
    }

    static class UploadCrashSuccess {
        String crashId;
        String deletionKey;
        String crashyUrl;
    }


}
