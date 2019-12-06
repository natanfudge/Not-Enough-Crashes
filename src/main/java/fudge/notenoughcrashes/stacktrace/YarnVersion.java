package fudge.notenoughcrashes.stacktrace;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;

import com.google.gson.Gson;

import net.minecraft.MinecraftVersion;

public class YarnVersion {
    public String gameVersion;
    public String separator;
    public int build;
    public String maven;
    public String version;
    public boolean stable;

    private static final String YARN_API_ENTRYPOINT = "https://meta.fabricmc.net/v2/versions/yarn/" + new MinecraftVersion().getName();

    public static String getLatestBuildForCurrentVersion() throws IOException {
        URL url = new URL(YARN_API_ENTRYPOINT);
        URLConnection request = url.openConnection();
        request.connect();

        YarnVersion[] versions = new Gson().fromJson(new InputStreamReader((InputStream)request.getContent()),YarnVersion[].class);
        return Arrays.stream(versions).max(Comparator.comparingInt(v -> v.build)).get().version;
    }
}
