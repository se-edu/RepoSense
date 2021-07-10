package reposense.report;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import reposense.RepoSense;
import reposense.authorship.AuthorshipReporter;
import reposense.authorship.model.AuthorshipSummary;
import reposense.commits.CommitsReporter;
import reposense.commits.model.CommitContributionSummary;
import reposense.git.GitClone;
import reposense.git.GitLsTree;
import reposense.git.GitRevParse;
import reposense.git.GitShortlog;
import reposense.git.exception.GitBranchException;
import reposense.git.exception.GitCloneException;
import reposense.git.exception.InvalidFilePathException;
import reposense.model.Author;
import reposense.model.CommitHash;
import reposense.model.RepoConfiguration;
import reposense.model.RepoLocation;
import reposense.model.ReportConfiguration;
import reposense.model.StandaloneConfig;
import reposense.parser.SinceDateArgumentType;
import reposense.parser.StandaloneConfigJsonParser;
import reposense.report.exception.NoAuthorsWithCommitsFoundException;
import reposense.system.LogsManager;
import reposense.util.FileUtil;
import reposense.util.ProgressTracker;
import reposense.util.TimeUtil;

/**
 * Contains report generation related functionalities.
 */
public class ReportGenerator {
    private static final String REPOSENSE_CONFIG_FOLDER = "_reposense";
    private static final String REPOSENSE_CONFIG_FILE = "config.json";
    private static final Logger logger = LogsManager.getLogger(ReportGenerator.class);

    // zip file which contains all the report template files
    private static final String TEMPLATE_FILE = "/templateZip.zip";
    private static final String INDEX_PAGE_TEMPLATE = "index.html";
    private static final String INDEX_PAGE_DEFAULT_TITLE = "<title>RepoSense Report</title>";

    private static final String MESSAGE_INVALID_CONFIG_JSON = "%s Ignoring the config provided by %s (%s).";
    private static final String MESSAGE_ERROR_CREATING_DIRECTORY =
            "Error has occurred while creating repo directory for %s (%s), will skip this repo.";
    private static final String MESSAGE_NO_STANDALONE_CONFIG = "%s (%s) does not contain a standalone config file.";
    private static final String MESSAGE_IGNORING_STANDALONE_CONFIG = "Ignoring standalone config file in %s (%s).";
    private static final String MESSAGE_MALFORMED_STANDALONE_CONFIG = "%s/%s/%s is malformed for %s (%s).";
    private static final String MESSAGE_NO_AUTHORS_SPECIFIED =
            "%s (%s) has no authors specified, using all authors by default.";
    private static final String MESSAGE_NO_AUTHORS_WITH_COMMITS_FOUND =
            "No authors found with commits for %s (%s).";
    private static final String MESSAGE_START_ANALYSIS = "Analyzing %s (%s)...";
    private static final String MESSAGE_COMPLETE_ANALYSIS = "Analysis of %s (%s) completed!";
    private static final String MESSAGE_REPORT_GENERATED = "The report is generated at %s";
    private static final String MESSAGE_BRANCH_DOES_NOT_EXIST = "Branch %s does not exist in %s! Analysis terminated.";

    private static final String LOG_ERROR_CLONING = "Failed to clone from %s";
    private static final String LOG_BRANCH_DOES_NOT_EXIST = "Branch \"%s\" does not exist.";
    private static final String LOG_BRANCH_CONTAINS_ILLEGAL_FILE_PATH =
            "Branch contains file paths with illegal characters and not analyzable.";
    private static final String LOG_ERROR_CLONING_OR_BRANCHING = "Exception met while cloning or checking out.";
    private static final String LOG_UNEXPECTED_ERROR = "Unexpected error stack trace for %s:\n>%s";

    private static Date earliestSinceDate = null;
    private static ProgressTracker progressTracker = null;
    private static final List<String> assetsFilesWhiteList =
            Collections.unmodifiableList(Arrays.asList(new String[] {"favicon.ico"}));

    /**
     * Generates the authorship and commits JSON file for each repo in {@code configs} at {@code outputPath}, as
     * well as the summary JSON file of all the repos.
     *
     * @return the list of file paths that were generated.
     * @throws IOException if templateZip.zip does not exists in jar file.
     */
    public static List<Path> generateReposReport(List<RepoConfiguration> configs, String outputPath, String assetsPath,
            ReportConfiguration reportConfig, String generationDate, Date cliSinceDate, Date untilDate,
            boolean isSinceDateProvided, boolean isUntilDateProvided, int numCloningThreads, int numAnalysisThreads,
            Supplier<String> reportGenerationTimeProvider, ZoneId zoneId) throws IOException {
        prepareTemplateFile(reportConfig, outputPath);
        if (Files.exists(Paths.get(assetsPath))) {
            FileUtil.copyDirectoryContents(assetsPath, outputPath, assetsFilesWhiteList);
        }

        earliestSinceDate = null;
        progressTracker = new ProgressTracker(configs.size());

        List<Path> reportFoldersAndFiles = cloneAndAnalyzeRepos(configs, outputPath,
                numCloningThreads, numAnalysisThreads);

        Date reportSinceDate = (cliSinceDate.equals(SinceDateArgumentType.ARBITRARY_FIRST_COMMIT_DATE))
                ? earliestSinceDate : cliSinceDate;

        Optional<Path> summaryPath = FileUtil.writeJsonFile(
                new SummaryJson(configs, reportConfig, generationDate,
                        TimeUtil.getZonedDateFromSystemDate(reportSinceDate, zoneId),
                        TimeUtil.getZonedDateFromSystemDate(untilDate, zoneId), isSinceDateProvided,
                        isUntilDateProvided, RepoSense.getVersion(), ErrorSummary.getInstance().getErrorList(),
                        reportGenerationTimeProvider.get(), zoneId.toString()),
                getSummaryResultPath(outputPath));
        summaryPath.ifPresent(reportFoldersAndFiles::add);

        logger.info(String.format(MESSAGE_REPORT_GENERATED, outputPath));

        return reportFoldersAndFiles;
    }

    /**
     * Copies the template file to the specified {@code outputPath} for the repo report to be generated.
     * @throws IOException if template resource is not found.
     */
    private static void prepareTemplateFile(ReportConfiguration config, String outputPath) throws IOException {
        InputStream is = RepoSense.class.getResourceAsStream(TEMPLATE_FILE);
        FileUtil.copyTemplate(is, outputPath);
        setReportConfiguration(config, outputPath);
    }

    private static void setReportConfiguration(ReportConfiguration config, String outputPath) throws IOException {
        setLandingPageTitle(outputPath, config.getTitle());
    }

    /**
     * Set title of template file located at {@code filePath} to {@code pageTitle}
     */
    private static void setLandingPageTitle(String filePath, String pageTitle) throws IOException {
        Path indexPagePath = Paths.get(filePath, INDEX_PAGE_TEMPLATE);
        String line = new String(Files.readAllBytes(indexPagePath));
        String newLine = line.replaceAll(INDEX_PAGE_DEFAULT_TITLE, "<title>" + escapeHtml4(pageTitle) + "</title>");
        Files.write(indexPagePath, newLine.getBytes());
    }

    /**
     * Groups {@code RepoConfiguration} with the same {@code RepoLocation} together so that they are only cloned once.
     */
    private static Map<RepoLocation, List<RepoConfiguration>> groupConfigsByRepoLocation(
            List<RepoConfiguration> configs) {
        Map<RepoLocation, List<RepoConfiguration>> repoLocationMap = new HashMap<>();
        for (RepoConfiguration config : configs) {
            RepoLocation location = config.getLocation();

            if (!repoLocationMap.containsKey(location)) {
                repoLocationMap.put(location, new ArrayList<>());
            }
            repoLocationMap.get(location).add(config);
        }
        return repoLocationMap;
    }

    /**
     * Clone, analyze and generate the report for repositories in {@code repoLocationMap}.
     * Performs cloning and analysis of each repository in parallel, and generates the report.
     * Also removes any configs that failed to clone or analyze from {@code configs}.
     *
     * By default, runs in multi-threaded mode with {@code numCloningThreads} threads for cloning
     * and {@code numAnalysisThreads} threads for analysis.
     * To turn off multi-threading, run the program with the flags
     * {@code --cloning-threads 1 --analysis-threads 1}.
     *
     * @return A list of paths to the JSON report files generated for each repository.
     */
    private static List<Path> cloneAndAnalyzeRepos(List<RepoConfiguration> configs, String outputPath,
            int numCloningThreads, int numAnalysisThreads) {
        Map<RepoLocation, List<RepoConfiguration>> repoLocationMap = groupConfigsByRepoLocation(configs);
        List<RepoLocation> repoLocationList = new ArrayList<>(repoLocationMap.keySet());

        // Fixed thread pools are used to limit the number of threads used by cloning and analysis jobs at any one time
        ExecutorService cloneExecutor = Executors.newFixedThreadPool(numCloningThreads);
        ExecutorService analyzeExecutor = Executors.newFixedThreadPool(numAnalysisThreads);

        List<CompletableFuture<AnalyzeJobOutput>> analyzeJobFutures = new ArrayList<>();
        for (RepoLocation location : repoLocationList) {
            List<RepoConfiguration> configsToAnalyze = repoLocationMap.get(location);

            // The `CompletableFuture.supplyAsync` method is used to clone the repo in parallel.
            // Note that the `cloneExecutor` is passed as a parameter to ensure that the number of threads used
            // for cloning is no more than `numCloningThreads`.
            CompletableFuture<CloneJobOutput> cloneFuture = CompletableFuture.supplyAsync(() ->
                    cloneRepo(configsToAnalyze.get(0), location), cloneExecutor);

            // The `thenApplyAsync` method is used to analyze the cloned repo in parallel.
            // This ensures that the analysis job for each repo will only be run after the repo has been cloned.
            // Note that the `analyzeExecutor` is passed as a parameter to ensure that the number of threads used
            // for analysis is no more than `numAnalysisThreads`.
            CompletableFuture<AnalyzeJobOutput> analyzeFuture = cloneFuture.thenApplyAsync(
                    cloneJobOutput -> analyzeRepos(outputPath, configsToAnalyze, cloneJobOutput), analyzeExecutor);

            analyzeJobFutures.add(analyzeFuture);
        }

        // Next, we collect the list of outputs from all the analyze jobs
        List<AnalyzeJobOutput> jobOutputs = analyzeJobFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Finally, the ExecutorService objects are shut down to prevent memory leaks
        cloneExecutor.shutdown();
        analyzeExecutor.shutdown();

        List<Path> generatedFiles = jobOutputs.stream()
                .flatMap(jobOutput -> jobOutput.getFiles().stream())
                .collect(Collectors.toList());

        List<RepoLocation> cloneFailLocations = jobOutputs
                .stream()
                .filter(jobOutput -> !jobOutput.isCloneSuccessful())
                .map(jobOutput -> jobOutput.getLocation())
                .collect(Collectors.toList());
        cloneFailLocations.forEach(location -> handleCloningFailed(configs, location));

        List<AnalysisErrorInfo> analysisErrors = jobOutputs
                .stream()
                .flatMap(jobOutput -> jobOutput.getAnalyseErrors().stream())
                .collect(Collectors.toList());
        analysisErrors.forEach(errorInfo ->
                handleAnalysisFailed(configs, errorInfo.getFailedConfig(), errorInfo.getErrorMessage()));

        RepoCloner repoCloner = new RepoCloner();
        repoCloner.cleanup();
        return generatedFiles;
    }

    /**
     * Clones repo at {@code location}.
     *
     * @return A {@code CloneJobOutput} object comprising the {@code location} of the repo, whether the cloning was
     * successful, and the {@code defaultBranch} of the repo.
     */
    private static CloneJobOutput cloneRepo(RepoConfiguration config, RepoLocation location) {
        RepoCloner repoCloner = new RepoCloner();
        repoCloner.cloneBare(config);
        RepoLocation clonedRepoLocation = repoCloner.getClonedRepoLocation();
        if (clonedRepoLocation != null) {
            String defaultBranch = repoCloner.getCurrentRepoDefaultBranch();
            return new CloneJobOutput(location, defaultBranch);
        } else {
            return new CloneJobOutput(location);
        }
    }

    /**
     * Analyzes all repos in {@code configsToAnalyze} and generates their report.
     *
     * @return An {@code AnalyzeJobOutput} object comprising the {@code location} of the repo, whether the cloning was
     * successful, the list of {@code filesGenerated} by the analysis and a list of {@code analysisErrors} encountered.
     */
    private static AnalyzeJobOutput analyzeRepos(String outputPath, List<RepoConfiguration> configsToAnalyze,
            CloneJobOutput cloneJobOutput) {
        RepoLocation location = cloneJobOutput.getLocation();
        boolean cloneSuccessful = cloneJobOutput.isCloneSuccessful();

        List<Path> generatedFiles = new ArrayList<>();
        List<AnalysisErrorInfo> analysisErrors = new ArrayList<>();
        RepoCloner repoCloner = new RepoCloner();
        if (!cloneSuccessful) {
            repoCloner.cleanupRepo(configsToAnalyze.get(0));
            return new AnalyzeJobOutput(location, cloneSuccessful, generatedFiles, analysisErrors);
        }
        Iterator<RepoConfiguration> itr = configsToAnalyze.iterator();
        while (itr.hasNext()) {
            progressTracker.incrementProgress();
            RepoConfiguration configToAnalyze = itr.next();
            configToAnalyze.updateBranch(cloneJobOutput.getDefaultBranch());

            Path repoReportDirectory = Paths.get(outputPath, configToAnalyze.getOutputFolderName());
            logger.info(
                    String.format(progressTracker.getProgress() + " "
                            + MESSAGE_START_ANALYSIS, configToAnalyze.getLocation(), configToAnalyze.getBranch()));
            try {
                GitRevParse.assertBranchExists(configToAnalyze, FileUtil.getBareRepoPath(configToAnalyze));
                GitLsTree.validateFilePaths(configToAnalyze, FileUtil.getBareRepoPath(configToAnalyze));
                GitClone.cloneFromBareAndUpdateBranch(Paths.get(FileUtil.REPOS_ADDRESS), configToAnalyze);

                FileUtil.createDirectory(repoReportDirectory);
                generatedFiles.addAll(analyzeRepo(configToAnalyze, repoReportDirectory.toString()));
            } catch (IOException ioe) {
                String logMessage = String.format(MESSAGE_ERROR_CREATING_DIRECTORY,
                        configToAnalyze.getLocation(), configToAnalyze.getBranch());
                logger.log(Level.WARNING, logMessage, ioe);
            } catch (GitBranchException gbe) {
                logger.log(Level.SEVERE, String.format(MESSAGE_BRANCH_DOES_NOT_EXIST,
                        configToAnalyze.getBranch(), configToAnalyze.getLocation()), gbe);
                analysisErrors.add(new AnalysisErrorInfo(configToAnalyze,
                        String.format(LOG_BRANCH_DOES_NOT_EXIST, configToAnalyze.getBranch())));
            } catch (InvalidFilePathException ipe) {
                analysisErrors.add(new AnalysisErrorInfo(configToAnalyze, LOG_BRANCH_CONTAINS_ILLEGAL_FILE_PATH));
            } catch (GitCloneException gce) {
                analysisErrors.add(new AnalysisErrorInfo(configToAnalyze, LOG_ERROR_CLONING_OR_BRANCHING));
            } catch (NoAuthorsWithCommitsFoundException nafe) {
                logger.log(Level.WARNING, String.format(MESSAGE_NO_AUTHORS_WITH_COMMITS_FOUND,
                        configToAnalyze.getLocation(), configToAnalyze.getBranch()));
                generatedFiles.addAll(generateEmptyRepoReport(repoReportDirectory.toString(),
                        Author.NAME_NO_AUTHOR_WITH_COMMITS_FOUND));
                generateEmptyRepoReport(repoReportDirectory.toString(), Author.NAME_NO_AUTHOR_WITH_COMMITS_FOUND);
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.log(Level.SEVERE, sw.toString());
                analysisErrors.add(new AnalysisErrorInfo(configToAnalyze,
                        String.format(LOG_UNEXPECTED_ERROR, configToAnalyze.getLocation(), sw.toString())));
            }
        }
        repoCloner.cleanupRepo(configsToAnalyze.get(0));
        return new AnalyzeJobOutput(location, cloneSuccessful, generatedFiles, analysisErrors);
    }

    /**
     * Analyzes repo specified by {@code config} and generates the report.
     * @return A list of paths to the JSON report files generated for the repo specified by {@code config}.
     */
    private static List<Path> analyzeRepo(
            RepoConfiguration config, String repoReportDirectory) throws NoAuthorsWithCommitsFoundException {
        // preprocess the config and repo
        updateRepoConfig(config);
        updateAuthorList(config);
        updateIgnoreCommitList(config);

        AuthorshipSummary authorshipSummary = AuthorshipReporter.generateAuthorshipSummary(config);
        CommitContributionSummary commitSummary = CommitsReporter.generateCommitSummary(config);
        List<Path> generatedFiles = generateIndividualRepoReport(repoReportDirectory, commitSummary, authorshipSummary);
        logger.info(String.format(MESSAGE_COMPLETE_ANALYSIS, config.getLocation(), config.getBranch()));
        return generatedFiles;
    }

    /**
     * Updates {@code config} with configuration provided by repository if exists.
     */
    public static void updateRepoConfig(RepoConfiguration config) {
        Path configJsonPath =
                Paths.get(config.getRepoRoot(), REPOSENSE_CONFIG_FOLDER, REPOSENSE_CONFIG_FILE).toAbsolutePath();

        if (!Files.exists(configJsonPath)) {
            logger.info(String.format(MESSAGE_NO_STANDALONE_CONFIG, config.getLocation(), config.getBranch()));
            return;
        }

        if (config.isStandaloneConfigIgnored()) {
            logger.info(String.format(MESSAGE_IGNORING_STANDALONE_CONFIG, config.getLocation(), config.getBranch()));
            return;
        }

        try {
            StandaloneConfig standaloneConfig = new StandaloneConfigJsonParser().parse(configJsonPath);
            config.update(standaloneConfig);
        } catch (JsonSyntaxException jse) {
            logger.warning(String.format(MESSAGE_MALFORMED_STANDALONE_CONFIG, config.getDisplayName(),
                    REPOSENSE_CONFIG_FOLDER, REPOSENSE_CONFIG_FILE, config.getLocation(), config.getBranch()));
        } catch (IllegalArgumentException iae) {
            logger.warning(String.format(MESSAGE_INVALID_CONFIG_JSON,
                    iae.getMessage(), config.getLocation(), config.getBranch()));
        } catch (IOException ioe) {
            throw new AssertionError(
                    "This exception should not happen as we have performed the file existence check.");
        }
    }

    /**
     * Find and update {@code config} with all the author identities if author list is empty.
     * Also removes ignored authors from author list.
     */
    private static void updateAuthorList(RepoConfiguration config) throws NoAuthorsWithCommitsFoundException {
        if (config.getAuthorList().isEmpty()) {
            logger.info(String.format(MESSAGE_NO_AUTHORS_SPECIFIED, config.getLocation(), config.getBranch()));
            List<Author> authorList = GitShortlog.getAuthors(config);

            if (authorList.isEmpty()) {
                throw new NoAuthorsWithCommitsFoundException();
            }

            config.setAuthorList(authorList);
        }
        config.removeIgnoredAuthors();
    }

    /**
     * Updates {@code config} with the exact list of commits if commit ranges are provided.
     */
    private static void updateIgnoreCommitList(RepoConfiguration config) {
        List<CommitHash> updatedIgnoreCommitList = config.getIgnoreCommitList().stream()
                .flatMap(x -> CommitHash.getHashes(config.getRepoRoot(), config.getBranch(), x))
                .collect(Collectors.toList());
        config.setIgnoreCommitList(updatedIgnoreCommitList);
    }

    /**
     * Adds {@code configs} that were not successfully cloned from {@code failedRepoLocation}
     * into the list of errors in the summary report and removes them from the list of {@code configs}.
     */
    private static void handleCloningFailed(List<RepoConfiguration> configs, RepoLocation failedRepoLocation) {
        List<RepoConfiguration> failedConfigs = configs.stream()
                .filter(config -> config.getLocation().equals(failedRepoLocation))
                .collect(Collectors.toList());
        handleFailedConfigs(configs, failedConfigs, String.format(LOG_ERROR_CLONING, failedRepoLocation));
    }

    /**
     * Adds {@code failedConfig} that failed analysis into the list of errors in the summary report and
     * removes {@code failedConfig} from the list of {@code configs}.
     */
    private static void handleAnalysisFailed(List<RepoConfiguration> configs, RepoConfiguration failedConfig,
            String errorMessage) {
        handleFailedConfigs(configs, Collections.singletonList(failedConfig), errorMessage);
    }

    /**
     * Adds {@code failedConfigs} that failed cloning/analysis into the list of errors in the summary report and
     * removes {@code failedConfigs} from the list of {@code configs}.
     */
    private static void handleFailedConfigs(
            List<RepoConfiguration> configs, List<RepoConfiguration> failedConfigs, String errorMessage) {
        Iterator<RepoConfiguration> itr = configs.iterator();
        while (itr.hasNext()) {
            RepoConfiguration config = itr.next();
            if (failedConfigs.contains(config)) {
                ErrorSummary.getInstance().addErrorMessage(config.getDisplayName(), errorMessage);
                itr.remove();
            }
        }
    }

    /**
     * Generates a report at the {@code repoReportDirectory}.
     * @return A list of paths to the JSON report files generated for this empty report.
     */
    private static List<Path> generateEmptyRepoReport(String repoReportDirectory, String displayName) {
        CommitReportJson emptyCommitReportJson = new CommitReportJson(displayName);

        List<Path> generatedFiles = new ArrayList<>();
        FileUtil.writeJsonFile(emptyCommitReportJson, getIndividualCommitsPath(repoReportDirectory))
                .ifPresent(generatedFiles::add);
        FileUtil.writeJsonFile(Collections.emptyList(), getIndividualAuthorshipPath(repoReportDirectory))
                .ifPresent(generatedFiles::add);

        return generatedFiles;
    }

    /**
     * Generates a report for a single repository at {@code repoReportDirectory}.
     * @return A list of paths to the JSON report files generated for this report.
     */
    private static List<Path> generateIndividualRepoReport(
            String repoReportDirectory, CommitContributionSummary commitSummary, AuthorshipSummary authorshipSummary) {
        CommitReportJson commitReportJson = new CommitReportJson(commitSummary, authorshipSummary);

        List<Path> generatedFiles = new ArrayList<>();
        FileUtil.writeJsonFile(commitReportJson, getIndividualCommitsPath(repoReportDirectory))
                .ifPresent(generatedFiles::add);
        FileUtil.writeJsonFile(authorshipSummary.getFileResults(), getIndividualAuthorshipPath(repoReportDirectory))
                .ifPresent(generatedFiles::add);
        return generatedFiles;
    }

    private static String getSummaryResultPath(String targetFileLocation) {
        return targetFileLocation + "/" + SummaryJson.SUMMARY_JSON_FILE_NAME;
    }

    private static String getIndividualAuthorshipPath(String repoReportDirectory) {
        return repoReportDirectory + "/authorship.json";
    }

    private static String getIndividualCommitsPath(String repoReportDirectory) {
        return repoReportDirectory + "/commits.json";
    }

    public static void setEarliestSinceDate(Date newEarliestSinceDate) {
        if (earliestSinceDate == null || newEarliestSinceDate.before(earliestSinceDate)) {
            earliestSinceDate = newEarliestSinceDate;
        }
    }
}
