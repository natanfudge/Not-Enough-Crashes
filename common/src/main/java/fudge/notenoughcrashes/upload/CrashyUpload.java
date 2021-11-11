//package fudge.notenoughcrashes.upload;
//
//import com.google.gson.Gson;
//import fudge.notenoughcrashes.NotEnoughCrashes;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.zip.GZIPOutputStream;
//
//TODO: restore after we have loader 0.12 dealt with
//public class CrashyUpload {
//    private static final Path CRASH_CODES_PATH = NotEnoughCrashes.DIRECTORY.resolve("Uploaded Crash logs.txt");
//
//    private static final boolean localTesting = false;
//
//    public static CompletableFuture<String> uploadToCrashy(String text) throws IOException {
//        try {
//            String prefix = localTesting ? "http://localhost:5001/crashy-9dd87/europe-west1" : "https://europe-west1-crashy-9dd87.cloudfunctions.net";
//            return java11PostAsync(prefix + "/uploadCrash", gzip(text)).thenApply(response -> {
//                int statusCode = response.statusCode();
//                String responseBody = response.body();
//                switch (statusCode) {
//                    case HttpURLConnection.HTTP_OK:
//                        UploadCrashSuccess responseObject = new Gson().fromJson(responseBody, UploadCrashSuccess.class);
//                        try {
//                            rememberCrashCode(responseObject.crashId, responseObject.key);
//                        } catch (IOException e) {
//                            NotEnoughCrashes.getLogger().error("Could not remember crash code when uploading crash " + responseObject.crashId, e);
//                        }
//                        return localTesting ?
//                                responseObject.crashUrl.replace("https://crashy.net", "http://localhost:3000") : responseObject.crashUrl;
//                    case HttpURLConnection.HTTP_BAD_REQUEST:
//                        throw new UploadToCrashyError.InvalidCrash();
//                    case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
//                        throw new UploadToCrashyError.TooLarge();
//                    default:
//                        throw new IllegalStateException("Unexpected status code when uploading to crashy: " + statusCode + " message: " + responseBody);
//                }
//            });
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static String uploadToCrashySync(String text) throws IOException, ExecutionException, InterruptedException {
//        return uploadToCrashy(text).get();
//    }
//
//    private static CompletableFuture<HttpResponse<String>> java11PostAsync(String url, byte[] body) throws InterruptedException {
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
//                .setHeader("content-type", "application/gzip")
//                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
//                .build();
//        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//    }
//
//    private static byte[] gzip(String string) throws IOException {
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
//                gzos.write(string.getBytes(StandardCharsets.UTF_8));
//            }
//            return baos.toByteArray();
//        }
//    }
//
//    private static void rememberCrashCode(String id, String code) throws IOException {
//        NotEnoughCrashes.ensureDirectoryExists();
//        final String oldCodes;
//        if (Files.exists(CRASH_CODES_PATH)) {
//            oldCodes = Files.readString(CRASH_CODES_PATH);
//        } else {
//            oldCodes = "";
//        }
//        Files.writeString(CRASH_CODES_PATH, oldCodes + id + ": " + code + "\n");
//    }
//
//    static class UploadCrashSuccess {
//        String crashId;
//        String key;
//        String crashUrl;
//    }
//
//}
