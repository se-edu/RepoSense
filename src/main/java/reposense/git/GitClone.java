package reposense.git;

import static reposense.system.CommandRunner.runCommand;
import static reposense.util.StringsUtil.addQuote;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import reposense.git.exception.GitBranchException;
import reposense.git.exception.GitCloneException;
import reposense.model.RepoConfiguration;
import reposense.system.CommandRunner;
import reposense.system.CommandRunnerProcess;
import reposense.system.LogsManager;
import reposense.util.FileUtil;
import reposense.util.SystemUtil;

/**
 * Contains git clone related functionalities.
 * Git clone is responsible for cloning a local/remote repository into a new directory.
 */
public class GitClone {
    private static final Logger logger = LogsManager.getLogger(GitClone.class);

    /**
     * Runs "git clone --bare" command asynchronously to clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     *
     * @return an instance of {@code CommandRunnerProcess} to allow tracking the status of the cloning process.
     * @throws GitCloneException when an error occurs during command execution.
     */
    public static CommandRunnerProcess cloneBareAsync(RepoConfiguration config, Path rootPath,
            String outputFolderName) throws GitCloneException {
        try {
            return CommandRunner.runCommandAsync(rootPath, getCloneBareCommand(config, outputFolderName));
        } catch (RuntimeException rte) {
            throw new GitCloneException(rte);
        }
    }

    /**
     * Runs "git clone --bare" command asynchronously to clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     *
     * @return an instance of {@code CommandRunnerProcess} to allow tracking the status of the cloning process.
     * @throws GitCloneException when an error occurs during command execution.
     */
    public static CommandRunnerProcess cloneShallowBareAsync(RepoConfiguration config, Path rootPath,
                                                  String outputFolderName, Date sinceDate) throws GitCloneException {
        try {
            return CommandRunner.runCommandAsync(rootPath,
                                                    getCloneShallowBareCommand(config, outputFolderName, sinceDate));
        } catch (RuntimeException rte) {
            throw new GitCloneException(rte);
        }
    }

    /**
     * Runs "git clone --bare" command asynchronously to clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     *
     * @throws GitCloneException when an error occurs during command execution.
     */
    public static void clonePartialBare(RepoConfiguration config, Path rootPath,
                                                      String outputFolderName) throws GitCloneException {
        try {
            CommandRunner.runCommand(rootPath, getClonePartialBareCommand(config, outputFolderName));
            return;
        } catch (RuntimeException rte) {
            throw new GitCloneException(rte);
        }
    }

    /**
     * Runs "git clone --bare" command asynchronously to clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     *
     * @throws GitCloneException when an error occurs during command execution.
     */
    public static void cloneShallowPartialBare(RepoConfiguration config, Path rootPath,
                                                  String outputFolderName, Date sinceDate) throws GitCloneException {
        try {
            CommandRunner.runCommand(rootPath, getCloneShallowPartialBareCommand(config, outputFolderName, sinceDate));
            return;
        } catch (RuntimeException rte) {
            throw new GitCloneException(rte);
        }
    }

    /**
     * Clones repo specified in the {@code config} and updates it with the branch info.
     */
    public static void clone(RepoConfiguration config) throws GitCloneException {
        try {
            Path rootPath = FileUtil.getRepoParentFolder(config);
            Path repoPath = Paths.get(rootPath.toString(), config.getRepoName());

            if (!SystemUtil.isTestEnvironment()) {
                FileUtil.deleteDirectory(config.getRepoRoot());
            } else if (SystemUtil.isTestEnvironment() && Files.exists(repoPath)) {
                logger.info("Skipped cloning from " + config.getLocation() + " as it was cloned before.");
            } else {
                logger.info("Cloning from " + config.getLocation() + "...");
                Files.createDirectories(rootPath);
                String command = getCloneCommand(config, repoPath.toString());
                runCommand(Paths.get("."), command);

                logger.info("Cloning completed!");
            }
        } catch (RuntimeException rte) {
            logger.log(Level.SEVERE, "Error encountered in Git Cloning, will attempt to continue analyzing", rte);
            throw new GitCloneException(rte);
            //Due to an unsolved bug on Windows Git, for some repository, Git Clone will return an error even
            // though the repo is cloned properly.
        } catch (IOException ioe) {
            throw new GitCloneException(ioe);
        }

        try {
            config.updateBranch();
            GitCheckout.checkout(config.getRepoRoot(), config.getBranch());
        } catch (GitBranchException gbe) {
            logger.log(Level.SEVERE,
                    "Exception met while trying to get current branch of repo. Analysis terminated.", gbe);
            throw new GitCloneException(gbe);
        } catch (RuntimeException rte) {
            logger.log(Level.SEVERE, "Branch does not exist! Analysis terminated.", rte);
            throw new GitCloneException(rte);
        }
    }

    /**
     * Clones a bare repo specified in {@code config} into the folder {@code outputFolderName}.
     * @throws IOException if it fails to delete a directory.
     */
    public static void cloneBare(RepoConfiguration config, Path rootPath,
            String outputFolderName) throws IOException {
        Path outputFolderPath = Paths.get(outputFolderName);
        if (!SystemUtil.isTestEnvironment()) {
            FileUtil.deleteDirectory(outputFolderName);
            FileUtil.createDirectory(outputFolderPath);
        } else if (SystemUtil.isTestEnvironment() && Files.exists(outputFolderPath)) {
            return;
        }
        String command = getCloneBareCommand(config, addQuote(outputFolderName));
        runCommand(rootPath, command);
    }

    /**
     * Performs a full clone from {@code clonedBareRepoLocation} into the folder {@code outputFolderName} and
     * directly branches out to {@code targetBranch}.
     * @throws IOException if it fails to delete a directory.
     * @throws GitCloneException when an error occurs during command execution.
     */
    public static void cloneFromBareAndUpdateBranch(Path rootPath, RepoConfiguration config)
            throws GitCloneException, IOException {
        Path relativePath = rootPath.relativize(FileUtil.getBareRepoPath(config));
        String outputFolderName = Paths.get(config.getRepoFolderName(), config.getRepoName()).toString();
        Path outputFolderPath = Paths.get(FileUtil.REPOS_ADDRESS, outputFolderName);

        if (!SystemUtil.isTestEnvironment()) {
            FileUtil.deleteDirectory(outputFolderPath.toString());
        } else if (SystemUtil.isTestEnvironment() && Files.exists(outputFolderPath)) {
            GitCheckout.checkoutBranch(outputFolderPath.toString(), config.getBranch());
            return;
        }

        String command = String.format(
                "git clone %s --branch %s %s", relativePath, config.getBranch(), outputFolderName);

        try {
            runCommand(rootPath, command);
        } catch (RuntimeException rte) {
            logger.severe("Exception met while cloning or checking out " + config.getDisplayName() + "."
                    + "Analysis terminated.");
            throw new GitCloneException(rte);
        }
    }

    /**
     * Constructs the command to clone a repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     */
    private static String getCloneCommand(RepoConfiguration config, String outputFolderName) {
        return "git clone " + addQuote(config.getLocation().toString()) + " "
                + outputFolderName;
    }

    /**
     * Constructs the command to clone a repo specified in the {@code config}
     * into the folder {@code outputFolderName} and subsequently branches to {@code branch}.
     */
    private static String getCloneAndBranchCommand(RepoConfiguration config, String branch, String outputFolderName) {
        return "git clone " + addQuote(config.getLocation().toString()) + " "
                + "--branch " + branch + " "
                + outputFolderName;
    }

    /**
     * Constructs the command to clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     */
    private static String getCloneBareCommand(RepoConfiguration config, String outputFolderName) {
        String output = "git clone --bare "
                + addQuote(config.getLocation().toString()) + " "
                + outputFolderName;
        return output;
    }

    /**
     * Constructs the command to shallow clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     */
    private static String getCloneShallowBareCommand(RepoConfiguration config,
                                                    String outputFolderName, Date shallowSinceDate) {
        return "git clone --bare --shallow-since="
                + addQuote(shallowSinceDate.toString()) + " "
                + addQuote(config.getLocation().toString()) + " "
                + outputFolderName;
    }

    /**
     * Constructs the command to partial clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     */
    private static String getClonePartialBareCommand(RepoConfiguration config, String outputFolderName) {
        return "git clone --bare --filter=blob:none "
                + addQuote(config.getLocation().toString()) + " "
                + outputFolderName;
    }

    /**
     * Constructs the command to shallow partial clone a bare repo specified in the {@code config}
     * into the folder {@code outputFolderName}.
     */
    private static String getCloneShallowPartialBareCommand(RepoConfiguration config,
                                                            String outputFolderName, Date shallowSinceDate) {
        return "git clone --bare --filter=blob:none --shallow-since="
                + addQuote(shallowSinceDate.toString()) + " "
                + addQuote(config.getLocation().toString()) + " "
                + outputFolderName;
    }
}
