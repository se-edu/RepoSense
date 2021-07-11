package reposense.report;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import reposense.git.GitClone;
import reposense.model.RepoConfiguration;
import reposense.model.RepoLocation;
import reposense.util.FileUtil;

public class RepoClonerTest {

    private static final String TEST_REPO_EMPTY_GIT_LOCATION = "https://github.com/reposense/testrepo-Empty.git";
    private static final String TEST_REPO_GIT_LOCATION = "https://github.com/reposense/testrepo-Alpha.git";
    private static final String OUTPUT_PATH = "repocloner_test";
    private static final Path REPOCLONE_LOCAL_TEST_PATH =
            Paths.get(FileUtil.REPOS_ADDRESS, "repoclone test/dummy-repo");

    @Test
    public void repoCloner_emptyRepo_failsGracefully() throws Exception {
        RepoConfiguration emptyRepositoryRepoConfig =
                new RepoConfiguration(new RepoLocation(TEST_REPO_EMPTY_GIT_LOCATION));

        RepoCloner repoCloner = new RepoCloner();
        repoCloner.cloneBare(emptyRepositoryRepoConfig);
        RepoLocation clonedRepoLocation = repoCloner.getClonedRepoLocation();

        Assert.assertNull(clonedRepoLocation);
    }

    @Test
    public void repoCloner_validRepoLocationWithRelativePathingAndSpaces_success() throws Exception {
        //Clones a test repository into the test directory for testing of relative pathing
        RepoConfiguration tempRemoteConfiguration = new RepoConfiguration(new RepoLocation(TEST_REPO_GIT_LOCATION));
        GitClone.cloneBare(tempRemoteConfiguration, Paths.get("."), REPOCLONE_LOCAL_TEST_PATH.toString());

        RepoConfiguration repoWithRelativePathingAndSpacesAndEndingBackslash =
                new RepoConfiguration(new RepoLocation(REPOCLONE_LOCAL_TEST_PATH.toString()));
        RepoCloner repoCloner = new RepoCloner();
        repoCloner.cloneBare(repoWithRelativePathingAndSpacesAndEndingBackslash);
        Assert.assertTrue(Files.exists(REPOCLONE_LOCAL_TEST_PATH));
    }
}
