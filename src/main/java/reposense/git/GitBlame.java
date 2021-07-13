package reposense.git;

import static reposense.system.CommandRunner.runCommand;
import static reposense.util.StringsUtil.addQuote;

import java.nio.file.Path;
import java.nio.file.Paths;

import reposense.util.StringsUtil;

/**
 * Contains git blame related functionalities.
 * Git blame is responsible for showing which revision and author last modified each line of a file.
 */
public class GitBlame {
    public static final String IGNORE_COMMIT_LIST_FILE_NAME = ".git-blame-ignore-revs";

    private static final String COMMIT_HASH_REGEX = "(^[0-9a-f]{40} .*)";
    private static final String AUTHOR_NAME_REGEX = "(^author .*)";
    private static final String AUTHOR_EMAIL_REGEX = "(^author-mail .*)";
    private static final String AUTHOR_TIME_REGEX = "(^author-time [0-9]+)";
    private static final String AUTHOR_TIMEZONE_REGEX = "(^author-tz .*)";
    private static final String COMBINATION_REGEX = COMMIT_HASH_REGEX + "|" + AUTHOR_NAME_REGEX + "|"
            + AUTHOR_EMAIL_REGEX + "|" + AUTHOR_TIME_REGEX + "|" + AUTHOR_TIMEZONE_REGEX;

    /**
     * Returns the raw git blame result for the {@code fileDirectory}, performed at the {@code root} directory.
     */
    public static String blame(String root, String fileDirectory) {
        Path rootPath = Paths.get(root);

        String blameCommand = "git blame -w --line-porcelain";
        blameCommand += " " + addQuote(fileDirectory);

        return StringsUtil.filterText(runCommand(rootPath, blameCommand), COMBINATION_REGEX);
    }

    /**
     * Returns the raw git blame result with finding previous authors enabled for the {@code fileDirectory},
     * performed at the {@code root} directory.
     */
    public static String blameWithPreviousAuthors(String root, String fileDirectory) {
        Path rootPath = Paths.get(root);

        String blameCommandWithFindingPreviousAuthors = "git blame -w --line-porcelain --ignore-revs-file";
        blameCommandWithFindingPreviousAuthors += " " + addQuote(IGNORE_COMMIT_LIST_FILE_NAME);
        blameCommandWithFindingPreviousAuthors += " " + addQuote(fileDirectory);

        return StringsUtil.filterText(runCommand(rootPath, blameCommandWithFindingPreviousAuthors), COMBINATION_REGEX);
    }
}
