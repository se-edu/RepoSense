package reposense.git;

import static reposense.system.CommandRunner.runCommand;
import static reposense.system.CommandRunner.spawnCommandProcess;
import static reposense.system.CommandRunner.waitForCommandProcess;
import static reposense.util.StringsUtil.addQuote;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import reposense.model.RepoConfiguration;
import reposense.model.RepoLocation;
import reposense.system.CommandRunnerProcess;
import reposense.system.LogsManager;
import reposense.util.FileUtil;

/**
 * Contains git clone related functionalities.
 * Git clone is responsible for cloning a local/remote repository into a new directory.
 */
public class GitClone {

    private static final Logger logger = LogsManager.getLogger(GitClone.class);
    private static CommandRunnerProcess crp;
    private static RepoLocation lastClonedRepoLocation;

    /**
     * Clones repo specified in the {@code repoConfig} and updates it with the branch info.
     */
    public static void clone(RepoConfiguration repoConfig)
            throws GitCloneException {
        try {
            FileUtil.deleteDirectory(repoConfig.getRepoRoot());
            logger.info("Cloning from " + repoConfig.getLocation() + "...");
            clone(repoConfig.getLocation(), repoConfig.getRepoName());
            logger.info("Cloning completed!");
        } catch (RuntimeException rte) {
            logger.log(Level.SEVERE, "Error encountered in Git Cloning, will attempt to continue analyzing", rte);
            throw new GitCloneException(rte);
            //Due to an unsolved bug on Windows Git, for some repository, Git Clone will return an error even
            // though the repo is cloned properly.
        } catch (IOException ioe) {
            throw new GitCloneException(ioe);
        }
        setRepoConfigBranch(repoConfig);
        checkOutBranch(repoConfig);
    }

    private static void clone(RepoLocation location, String repoName) throws IOException {
        Path rootPath = Paths.get(FileUtil.REPOS_ADDRESS, repoName);
        Files.createDirectories(rootPath);
        runCommand(rootPath, "git clone " + addQuote(location.toString()));
    }

    public static void spawnCloneProcess(RepoConfiguration repoConfig) throws GitCloneException {
        if (isRepoPreviouslyCloned(repoConfig)) {
            return;
        }
        try {
            FileUtil.deleteDirectory(repoConfig.getRepoRoot());
            logger.info("Cloning in parallel from " + repoConfig.getLocation() + "...");
            Path rootPath = Paths.get(FileUtil.REPOS_ADDRESS, repoConfig.getRepoName());
            Files.createDirectories(rootPath);
            crp = spawnCommandProcess(rootPath, "git clone " + addQuote(repoConfig.getLocation().toString()));
        } catch (RuntimeException rte) {
            logger.log(Level.SEVERE, "Error encountered in Git Cloning, will attempt to continue analyzing", rte);
            throw new GitCloneException(rte);
        } catch (IOException ioe) {
            throw new GitCloneException(ioe);
        }
    }

    public static void waitForCloneProcess(RepoConfiguration repoConfig) throws GitCloneException {
        if (isRepoPreviouslyCloned(repoConfig)) {
            return;
        }
        try {
            Path rootPath = Paths.get(FileUtil.REPOS_ADDRESS, repoConfig.getRepoName());
            waitForCommandProcess(rootPath, "git clone " + addQuote(repoConfig.getLocation().toString()), crp);
            logger.info("Cloning of " + repoConfig.getLocation() + " completed!");
            setRepoConfigBranch(repoConfig);
            lastClonedRepoLocation = repoConfig.getLocation();
        } catch (RuntimeException rte) {
            logger.log(Level.SEVERE, "Error encountered in Git Cloning, will attempt to continue analyzing", rte);
            throw new GitCloneException(rte);
        }
    }

    private static void setRepoConfigBranch(RepoConfiguration repoConfig) {
        if (repoConfig.getBranch().equals(RepoConfiguration.DEFAULT_BRANCH)) {
            String currentBranch = GitBranch.getCurrentBranch(repoConfig.getRepoRoot());
            repoConfig.setBranch(currentBranch);
        }
    }

    public static void checkOutBranch(RepoConfiguration repoConfig) throws GitCloneException {
        try {
            GitCheckout.checkout(repoConfig.getRepoRoot(), repoConfig.getBranch());
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Branch does not exist! Analyze terminated.", e);
            throw new GitCloneException(e);
        }
    }

    private static boolean isRepoPreviouslyCloned(RepoConfiguration repoConfig) {
        return lastClonedRepoLocation != null && lastClonedRepoLocation.equals(repoConfig.getLocation());
    }
}
