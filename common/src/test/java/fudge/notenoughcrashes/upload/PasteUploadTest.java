package fudge.notenoughcrashes.upload;

import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class PasteUploadTest {
    @Test public void uploadsUtf8WithIdentificationAndReturnsViewerUrl() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> agent = new AtomicReference<>();
        AtomicReference<String> type = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/post", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            agent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
            type.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            byte[] response = "{\"key\":\"abc_123-xyz\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, response.length);
            try (var out = exchange.getResponseBody()) { out.write(response); }
        });
        server.start();
        try {
            assertEquals("https://pastes.dev/abc_123-xyz", PasteUpload.upload("Zażółć — 崩溃", endpoint(server)));
            assertEquals("Zażółć — 崩溃", body.get());
            assertTrue(agent.get().contains("github.com/natanfudge/Not-Enough-Crashes"));
            assertEquals("text/plain; charset=utf-8", type.get());
        } finally { server.stop(0); }
    }

    @Test public void rejectsHttpErrorsAndMalformedKeys() throws Exception {
        assertRejected(429, "{\"key\":\"do-not-open\"}");
        assertRejected(500, "upstream error");
        assertRejected(201, "not json");
        assertRejected(201, "{}");
        assertRejected(201, "{\"key\":\"../other\"}");
        assertRejected(201, "{\"key\":\"\"}");
    }

    private void assertRejected(int status, String text) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/post", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = text.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            try (var out = exchange.getResponseBody()) { out.write(response); }
        });
        server.start();
        try { assertThrows(java.io.IOException.class, () -> PasteUpload.upload("test report", endpoint(server))); }
        finally { server.stop(0); }
    }

    private URI endpoint(HttpServer server) {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/post");
    }
}
