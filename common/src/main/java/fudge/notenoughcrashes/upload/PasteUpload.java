package fudge.notenoughcrashes.upload;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Uploads only when the player explicitly chooses to share a report. */
public final class PasteUpload {
    private PasteUpload() {}

    public static String upload(String text) throws IOException {
        return upload(text, URI.create("https://api.pastes.dev/post"));
    }

    static String upload(String text, URI endpoint) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "NotEnoughCrashes (github.com/natanfudge/Not-Enough-Crashes)")
                .header("Content-Type", "text/plain; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(text, StandardCharsets.UTF_8))
                .build();
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("pastes.dev returned HTTP " + response.statusCode());
            }
            try {
                String key = JsonParser.parseString(response.body()).getAsJsonObject().get("key").getAsString();
                if (!key.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("Invalid paste key");
                return "https://pastes.dev/" + key;
            } catch (RuntimeException exception) {
                throw new IOException("Invalid response from pastes.dev", exception);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", exception);
        }
    }
}
