package reposense.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import reposense.git.CommitNotFoundException;
import reposense.model.Author;
import reposense.model.RepoConfiguration;
import reposense.util.FileUtil;

public class CommandRunner {
    private static final DateFormat GIT_LOG_SINCE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00+08:00");
    private static final DateFormat GIT_LOG_UNTIL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59+08:00");

    // ignore check against email
    private static final String AUTHOR_NAME_PATTERN = "^%s <.*>$";
    private static final String OR_OPERATOR_PATTERN = "\\|";
    private static final String REBASE_ONTO_BRANCH_FROM_TEMP_BRANCH_COMMAND_FORMAT =
            "git rebase --onto %s HEAD %s -Xours";
    private static final String REMOVE_COMMIT_AUTHOR_COMMAND =
            "git commit --amend --author=\"- <->\" --no-edit";
    private static final String DELETE_BRANCH_COMMAND_FORMAT = "git branch -D %s";
    private static final String CHECKOUT_COMMIT_IN_NEW_BRANCH_COMMAND_FORMAT = "git checkout -b %s %s";

    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$|]");

    private static boolean isWindows = isWindows();

    public static String gitLog(RepoConfiguration config, Author author) {
        Path rootPath = Paths.get(config.getRepoRoot());

        String command = "git log --no-merges ";
        command += convertToGitDateRangeArgs(config.getSinceDate(), config.getUntilDate());
        command += " --pretty=format:\"%H|%aN|%ad|%s\" --date=iso --shortstat";
        command += convertToFilterAuthorArgs(author);
        command += convertToGitFormatsArgs(config.getFormats());
        command += convertToGitExcludeGlobArgs(author.getIgnoreGlobList());

        return runCommand(rootPath, command);
    }

    public static void checkout(String root, String hash) {
        Path rootPath = Paths.get(root);
        runCommand(rootPath, "git checkout " + hash);
    }

    /**
     * Creates a new branch with {@code branchName} from the current branch in the repository
     * at {@code root} and checks out to it.
     */
    public static void checkoutNewBranch(String root, String branchName) {
        checkoutNewBranch(root, branchName, "");
    }

    /**
     * Creates a new branch with {@code branchName} with {@code hash} as the HEAD commit in the repository
     * at {@code root} and checks out to it.
     */
    public static void checkoutNewBranch(String root, String branchName, String hash) {
        Path rootPath = Paths.get(root);
        runCommand(rootPath, String.format(CHECKOUT_COMMIT_IN_NEW_BRANCH_COMMAND_FORMAT, branchName, hash));
    }

    /**
     * Checks out to the latest commit before {@code untilDate} in {@code branchName} branch
     * if {@code untilDate} is not null.
     * @throws CommitNotFoundException if commits before {@code untilDate} cannot be found.
     */
    public static void checkoutToDate(String root, String branchName, Date untilDate) throws CommitNotFoundException {
        if (untilDate == null) {
            return;
        }

        Path rootPath = Paths.get(root);

        String substituteCommand = "git rev-list -1 --before="
                + GIT_LOG_UNTIL_DATE_FORMAT.format(untilDate) + " " + branchName;
        String hash = runCommand(rootPath, substituteCommand);
        if (hash.isEmpty()) {
            throw new CommitNotFoundException("Commit before until date is not found.");
        }
        String checkoutCommand = "git checkout " + hash;
        runCommand(rootPath, checkoutCommand);
    }

    public static String blameRaw(String root, String fileDirectory) {
        Path rootPath = Paths.get(root);

        String blameCommand = "git blame -w --line-porcelain";
        blameCommand += " " + addQuote(fileDirectory);
        blameCommand += getAuthorFilterCommand();

        return runCommand(rootPath, blameCommand);
    }

    public static String checkStyleRaw(String absoluteDirectory) {
        Path rootPath = Paths.get(absoluteDirectory);
        return runCommand(
                rootPath,
                "java -jar checkstyle-7.7-all.jar -c /google_checks.xml -f xml " + absoluteDirectory
        );
    }

    /**
     * Deletes the branch with {@code branchName} in the repository at {@code root}.
     */
    public static void deleteBranch(String root, String branchName) {
        Path rootPath = Paths.get(root);
        runCommand(rootPath, String.format(DELETE_BRANCH_COMMAND_FORMAT, branchName));
    }

    /**
     * Returns the git diff result of the current commit compared to {@code lastCommitHash}, without any context.
     */
    public static String diffCommit(String root, String lastCommitHash) {
        Path rootPath = Paths.get(root);
        return runCommand(rootPath, "git diff -U0 " + lastCommitHash);
    }

    /**
     * Returns the latest commit hash before {@code date}.
     * Returns an empty {@code String} if {@code date} is null, or there is no such commit.
     */
    public static String getCommitHashBeforeDate(String root, String branchName, Date date) {
        if (date == null) {
            return "";
        }

        Path rootPath = Paths.get(root);
        String revListCommand = "git rev-list -1 --before="
                + GIT_LOG_SINCE_DATE_FORMAT.format(date) + " " + branchName;
        return runCommand(rootPath, revListCommand);
    }

    public static String getShortlogSummary(RepoConfiguration config) {
        Path rootPath = Paths.get(config.getRepoRoot());
        String command = "git log --pretty=short | git shortlog --summary";

        return runCommand(rootPath, command);
    }

    /**
     * Removes the author for the {@code commitHash} in the {@code branch} of the repo.
     */
    public static void removeCommitAuthor(String root, String branch, String commitHash) {
        Path rootPath = Paths.get(root);
        String tempBranch = commitHash + "temp";

        checkoutNewBranch(root, tempBranch, commitHash);

        // remove the author for the given commit
        runCommand(rootPath, REMOVE_COMMIT_AUTHOR_COMMAND);

        // rebase temp branch onto the given branch, replacing the given commit's author on that branch
        runCommand(rootPath, String.format(REBASE_ONTO_BRANCH_FROM_TEMP_BRANCH_COMMAND_FORMAT, tempBranch, branch));

        // checkout back to original branch being analyzed
        checkout(root, branch);

        // clean up temp branch
        deleteBranch(root, tempBranch);
    }

    public static String cloneRepo(String location, String displayName) throws IOException {
        Path rootPath = Paths.get(FileUtil.REPOS_ADDRESS, displayName);
        Files.createDirectories(rootPath);
        return runCommand(rootPath, "git clone " + addQuote(location));
    }

    private static String runCommand(Path path, String command) {
        ProcessBuilder pb = null;
        if (isWindows) {
            pb = new ProcessBuilder()
                    .command(new String[]{"CMD", "/c", command})
                    .directory(path.toFile());
        } else {
            pb = new ProcessBuilder()
                    .command(new String[]{"bash", "-c", command})
                    .directory(path.toFile());
        }
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new RuntimeException("Error Creating Thread:" + e.getMessage());
        }
        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        int exit = 0;
        try {
            exit = p.waitFor();
            outputGobbler.join();
            errorGobbler.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error Handling Thread.");
        }

        if (exit == 0) {
            return outputGobbler.getValue();
        } else {
            String errorMessage = "Error returned from command ";
            errorMessage += command + "on path ";
            errorMessage += path.toString() + " :\n" + errorGobbler.getValue();
            throw new RuntimeException(errorMessage);
        }
    }

    private static String addQuote(String original) {
        return "\"" + original + "\"";
    }

    private static boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }

    /**
     * Returns the {@code String} command which filters the git blame output to produce only the necessary author
     * name for each line.
     */
    private static String getAuthorFilterCommand() {
        return isWindows
                ? "| findstr /B /C:" + addQuote("author ")
                : "| grep " + addQuote("^\\(author\\|[0-9a-f]\\{40\\}\\) .*");
    }

    /**
     * Returns the {@code String} command to specify the date range of commits to analyze for `git` commands.
     */
    private static String convertToGitDateRangeArgs(Date sinceDate, Date untilDate) {
        String gitDateRangeArgs = "";

        if (sinceDate != null) {
            gitDateRangeArgs += " --since=" + addQuote(GIT_LOG_SINCE_DATE_FORMAT.format(sinceDate));
        }
        if (untilDate != null) {
            gitDateRangeArgs += " --until=" + addQuote(GIT_LOG_UNTIL_DATE_FORMAT.format(untilDate));
        }

        return gitDateRangeArgs;
    }

    /**
     * Returns the {@code String} command to specify the authors to analyze for `git log` command.
     */
    private static String convertToFilterAuthorArgs(Author author) {
        StringBuilder filterAuthorArgsBuilder = new StringBuilder(" --author=\"");

        // git author names may contain regex meta-characters, so we need to escape those
        author.getAuthorAliases().stream()
                .map(authorAlias -> String.format(
                        AUTHOR_NAME_PATTERN, escapeSpecialRegexChars(authorAlias)) + OR_OPERATOR_PATTERN)
                .forEach(filterAuthorArgsBuilder::append);

        filterAuthorArgsBuilder.append(String.format(AUTHOR_NAME_PATTERN, author.getGitId())).append("\"");
        return filterAuthorArgsBuilder.toString();
    }

    /**
     * Returns the {@code String} command to specify the file formats to analyze for `git` commands.
     */
    private static String convertToGitFormatsArgs(List<String> formats) {
        StringBuilder gitFormatsArgsBuilder = new StringBuilder();
        final String cmdFormat = " -- " + addQuote("*.%s");
        formats.stream()
                .map(format -> String.format(cmdFormat, format))
                .forEach(gitFormatsArgsBuilder::append);

        return gitFormatsArgsBuilder.toString();
    }

    /**
     * Returns the {@code String} command to specify the globs to exclude for `git log` command.
     */
    private static String convertToGitExcludeGlobArgs(List<String> ignoreGlobList) {
        StringBuilder gitExcludeGlobArgsBuilder = new StringBuilder();
        final String cmdFormat = " " + addQuote(":(exclude)%s");
        ignoreGlobList.stream()
                .filter(item -> !item.isEmpty())
                .map(ignoreGlob -> String.format(cmdFormat, ignoreGlob))
                .forEach(gitExcludeGlobArgsBuilder::append);

        return gitExcludeGlobArgsBuilder.toString();
    }

    /**
     * Converts the {@code regexString} to a literal {@code String} where all regex meta-characters are escaped
     * and returns it.
     */
    private static String escapeSpecialRegexChars(String regexString) {
        return SPECIAL_REGEX_CHARS.matcher(regexString.replace("\\", "\\\\\\")).replaceAll("\\\\$0");
    }
}
