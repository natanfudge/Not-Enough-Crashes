package fudge.notenoughcrashes.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Java11 {
    // Reads file as string
    public static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }    // Reads file as string

    public static void writeString(Path path, String string) throws IOException {
        Files.write(path, string.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        return IOUtils.toByteArray(inputStream);
    }
}
