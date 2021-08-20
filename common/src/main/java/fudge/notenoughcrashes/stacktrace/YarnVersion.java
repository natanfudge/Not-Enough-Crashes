package fudge.notenoughcrashes.stacktrace;

import com.google.gson.Gson;
import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.MinecraftVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class YarnVersion {
    public String gameVersion;
    public String separator;
    public int build;
    public String maven;
    public String version;
    public boolean stable;

    private static final String YARN_API_ENTRYPOINT = "https://meta.fabricmc.net/v2/versions/yarn/" + MinecraftVersion.create().getName();
    private static final Path VERSION_FILE = NotEnoughCrashes.DIRECTORY.resolve("yarn-version.txt");
    private static String versionMemCache = null;


    public static String getLatestBuildForCurrentVersion() throws IOException {
        if (versionMemCache == null) {
            if (!Files.exists(VERSION_FILE)) {
                URL url = new URL(YARN_API_ENTRYPOINT);
                URLConnection request = url.openConnection();
                request.connect();

                InputStream response = (InputStream) request.getContent();
                YarnVersion[] versions = new Gson().fromJson(new InputStreamReader(response), YarnVersion[].class);
                if (versions.length == 0) {
                    throw new IllegalStateException("No yarn versions were received at the API endpoint. Received json: " + getString(response));
                }
                String version = Arrays.stream(versions).max(Comparator.comparingInt(v -> v.build)).get().version;
                NotEnoughCrashes.ensureDirectoryExists();
                Files.write(VERSION_FILE, version.getBytes());
                versionMemCache = version;
            } else {
                versionMemCache = new String(Files.readAllBytes(VERSION_FILE));
            }
        }

        return versionMemCache;
    }

    private static String getString(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
