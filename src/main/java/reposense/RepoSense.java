package reposense;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.argparse4j.helper.HelpScreenException;
import reposense.model.AuthorConfiguration;
import reposense.model.CliArguments;
import reposense.model.ConfigCliArguments;
import reposense.model.GroupConfiguration;
import reposense.model.LocationsCliArguments;
import reposense.model.RepoConfiguration;
import reposense.model.RepoLocation;
import reposense.model.ReportConfiguration;
import reposense.model.ViewCliArguments;
import reposense.parser.ArgsParser;
import reposense.parser.AuthorConfigCsvParser;
import reposense.parser.GroupConfigCsvParser;
import reposense.parser.InvalidCsvException;
import reposense.parser.InvalidLocationException;
import reposense.parser.ParseException;
import reposense.parser.RepoConfigCsvParser;
import reposense.report.ReportGenerator;
import reposense.system.LogsManager;
import reposense.system.ReportServer;
import reposense.util.FileUtil;
import reposense.util.TimeUtil;

/**
 * The main RepoSense class.
 */
public class RepoSense {
    private static final Logger logger = LogsManager.getLogger(RepoSense.class);
    private static final int SERVER_PORT_NUMBER = 9000;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy z");
    private static final String VERSION_UNSPECIFIED = "unspecified";

    /**
     * The entry point of the program.
     */
    public static void main(String[] args) {
        try {
            TimeUtil.startTimer();
            CliArguments cliArguments = ArgsParser.parse(args);
            List<RepoConfiguration> configs = null;
            ReportConfiguration reportConfig = new ReportConfiguration();

            if (cliArguments instanceof ViewCliArguments) {
                ReportServer.startServer(SERVER_PORT_NUMBER, ((
                        ViewCliArguments) cliArguments).getReportDirectoryPath().toAbsolutePath());
                return;
            } else if (cliArguments instanceof ConfigCliArguments) {
                configs = getRepoConfigurations((ConfigCliArguments) cliArguments);
                reportConfig = ((ConfigCliArguments) cliArguments).getReportConfiguration();
            } else if (cliArguments instanceof LocationsCliArguments) {
                configs = getRepoConfigurations((LocationsCliArguments) cliArguments);
            } else {
                throw new AssertionError("CliArguments's subclass type is unhandled.");
            }

            RepoConfiguration.setFormatsToRepoConfigs(configs, cliArguments.getFormats());
            RepoConfiguration.setDatesToRepoConfigs(configs, cliArguments.getSinceDate(), cliArguments.getUntilDate());
            RepoConfiguration.setZoneIdToRepoConfigs(configs, cliArguments.getZoneId().toString());
            RepoConfiguration.setStandaloneConfigIgnoredToRepoConfigs(configs,
                    cliArguments.isStandaloneConfigIgnored());
            RepoConfiguration.setIsLastModifiedDateIncludedToRepoConfigs(configs,
                    cliArguments.isLastModifiedDateIncluded());
            RepoConfiguration.setIsShallowCloningPerformedToRepoConfigs(configs,
                    cliArguments.isShallowCloningPerformed());
            List<Path> reportFoldersAndFiles = ReportGenerator.generateReposReport(configs,
                    cliArguments.getOutputFilePath().toAbsolutePath().toString(),
                    cliArguments.getAssetsFilePath().toAbsolutePath().toString(), reportConfig,
                    formatter.format(ZonedDateTime.now(cliArguments.getZoneId())),
                    cliArguments.getSinceDate(), cliArguments.getUntilDate(),
                    cliArguments.isSinceDateProvided(), cliArguments.isUntilDateProvided(),
                    cliArguments.getNumCloningThreads(), cliArguments.getNumAnalysisThreads(),
                    TimeUtil::getElapsedTime, cliArguments.getZoneId());
            FileUtil.zipFoldersAndFiles(reportFoldersAndFiles, cliArguments.getOutputFilePath().toAbsolutePath(),
                    ".json");

            logger.info(TimeUtil.getElapsedTimeMessage());

            if (cliArguments.isAutomaticallyLaunching()) {
                ReportServer.startServer(SERVER_PORT_NUMBER, cliArguments.getOutputFilePath().toAbsolutePath());
            }
        } catch (IOException | ParseException | InvalidCsvException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (HelpScreenException e) {
            // help message was printed by the ArgumentParser; it is safe to exit.
        }
    }

    /**
     * Constructs a list of {@code RepoConfiguration} if {@code cliArguments} is a {@code ConfigCliArguments}.
     *
     * @throws IOException if user-supplied csv file does not exists or is not readable.
     * @throws InvalidCsvException if user-supplied repo-config csv is malformed.
     */
    public static List<RepoConfiguration> getRepoConfigurations(ConfigCliArguments cliArguments)
            throws IOException, InvalidCsvException {
        List<RepoConfiguration> repoConfigs = new RepoConfigCsvParser(cliArguments.getRepoConfigFilePath()).parse();
        List<AuthorConfiguration> authorConfigs;
        List<GroupConfiguration> groupConfigs;
        AuthorConfiguration.setHasAuthorConfigFile(true);

        try {
            authorConfigs = new AuthorConfigCsvParser(cliArguments.getAuthorConfigFilePath()).parse();
            RepoConfiguration.merge(repoConfigs, authorConfigs);
        } catch (FileNotFoundException fnfe) {
            // FileNotFoundException thrown as author-config.csv is not found.
            // Ignore exception as the file is optional.
        } catch (IOException | InvalidCsvException e) {
            // for all IO and invalid csv exceptions, log the error and continue
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        try {
            groupConfigs = new GroupConfigCsvParser(cliArguments.getGroupConfigFilePath()).parse();
            RepoConfiguration.setGroupConfigsToRepos(repoConfigs, groupConfigs);
        } catch (FileNotFoundException fnfe) {
            // FileNotFoundException thrown as groups-config.csv is not found.
            // Ignore exception as the file is optional.
        } catch (IOException | InvalidCsvException e) {
            // for all other IO and invalid csv exceptions, log the error and continue
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        return repoConfigs;
    }

    /**
     * Constructs a list of {@code RepoConfiguration} if {@code cliArguments} is a {@code LocationsCliArguments}.
     *
     * @throws ParseException if all repo locations are invalid.
     */
    public static List<RepoConfiguration> getRepoConfigurations(LocationsCliArguments cliArguments)
            throws ParseException {
        List<RepoConfiguration> configs = new ArrayList<>();
        for (String locationString : cliArguments.getLocations()) {
            try {
                configs.add(new RepoConfiguration(new RepoLocation(locationString)));
            } catch (InvalidLocationException ile) {
                logger.log(Level.WARNING, ile.getMessage(), ile);
            }
        }

        if (configs.isEmpty()) {
            throw new ParseException("All repository locations are invalid.");
        }

        return configs;
    }

    public static String getVersion() {
        String version = RepoSense.class.getPackage().getImplementationVersion();

        if (version == null || version.equals(VERSION_UNSPECIFIED)) {
            version = System.getProperty("version");
        }

        return version;
    }
}
