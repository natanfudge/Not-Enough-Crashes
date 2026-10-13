package fudge.notenoughcrashes.platform;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public record CommonModMetadata(
        String id, String name, String version, @Nullable String issuesPage, @Nullable List<String> authors, Path rootPath
) {

    public static final CommonModMetadata STUB = new CommonModMetadata(
            "", "UNKNOWN", "unknown", null, null, Paths.get("")
    );
}
