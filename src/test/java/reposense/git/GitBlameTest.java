package reposense.git;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import reposense.template.GitTestTemplate;

public class GitBlameTest extends GitTestTemplate {

    @Test
    public void blameRaw_validFile_success() {
        String content = GitBlame.blame(config.getRepoRoot(), "blameTest.java");
        Assert.assertFalse(content.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void blameRaw_nonExistentFile_throwsRunTimeException() {
        GitBlame.blame(config.getRepoRoot(), "nonExistentFile");
    }

    @Test
    public void blameWithPreviousAuthors_validFile_success() {
        createTestIgnoreRevsFile(new ArrayList<>());
        String content = GitBlame.blame(config.getRepoRoot(), "blameTest.java");
        Assert.assertFalse(content.isEmpty());
        removeTestIgnoreRevsFile();
    }
}
