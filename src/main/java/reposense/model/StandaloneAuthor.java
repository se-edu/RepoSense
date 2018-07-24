package reposense.model;

import java.util.Arrays;

public class StandaloneAuthor {
    private String githubId;
    private String displayName;
    private String[] authorNames;

    public StandaloneAuthor(String githubId, String displayName, String[] authorNames) {
        this.githubId = githubId;
        this.displayName = displayName;
        this.authorNames = authorNames;
    }

    public String getGithubId() {
        return githubId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getAuthorNames() {
        if (authorNames == null) {
            return new String[]{};
        }

        return authorNames;
    }
}
