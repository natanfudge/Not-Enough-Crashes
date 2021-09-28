package fudge.notenoughcrashes.platform;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public final class CommonModMetadata {

    public static final CommonModMetadata STUB = new CommonModMetadata(
            "", "UNKNOWN", null, null, Paths.get("")
    );
    private final String id;
    private final String name;
    private final @Nullable String issuesPage;
    private final @Nullable List<String> authors;
    private final Path rootPath;

    public CommonModMetadata(
            String id, String name, @Nullable String issuesPage, @Nullable List<String> authors, Path rootPath
    ) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CommonModMetadata) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.issuesPage, that.issuesPage) &&
                Objects.equals(this.authors, that.authors) &&
                Objects.equals(this.rootPath, that.rootPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, issuesPage, authors, rootPath);
    }

    @Override
    public String toString() {
        return "CommonModMetadata[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "issuesPage=" + issuesPage + ", " +
                "authors=" + authors + ", " +
                "rootPath=" + rootPath + ']';
    }

}
