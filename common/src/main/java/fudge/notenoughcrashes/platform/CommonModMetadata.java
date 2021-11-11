package fudge.notenoughcrashes.platform;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommonModMetadata {
    private String id;
    private String name;
    private @Nullable String issuesPage;
    private @Nullable List<String> authors;
    private Path rootPath;

    public CommonModMetadata(String id, String name, @Nullable String issuesPage, @Nullable List<String> authors, Path rootPath) {
        this.id = id;
        this.name = name;
        this.issuesPage = issuesPage;
        this.authors = authors;
        this.rootPath = rootPath;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public @Nullable String issuesPage() {
        return issuesPage;
    }

    public @Nullable List<String> authors() {
        return authors;
    }

    public Path rootPath() {
        return rootPath;
    }

    public static final CommonModMetadata STUB = new CommonModMetadata(
            "", "UNKNOWN", null, null, Paths.get("")
    );
}
