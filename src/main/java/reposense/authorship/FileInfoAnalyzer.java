package reposense.authorship;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import reposense.authorship.analyzer.AnnotatorAnalyzer;
import reposense.authorship.model.FileInfo;
import reposense.authorship.model.FileResult;
import reposense.authorship.model.LineInfo;
import reposense.git.GitBlame;
import reposense.git.GitLog;
import reposense.model.Author;
import reposense.model.CommitHash;
import reposense.model.RepoConfiguration;
import reposense.system.LogsManager;
import reposense.util.FileUtil;
import reposense.util.TimeUtil;

/**
 * Analyzes the target and information given in the {@code FileInfo}.
 */
public class FileInfoAnalyzer {
    private static final Logger logger = LogsManager.getLogger(FileInfoAnalyzer.class);

    private static final int AUTHOR_NAME_OFFSET = "author ".length();
    private static final int AUTHOR_EMAIL_OFFSET = "author-mail ".length();
    private static final int AUTHOR_TIME_OFFSET = "author-time ".length();
    private static final int AUTHOR_TIMEZONE_OFFSET = "author-tz ".length();
    private static final int FULL_COMMIT_HASH_LENGTH = 40;

    private static final String MESSAGE_FILE_MISSING = "Unable to analyze the file located at \"%s\" "
            + "as the file is missing from your system. Skipping this file.";

    private static final String MESSAGE_SHALLOW_CLONING_LAST_MODIFIED_DATE_CONFLICT = "Repo %s was cloned using "
            + "shallow cloning. As such, the \"last modified date\" values may be incorrect.";

    /**
     * Analyzes the lines of the file, given in the {@code fileInfo}, that has changed in the time period provided
     * by {@code config}.
     * Returns null if the file is missing from the local system, or none of the
     * {@code Author} specified in {@code config} contributed to the file in {@code fileInfo}.
     */
    public static FileResult analyzeTextFile(RepoConfiguration config, FileInfo fileInfo) {
        String relativePath = fileInfo.getPath();

        if (Files.notExists(Paths.get(config.getRepoRoot(), relativePath))) {
            logger.severe(String.format(MESSAGE_FILE_MISSING, relativePath));
            return null;
        }

        if (FileUtil.isEmptyFile(config.getRepoRoot(), relativePath)) {
            return null;
        }

        aggregateBlameAuthorModifiedAndDateInfo(config, fileInfo);
        fileInfo.setFileType(config.getFileType(fileInfo.getPath()));

        AnnotatorAnalyzer.aggregateAnnotationAuthorInfo(fileInfo, config.getAuthorConfig());

        if (!config.getAuthorList().isEmpty() && fileInfo.isAllAuthorsIgnored(config.getAuthorList())) {
            return null;
        }

        return generateTextFileResult(fileInfo);
    }

    /**
     * Analyzes the binary file, given in the {@code fileInfo}, that has changed in the time period provided
     * by {@code config}.
     * Returns null if the file is missing from the local system, or none of the
     * {@code Author} specified in {@code config} contributed to the file in {@code fileInfo}.
     */
    public static FileResult analyzeBinaryFile(RepoConfiguration config, FileInfo fileInfo) {
        String relativePath = fileInfo.getPath();

        if (Files.notExists(Paths.get(config.getRepoRoot(), relativePath))) {
            logger.severe(String.format(MESSAGE_FILE_MISSING, relativePath));
            return null;
        }

        fileInfo.setFileType(config.getFileType(fileInfo.getPath()));

        return generateBinaryFileResult(config, fileInfo);
    }

    /**
     * Generates and returns a {@code FileResult} with the authorship results from {@code fileInfo} consolidated.
     */
    private static FileResult generateTextFileResult(FileInfo fileInfo) {
        HashMap<Author, Integer> authorContributionMap = new HashMap<>();
        for (LineInfo line : fileInfo.getLines()) {
            Author author = line.getAuthor();
            authorContributionMap.put(author, authorContributionMap.getOrDefault(author, 0) + 1);
        }
        return FileResult.createTextFileResult(
            fileInfo.getPath(), fileInfo.getFileType(), fileInfo.getLines(), authorContributionMap);
    }

    /**
     * Generates and returns a {@code FileResult} with the authorship results from binary {@code fileInfo} consolidated.
     * Authorship results are indicated in the {@code authorContributionMap} as contributions with zero line counts.
     * Returns {@code null} if none of the {@code Author} specified in {@code config} contributed to the file in
     * {@code fileInfo}.
     */
    private static FileResult generateBinaryFileResult(RepoConfiguration config, FileInfo fileInfo) {
        String authorsString = GitLog.getBinaryFileAuthors(config, fileInfo.getPath());
        if (authorsString.isEmpty()) { // Empty string, means no author at all
            return null;
        }

        Set<Author> authors = new HashSet<>();
        HashMap<Author, Integer> authorContributionMap = new HashMap<>();

        for (String authorString : authorsString.split("\n")) {
            String[] arr = authorString.split("\t");
            String authorName = arr[0];
            String authorEmail = arr[1];
            authors.add(config.getAuthor(authorName, authorEmail));
        }

        for (Author author : authors) {
            authorContributionMap.put(author, 0);
        }

        return FileResult.createBinaryFileResult(fileInfo.getPath(), fileInfo.getFileType(), authorContributionMap);
    }

    /**
     * Sets the {@code Author} and {@code Date} for each line in {@code fileInfo} based on the git blame analysis
     * on the file.
     */
    private static void aggregateBlameAuthorModifiedAndDateInfo(RepoConfiguration config, FileInfo fileInfo) {
        String blameResults = getGitBlameResult(config, fileInfo.getPath());
        String[] blameResultLines = blameResults.split("\n");
        Path filePath = Paths.get(fileInfo.getPath());
        Long sinceDateInMs = config.getSinceDate().getTime();
        Long untilDateInMs = config.getUntilDate().getTime();

        for (int lineCount = 0; lineCount < blameResultLines.length; lineCount += 5) {
            String commitHash = blameResultLines[lineCount].substring(0, FULL_COMMIT_HASH_LENGTH);
            String authorName = blameResultLines[lineCount + 1].substring(AUTHOR_NAME_OFFSET);
            String authorEmail = blameResultLines[lineCount + 2]
                    .substring(AUTHOR_EMAIL_OFFSET).replaceAll("<|>", "");
            Long commitDateInMs = Long.parseLong(blameResultLines[lineCount + 3].substring(AUTHOR_TIME_OFFSET)) * 1000;
            String authorTimeZone = blameResultLines[lineCount + 4].substring(AUTHOR_TIMEZONE_OFFSET);
            Author author = config.getAuthor(authorName, authorEmail);

            if (!fileInfo.isFileLineTracked(lineCount / 5) || author.isIgnoringFile(filePath)
                    || CommitHash.isInsideCommitList(commitHash, config.getIgnoreCommitList())
                    || commitDateInMs < sinceDateInMs || commitDateInMs > untilDateInMs) {
                author = Author.UNKNOWN_AUTHOR;
            }

            if (config.isLastModifiedDateIncluded()) {
                if (config.isShallowCloningPerformed()) {
                    logger.warning(String.format(
                            MESSAGE_SHALLOW_CLONING_LAST_MODIFIED_DATE_CONFLICT, config.getRepoName()));
                }
                // convert the commit date from the system default time zone to cli-specified timezone
                Date convertedCommitDate = TimeUtil.getZonedDateFromSystemDate(new Date(commitDateInMs),
                        ZoneId.of(config.getZoneId()));

                fileInfo.setLineLastModifiedDate(lineCount / 5, convertedCommitDate);
            }
            fileInfo.setLineAuthor(lineCount / 5, author);
        }
    }

    /**
     * Returns the analysis result from running git blame on {@code filePath}.
     */
    private static String getGitBlameResult(RepoConfiguration config, String filePath) {
        return GitBlame.blame(config.getRepoRoot(), filePath);
    }
}
