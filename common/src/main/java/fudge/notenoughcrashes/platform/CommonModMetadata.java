package fudge.notenoughcrashes.platform;

import javax.annotation.Nullable;
import java.util.List;

public class CommonModMetadata {
    private final String id;
    private final String name;
    @Nullable
    private final String issuesPage;
    @Nullable
    private final List<String> authors;

    public CommonModMetadata(String id, String name, @Nullable String issuesPage,@Nullable List<String> authors) {
        this.id = id;
        this.name = name;
        this.issuesPage = issuesPage;
        this.authors = authors;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getIssuesPage() {
        return issuesPage;
    }

    @Nullable
    public List<String> getAuthors() {
        return authors;
    }
}
