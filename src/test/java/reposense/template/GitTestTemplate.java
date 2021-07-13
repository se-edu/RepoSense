package reposense.template;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import reposense.authorship.FileInfoAnalyzer;
import reposense.authorship.FileInfoExtractor;
import reposense.authorship.model.FileInfo;
import reposense.authorship.model.FileResult;
import reposense.authorship.model.LineInfo;
import reposense.git.GitCheckout;
import reposense.git.GitClone;
import reposense.model.Author;
import reposense.model.CommitHash;
import reposense.model.FileTypeTest;
import reposense.model.RepoConfiguration;
import reposense.model.RepoLocation;
import reposense.util.FileUtil;

/**
 * Contains templates for git testing.
 */
public class GitTestTemplate {
    protected static final String TEST_REPO_GIT_LOCATION = "https://github.com/reposense/testrepo-Alpha.git";
    protected static final String DISK_REPO_DISPLAY_NAME = "testrepo-Alpha_master";
    protected static final String TEST_IGNORE_REVS_FILE_LOCATION =
            "repos/reposense_testrepo-Alpha/testrepo-Alpha/.git-blame-ignore-revs";
    protected static final String FIRST_COMMIT_HASH = "7d7584f";
    protected static final String ROOT_COMMIT_HASH = "fd425072e12004b71d733a58d819d845509f8db3";
    protected static final String TEST_COMMIT_HASH = "2fb6b9b";
    protected static final String TEST_COMMIT_HASH_PARENT = "c5a6dc774e22099cd9ddeb0faff1e75f9cf4f151";
    protected static final String MAIN_AUTHOR_NAME = "harryggg";
    protected static final String FAKE_AUTHOR_NAME = "fakeAuthor";
    protected static final String EUGENE_AUTHOR_NAME = "eugenepeh";
    protected static final String YONG_AUTHOR_NAME = "Yong Hao TENG";
    protected static final String MINGYI_AUTHOR_NAME = "myteo";
    protected static final String JAMES_AUTHOR_NAME = "jamessspanggg";
    protected static final String JAMES_ALTERNATIVE_AUTHOR_NAME = "James Pang";
    protected static final String JINYAO_AUTHOR_NAME = "jylee-git";
    protected static final String LATEST_COMMIT_HASH = "c08107145269d5d5bb42ad78833774b7e5532977";
    protected static final String LATEST_COMMIT_HASH_PARENT = "136c6713fc00cfe79a1598e8ce83c6ef3b878660";
    protected static final String EMPTY_TREE_HASH = "4b825dc642cb6eb9a060e54bf8d69288fbee4904";
    protected static final String EUGENE_AUTHOR_README_FILE_COMMIT_07052018_STRING =
            "2d87a431fcbb8f73a731b6df0fcbee962c85c250";
    protected static final CommitHash EUGENE_AUTHOR_README_FILE_COMMIT_07052018 =
            new CommitHash(EUGENE_AUTHOR_README_FILE_COMMIT_07052018_STRING);
    protected static final String FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018_STRING =
            "768015345e70f06add2a8b7d1f901dc07bf70582";
    protected static final CommitHash FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018 =
            new CommitHash(FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018_STRING);
    protected static final String MAIN_AUTHOR_BLAME_TEST_FILE_COMMIT_06022018_STRING =
            "8d0ac2ee20f04dce8df0591caed460bffacb65a4";
    protected static final CommitHash MAIN_AUTHOR_BLAME_TEST_FILE_COMMIT_06022018 =
            new CommitHash(MAIN_AUTHOR_BLAME_TEST_FILE_COMMIT_06022018_STRING);
    protected static final String FAKE_AUTHOR_BLAME_RANGED_COMMIT_ONE_06022018_STRING =
            "7d7584fc204922cc5ff3bd5ca073cad6bed2c46a";
    protected static final String FAKE_AUTHOR_BLAME_RANGED_COMMIT_TWO_06022018_STRING =
            "8d0ac2ee20f04dce8df0591caed460bffacb65a4";
    protected static final String FAKE_AUTHOR_BLAME_RANGED_COMMIT_THREE_07022018_STRING =
            "8e4ca1da5d413e9ab84a1e8d1474918afa97f7a1";
    protected static final String FAKE_AUTHOR_BLAME_RANGED_COMMIT_FOUR_08022018_STRING =
            "768015345e70f06add2a8b7d1f901dc07bf70582";
    protected static final List<CommitHash> FAKE_AUTHOR_BLAME_RANGED_COMMIT_LIST_09022018 = Arrays.asList(
            new CommitHash(FAKE_AUTHOR_BLAME_RANGED_COMMIT_ONE_06022018_STRING),
            new CommitHash(FAKE_AUTHOR_BLAME_RANGED_COMMIT_TWO_06022018_STRING),
            new CommitHash(FAKE_AUTHOR_BLAME_RANGED_COMMIT_THREE_07022018_STRING),
            new CommitHash(FAKE_AUTHOR_BLAME_RANGED_COMMIT_FOUR_08022018_STRING));
    protected static final String NONEXISTENT_COMMIT_HASH = "nonExistentCommitHash";
    protected static final String TIME_ZONE_ID_STRING = "Asia/Singapore";

    protected static final Author MAIN_AUTHOR = new Author(MAIN_AUTHOR_NAME);
    protected static final Author FAKE_AUTHOR = new Author(FAKE_AUTHOR_NAME);


    protected static RepoConfiguration config;

    @Before
    public void before() throws Exception {
        config = new RepoConfiguration(new RepoLocation(TEST_REPO_GIT_LOCATION), "master");
        config.setAuthorList(Collections.singletonList(getAlphaAllAliasAuthor()));
        config.setFormats(FileTypeTest.DEFAULT_TEST_FORMATS);
        config.setIsLastModifiedDateIncluded(false);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        config = new RepoConfiguration(new RepoLocation(TEST_REPO_GIT_LOCATION), "master");
        config.setZoneId(TIME_ZONE_ID_STRING);
        GitClone.clone(config);
    }

    @After
    public void after() {
        GitCheckout.checkout(config.getRepoRoot(), "master");
    }

    /**
     * Generates the information for test file.
     */
    public FileInfo generateTestFileInfo(String relativePath) {
        FileInfo fileInfo = FileInfoExtractor.generateFileInfo(config.getRepoRoot(), relativePath);

        config.getAuthorDetailsToAuthorMap().put(MAIN_AUTHOR_NAME, new Author(MAIN_AUTHOR_NAME));
        config.getAuthorDetailsToAuthorMap().put(FAKE_AUTHOR_NAME, new Author(FAKE_AUTHOR_NAME));

        return fileInfo;
    }

    public void createTestIgnoreRevsFile(List<CommitHash> toIgnore) {
        FileUtil.writeIgnoreRevsFile(TEST_IGNORE_REVS_FILE_LOCATION, toIgnore);
    }

    public void removeTestIgnoreRevsFile() {
        new File(TEST_IGNORE_REVS_FILE_LOCATION).delete();
    }

    public FileResult getFileResult(String relativePath) {
        FileInfo fileinfo = generateTestFileInfo(relativePath);
        return FileInfoAnalyzer.analyzeTextFile(config, fileinfo);
    }

    /**
     * For each line in {@code FileResult}, assert that it is attributed to the expected author provided by
     * {@code expectedLineAuthors}.
     */
    public void assertFileAnalysisCorrectness(FileResult fileResult, List<Author> expectedLineAuthors) {
        List<LineInfo> lines = fileResult.getLines();
        assertEquals(expectedLineAuthors.size(), lines.size());

        Iterator<Author> lineAuthorsItr = expectedLineAuthors.iterator();
        Iterator<LineInfo> linesItr = lines.iterator();

        while (linesItr.hasNext() && lineAuthorsItr.hasNext()) {
            assertEquals(lineAuthorsItr.next(), linesItr.next().getAuthor());
        }
    }

    /**
     * Returns a {@code Author} that has git id and aliases of all authors in testrepo-Alpha, so that no commits
     * will be filtered out in the `git log` command.
     */
    protected Author getAlphaAllAliasAuthor() {
        Author author = new Author(MAIN_AUTHOR_NAME);
        author.setAuthorAliases(Arrays.asList(FAKE_AUTHOR_NAME, EUGENE_AUTHOR_NAME, YONG_AUTHOR_NAME));
        return author;
    }
}
