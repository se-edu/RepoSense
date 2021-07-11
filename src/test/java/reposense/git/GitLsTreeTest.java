package reposense.git;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import reposense.git.exception.InvalidFilePathException;
import reposense.model.RepoConfiguration;
import reposense.template.GitTestTemplate;
import reposense.util.FileUtil;
import reposense.util.SystemUtil;

public class GitLsTreeTest extends GitTestTemplate {
    private static Method isValidWindowsFilenameMethod;

    @BeforeClass
    public static void beforeClass() throws Exception {
        GitTestTemplate.beforeClass();
        isValidWindowsFilenameMethod = GitLsTree.class.getDeclaredMethod("isValidWindowsFilename", String.class);
        isValidWindowsFilenameMethod.setAccessible(true);
    }

    @After
    public void after() {
       // Overrides checkout master behaviour in GitTestTemplate as it throws error when run on a bare clone.
    }

    @Test
    public void isValidWindowsFilename_validFilenames_success() throws Exception {
        // Runs test only on Windows operating systems
        Assume.assumeTrue(SystemUtil.isWindows());

        List<String> validDirectoryNames = Arrays.asList("com10.txt", "folder name/file name.txt", ".txt");
        for (String fileName : validDirectoryNames) {
            Assert.assertTrue((boolean) isValidWindowsFilenameMethod.invoke(null, fileName));
        }
    }

    @Test
    public void isValidWindowsFilename_illegalFileNames_fail() throws Exception {
        // Runs test only on Windows operating systems
        Assume.assumeTrue(SystemUtil.isWindows());

        List<String> windowsReservedFilenames = Arrays.asList("com1.txt", "folder/com1.txt", "folder/com1/file.txt",
                "nul", "fi\\le.txt", "\"file\".txt", "file.txt.", "folder./file.txt", "file.txt ", "folder /file.txt");
        for (String fileName : windowsReservedFilenames) {
            Assert.assertFalse((boolean) isValidWindowsFilenameMethod.invoke(null, fileName));
        }
    }

    @Test
    public void repo_validFilePaths_success() throws Exception {
        validateFilePaths(config);
    }

    @Test(expected = InvalidFilePathException.class)
    public void windows_cloneInvalidWindowsFilePaths_throwsInvalidFilePathException() throws Exception {
        // Runs test only on Windows operating systems
        Assume.assumeTrue(SystemUtil.isWindows());

        config.setBranch("391-invalid-filepaths");
        validateFilePaths(config);
    }

    @Test
    public void unix_cloneInvalidWindowsFilePaths_success() throws Exception {
        // Runs test only on non Windows (Unix) operating systems
        Assume.assumeTrue(!SystemUtil.isWindows());

        config.setBranch("391-invalid-filepaths");
        validateFilePaths(config);
    }

    /**
     * Clones a bare repo in {@code config} and verifies that the repo contains only file paths that are
     * compatible in Windows.
     * @throws IOException if it fails to create/delete the folders.
     * @throws InvalidFilePathException if the repository contains invalid file paths that are not compatible with
     * Windows.
     */
    private void validateFilePaths(RepoConfiguration config) throws IOException, InvalidFilePathException {
        GitClone.cloneBare(config, Paths.get("."), FileUtil.getBareRepoPath(config).toString());
        GitLsTree.validateFilePaths(config, FileUtil.getBareRepoPath(config));
    }
}
