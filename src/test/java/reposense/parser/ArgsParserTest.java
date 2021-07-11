package reposense.parser;

import static org.apache.tools.ant.types.Commandline.translateCommandline;

import static reposense.util.TestUtil.loadResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.argparse4j.helper.HelpScreenException;
import reposense.RepoSense;
import reposense.model.CliArguments;
import reposense.model.ConfigCliArguments;
import reposense.model.FileType;
import reposense.model.FileTypeTest;
import reposense.model.LocationsCliArguments;
import reposense.model.RepoConfiguration;
import reposense.model.ViewCliArguments;
import reposense.util.FileUtil;
import reposense.util.InputBuilder;
import reposense.util.TestUtil;
import reposense.util.TimeUtil;

public class ArgsParserTest {

    private static final Path PROJECT_DIRECTORY = Paths.get(System.getProperty("user.dir"));
    private static final Path CONFIG_DIRECTORY = Paths.get(System.getProperty("user.dir")
            + File.separator + "config" + File.separator);
    private static final Path CONFIG_FOLDER_ABSOLUTE = loadResource(ArgsParserTest.class, "cli_location_test");
    private static final Path OUTPUT_DIRECTORY_ABSOLUTE = loadResource(ArgsParserTest.class, "output");
    private static final Path CONFIG_FOLDER_RELATIVE = PROJECT_DIRECTORY.relativize(CONFIG_FOLDER_ABSOLUTE);
    private static final Path OUTPUT_DIRECTORY_RELATIVE = PROJECT_DIRECTORY.relativize(OUTPUT_DIRECTORY_ABSOLUTE);
    private static final Path REPO_CONFIG_CSV_FILE =
            CONFIG_FOLDER_ABSOLUTE.resolve(RepoConfigCsvParser.REPO_CONFIG_FILENAME);
    private static final Path AUTHOR_CONFIG_CSV_FILE =
            CONFIG_FOLDER_ABSOLUTE.resolve(AuthorConfigCsvParser.AUTHOR_CONFIG_FILENAME);
    private static final String NONEXISTENT_DIRECTORY = "some_non_existent_dir/";

    private static final Path REPORT_CONFIG_FOLDER_ABSOLUTE_ONE = loadResource(ArgsParserTest.class,
            "report_config_test/one");
    private static final Path REPORT_CONFIG_FOLDER_ABSOLUTE_TWO = loadResource(ArgsParserTest.class,
            "report_config_test/two");
    private static final Path REPORT_CONFIG_FOLDER_ABSOLUTE_THREE = loadResource(ArgsParserTest.class,
            "report_config_test/three");

    private static final InputBuilder DEFAULT_INPUT_BUILDER = new InputBuilder();

    private static final String TEST_REPO_REPOSENSE = "https://github.com/reposense/RepoSense.git";
    private static final String TEST_REPO_BETA = "https://github.com/reposense/testrepo-Beta.git";
    private static final String TEST_REPO_CHARLIE = "https://github.com/reposense/testrepo-Charlie.git";
    private static final String TEST_REPO_DELTA = "https://github.com/reposense/testrepo-Delta.git";

    private static final String DEFAULT_TIMEZONE = "Asia/Singapore";
    private static final ZoneId TIME_ZONE_ID = TestUtil.getZoneId(DEFAULT_TIMEZONE);

    @Before
    public void before() {
        DEFAULT_INPUT_BUILDER.reset().addConfig(CONFIG_FOLDER_ABSOLUTE);
    }

    @After
    public void after() {
        try {
            FileUtil.deleteDirectory(PROJECT_DIRECTORY.resolve(NONEXISTENT_DIRECTORY).toString());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void parse_allCorrectInputs_success() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addOutput(OUTPUT_DIRECTORY_ABSOLUTE)
                .addSinceDate("01/07/2017")
                .addUntilDate("30/11/2017")
                .addFormats("java adoc html css js")
                .addIgnoreStandaloneConfig()
                .addView()
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                OUTPUT_DIRECTORY_ABSOLUTE.resolve(ArgsParser.DEFAULT_REPORT_NAME), cliArguments.getOutputFilePath()));

        Date expectedSinceDate = TestUtil.getSinceDate(2017, Calendar.JULY, 1);
        Date expectedUntilDate = TestUtil.getUntilDate(2017, Calendar.NOVEMBER, 30);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        List<FileType> expectedFormats = FileType.convertFormatStringsToFileTypes(
                Arrays.asList("java", "adoc", "html", "css", "js"));
        Assert.assertEquals(expectedFormats, cliArguments.getFormats());

        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());

        Assert.assertEquals(ZoneId.of(DEFAULT_TIMEZONE), cliArguments.getZoneId());
    }

    @Test(expected = HelpScreenException.class)
    public void parse_help_throwsHelpScreenException() throws Exception {
        String input = "--help";
        ArgsParser.parse(translateCommandline(input));
    }

    @Test
    public void parse_allCorrectInputsAlias_success() throws Exception {
        String input = String.format(
                "-c \"%s\" -o \"%s\" -s 01/07/2017 -u 30/11/2017 -f java adoc html css js -i -v -t %s",
                CONFIG_FOLDER_ABSOLUTE, OUTPUT_DIRECTORY_ABSOLUTE, DEFAULT_TIMEZONE);
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                OUTPUT_DIRECTORY_ABSOLUTE.resolve(ArgsParser.DEFAULT_REPORT_NAME), cliArguments.getOutputFilePath()));

        Date expectedSinceDate = TestUtil.getSinceDate(2017, Calendar.JULY, 1);
        Date expectedUntilDate = TestUtil.getUntilDate(2017, Calendar.NOVEMBER, 30);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        List<FileType> expectedFormats = FileType.convertFormatStringsToFileTypes(
                Arrays.asList("java", "adoc", "html", "css", "js"));
        Assert.assertEquals(expectedFormats, cliArguments.getFormats());

        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());

        Assert.assertEquals(ZoneId.of(DEFAULT_TIMEZONE), cliArguments.getZoneId());
    }

    @Test
    public void parse_withExtraWhitespaces_success() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE).addWhiteSpace(5)
                .addOutput(OUTPUT_DIRECTORY_ABSOLUTE).addWhiteSpace(4)
                .addSinceDate("01/07/2017").addWhiteSpace(3)
                .addUntilDate("30/11/2017").addWhiteSpace(6)
                .addFormats("java   adoc  html      css js   ")
                .addIgnoreStandaloneConfig().addWhiteSpace(1)
                .addView().addWhiteSpace(4)
                .addTimezone(DEFAULT_TIMEZONE).addWhiteSpace(5)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                OUTPUT_DIRECTORY_ABSOLUTE.resolve(ArgsParser.DEFAULT_REPORT_NAME), cliArguments.getOutputFilePath()));

        Date expectedSinceDate = TestUtil.getSinceDate(2017, Calendar.JULY, 1);
        Date expectedUntilDate = TestUtil.getUntilDate(2017, Calendar.NOVEMBER, 30);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        List<FileType> expectedFormats = FileType.convertFormatStringsToFileTypes(Arrays.asList(
                "java", "adoc", "html", "css", "js"));
        Assert.assertEquals(expectedFormats, cliArguments.getFormats());

        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());

        Assert.assertEquals(ZoneId.of(DEFAULT_TIMEZONE), cliArguments.getZoneId());
    }

    @Test
    public void parse_configFolderOnly_success() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        // Optional arguments have default values
        assertDateDiffOneMonth(cliArguments.getSinceDate(), cliArguments.getUntilDate(), cliArguments.getZoneId());
        assertDateDiffEndOfDay(cliArguments.getUntilDate(), cliArguments.getZoneId());
        Assert.assertEquals(ArgsParser.DEFAULT_REPORT_NAME, cliArguments.getOutputFilePath().getFileName().toString());
        Assert.assertEquals(FileTypeTest.NO_SPECIFIED_FORMATS, cliArguments.getFormats());
        Assert.assertFalse(cliArguments.isAutomaticallyLaunching());

        input = new InputBuilder().addConfig(CONFIG_FOLDER_RELATIVE).build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        // Optional arguments have default values
        assertDateDiffOneMonth(cliArguments.getSinceDate(), cliArguments.getUntilDate(), cliArguments.getZoneId());
        assertDateDiffEndOfDay(cliArguments.getUntilDate(), cliArguments.getZoneId());
        Assert.assertEquals(ArgsParser.DEFAULT_REPORT_NAME, cliArguments.getOutputFilePath().getFileName().toString());
        Assert.assertEquals(FileTypeTest.NO_SPECIFIED_FORMATS, cliArguments.getFormats());
        Assert.assertFalse(cliArguments.isAutomaticallyLaunching());
        Assert.assertEquals(ZoneId.systemDefault(), cliArguments.getZoneId());
    }

    @Test
    public void parse_viewOnly_success() throws Exception {
        String input = new InputBuilder().addView(OUTPUT_DIRECTORY_ABSOLUTE).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ViewCliArguments);
        Assert.assertTrue(Files.isSameFile(
                OUTPUT_DIRECTORY_ABSOLUTE, ((ViewCliArguments) cliArguments).getReportDirectoryPath()));
    }

    @Test
    public void parse_withIgnore_success() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA)
                .addIgnoreStandaloneConfig()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        String inputWithAlias = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA)
                .add("-i")
                .build();
        CliArguments cliArgumentsWithAlias = ArgsParser.parse(translateCommandline(inputWithAlias));

        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        Assert.assertTrue(cliArgumentsWithAlias instanceof LocationsCliArguments);

        Assert.assertTrue(((LocationsCliArguments) cliArguments).isStandaloneConfigIgnored());
        Assert.assertTrue(((LocationsCliArguments) cliArgumentsWithAlias).isStandaloneConfigIgnored());

        Assert.assertEquals(cliArguments, cliArgumentsWithAlias);
    }

    @Test
    public void parse_withoutIgnore_success() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        Assert.assertFalse(((LocationsCliArguments) cliArguments).isStandaloneConfigIgnored());
    }

    @Test
    public void parse_viewOnlyWithoutArgs_returnsConfigCliArguments() throws Exception {
        String input = new InputBuilder().addView().build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(CONFIG_DIRECTORY.toString(), (
                (ConfigCliArguments) cliArguments).getConfigFolderPath().toString());
        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());
    }

    @Test
    public void parse_configFolderAndOutputDirectory_success() throws Exception {
        Path expectedRelativeOutputDirectoryPath = OUTPUT_DIRECTORY_RELATIVE.resolve(ArgsParser.DEFAULT_REPORT_NAME);
        Path expectedAbsoluteOutputDirectoryPath = OUTPUT_DIRECTORY_ABSOLUTE.resolve(ArgsParser.DEFAULT_REPORT_NAME);

        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addOutput(OUTPUT_DIRECTORY_RELATIVE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(expectedRelativeOutputDirectoryPath, cliArguments.getOutputFilePath()));

        input = new InputBuilder().addConfig(CONFIG_FOLDER_RELATIVE)
                .addOutput(OUTPUT_DIRECTORY_ABSOLUTE)
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(expectedAbsoluteOutputDirectoryPath, cliArguments.getOutputFilePath()));
    }

    @Test
    public void parse_configFolderAndViewWithouthArgs_returnsConfigCliArguments() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addView()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());

        input = new InputBuilder().addConfig(CONFIG_FOLDER_RELATIVE)
                .addView()
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());
    }

    @Test
    public void parse_configFolderAndViewWithArgs_returnsConfigCliArguments() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addView(OUTPUT_DIRECTORY_ABSOLUTE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                REPO_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getRepoConfigFilePath()));
        Assert.assertTrue(Files.isSameFile(
                AUTHOR_CONFIG_CSV_FILE, ((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()));
        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());
    }

    @Test
    public void sinceDate_correctFormat_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addSinceDate("01/07/2017")
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Date expectedSinceDate = TestUtil.getSinceDate(2017, Calendar.JULY, 1);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
    }

    @Test
    public void untilDate_correctFormat_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addUntilDate("30/11/2017")
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Date expectedUntilDate = TestUtil.getUntilDate(2017, Calendar.NOVEMBER, 30);
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void sinceDate_withExtraDate_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addSinceDate("\"01/07/2017 01/07/2018\"")
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Date expectedSinceDate = TestUtil.getSinceDate(2017, Calendar.JULY, 1);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
    }

    @Test
    public void untilDate_withExtraTime_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addUntilDate("\"30/11/2017 10:10:10\"")
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Date expectedUntilDate = TestUtil.getUntilDate(2017, Calendar.NOVEMBER, 30);
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void period_inDaysWithSinceDate_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addSinceDate("01/07/2017")
                .addPeriod("2d")
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Date expectedUntilDate = TestUtil.getUntilDate(2017, Calendar.JULY, 2);
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void period_inWeeksWithUntilDate_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addUntilDate("14/07/2017")
                .addTimezone(DEFAULT_TIMEZONE)
                .addPeriod("2w")
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Date expectedSinceDate = TestUtil.getSinceDate(2017, Calendar.JULY, 1);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
    }

    @Test
    public void formats_inAlphanumeric_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addFormats("java js css 7z").build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        List<FileType> expectedFormats = FileType.convertFormatStringsToFileTypes(
                Arrays.asList("java", "js", "css", "7z"));
        Assert.assertEquals(expectedFormats, cliArguments.getFormats());
    }

    @Test
    public void numCloningThreads_default_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        int expectedNumThreads = ArgsParser.DEFAULT_NUM_CLONING_THREADS;
        Assert.assertEquals(expectedNumThreads, cliArguments.getNumCloningThreads());
    }

    @Test
    public void numCloningThreads_isNumeric_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addNumCloningThreads(2)
                .build();
        System.out.println(input);
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        int expectedNumThreads = 2;
        Assert.assertEquals(expectedNumThreads, cliArguments.getNumCloningThreads());
    }

    @Test
    public void numAnalysisThreads_default_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        int expectedNumThreads = ArgsParser.DEFAULT_NUM_ANALYSIS_THREADS;
        Assert.assertEquals(expectedNumThreads, cliArguments.getNumAnalysisThreads());
    }

    @Test
    public void numAnalysisThreads_isNumeric_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addNumAnalysisThreads(2)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        int expectedNumThreads = 2;
        Assert.assertEquals(expectedNumThreads, cliArguments.getNumAnalysisThreads());
    }

    @Test
    public void parse_validGitRepoLocations_repoConfigurationListCorrectSize() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        List<RepoConfiguration> repoConfigs = RepoSense.getRepoConfigurations((LocationsCliArguments) cliArguments);
        Assert.assertEquals(2, repoConfigs.size());
    }

    @Test
    public void parse_repoLocationsAndView_returnsLocationCliArguments() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA)
                .addView()
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());
        List<String> expectedLocations = Arrays.asList(TEST_REPO_REPOSENSE, TEST_REPO_DELTA);
        Assert.assertEquals(expectedLocations, ((LocationsCliArguments) cliArguments).getLocations());
    }

    @Test
    public void parse_repoLocationsOnly_success() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        Assert.assertFalse(cliArguments.isAutomaticallyLaunching());
        List<String> expectedLocations = Arrays.asList(TEST_REPO_REPOSENSE, TEST_REPO_DELTA);
        Assert.assertEquals(expectedLocations, ((LocationsCliArguments) cliArguments).getLocations());
    }

    @Test
    public void parse_repoLocationsAndViewWithArgs_returnsLocationsCliArguments() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_REPOSENSE, TEST_REPO_DELTA)
                .addView(OUTPUT_DIRECTORY_ABSOLUTE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        Assert.assertTrue(cliArguments.isAutomaticallyLaunching());
        List<String> expectedLocations = Arrays.asList(TEST_REPO_REPOSENSE, TEST_REPO_DELTA);
        Assert.assertEquals(expectedLocations, ((LocationsCliArguments) cliArguments).getLocations());
    }

    @Test
    public void parse_viewWithArgCwd_returnsViewCliArguments() throws Exception {
        String input = new InputBuilder().addView(new File(".").toPath()).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ViewCliArguments);
    }

    @Test
    public void parse_configOrLocationsSimilar_success() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE).build();
        CliArguments configCliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(configCliArguments instanceof ConfigCliArguments);
        List<RepoConfiguration> actualRepoConfigs =
                RepoSense.getRepoConfigurations((ConfigCliArguments) configCliArguments);

        input = new InputBuilder().addRepos(TEST_REPO_BETA, TEST_REPO_CHARLIE, TEST_REPO_DELTA).build();
        CliArguments locationCliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(locationCliArguments instanceof LocationsCliArguments);
        List<RepoConfiguration> expectedRepoConfigs =
                RepoSense.getRepoConfigurations((LocationsCliArguments) locationCliArguments);

        Assert.assertEquals(actualRepoConfigs, expectedRepoConfigs);
    }

    @Test
    public void emptyArgs_defaultConfigFolderPath() throws Exception {
        CliArguments cliArguments = ArgsParser.parse(new String[]{});
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(CONFIG_DIRECTORY.toString(), (
                (ConfigCliArguments) cliArguments).getConfigFolderPath().toString());
    }

    @Test
    public void parse_repoAliases_sameResult() throws Exception {
        String input = new InputBuilder().addRepos(TEST_REPO_BETA).build();
        CliArguments repoAliasCliArguments = ArgsParser.parse(translateCommandline(input));

        input = new InputBuilder().add(String.format("--repos %s", TEST_REPO_BETA)).build();
        CliArguments reposAliasCliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertEquals(repoAliasCliArguments, reposAliasCliArguments);
    }

    @Test (expected = ParseException.class)
    public void parse_noValidRepoLocation_throwsParseException()
            throws ParseException, HelpScreenException {
        String input = new InputBuilder().addRepos("https://githubaaaa.com/asdasdasdasd/RepoSense").build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof LocationsCliArguments);
        RepoSense.getRepoConfigurations((LocationsCliArguments) cliArguments);
    }

    @Test(expected = ParseException.class)
    public void absoluteConfigFolder_withoutRequiredConfigFiles_throwsParseException() throws Exception {
        Path absDirectory = PROJECT_DIRECTORY.getParent().toAbsolutePath();
        String input = new InputBuilder().addConfig(absDirectory).build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void relativeConfigFolder_withoutRequiredConfigFiles_throwsParseException() throws Exception {
        Path relDirectory = PROJECT_DIRECTORY.getParent();
        String input = new InputBuilder().addConfig(relDirectory).build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void parse_notExistsConfigFolder_throwsParseException() throws Exception {
        Path absConfigFolder = PROJECT_DIRECTORY.resolve("non_existing_random_folder");
        String input = new InputBuilder().addConfig(absConfigFolder).build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void parse_configCsvFileAsConfigFolder_throwsParseException() throws Exception {
        String input = new InputBuilder().addConfig(REPO_CONFIG_CSV_FILE).build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void parse_missingConfigValue_throwsParseException() throws Exception {
        String input = new InputBuilder().addConfig(new File("").toPath()).build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test
    public void outputPath_nonExistentDirectory_success() throws Exception {
        Path nonExistentDirectory = PROJECT_DIRECTORY.resolve(NONEXISTENT_DIRECTORY);
        Path expectedRelativeOutputDirectoryPath = nonExistentDirectory.resolve(ArgsParser.DEFAULT_REPORT_NAME);
        String input = new InputBuilder().addOutput(nonExistentDirectory).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertTrue(Files.isSameFile(
                expectedRelativeOutputDirectoryPath, cliArguments.getOutputFilePath()));
    }

    @Test(expected = ParseException.class)
    public void sinceDate_unsupportedFormats_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addSinceDate("01 July 17").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void untilDate_unsupportedFormats_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addUntilDate("11/31/2017").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void sinceDate_laterThanUntilDate_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addSinceDate("01/12/2017")
                .addUntilDate("30/11/2017")
                .build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void period_withBothSinceDateAndUntilDate_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addPeriod("18d")
                .addSinceDate("30/11/2017")
                .addUntilDate("01/12/2017")
                .build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void period_notNumeric_throwsParseExcpetion() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addPeriod("abcd").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void period_isZero_throwsParseExcpetion() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addPeriod("0w").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void formats_notInAlphanumeric_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addFormats(".java").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void parse_mutuallyExclusiveArgumentsConfigAndReposTogether_throwsParseException() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addRepos(TEST_REPO_REPOSENSE)
                .build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void parse_extraArgumentForIgnore_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addIgnoreStandaloneConfig().add("true").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test
    public void parse_withTimezone_success() throws Exception {
        String zoneId = "UTC+11";
        String input = DEFAULT_INPUT_BUILDER.addTimezone(zoneId).build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(ZoneId.of(zoneId), cliArguments.getZoneId());

        zoneId = "UTC-1030";
        input = DEFAULT_INPUT_BUILDER.addTimezone(zoneId).build();
        cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(ZoneId.of(zoneId), cliArguments.getZoneId());

        zoneId = "UTC";
        input = DEFAULT_INPUT_BUILDER.addTimezone(zoneId).build();
        cliArguments = ArgsParser.parse(translateCommandline(input));

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(ZoneId.of(zoneId), cliArguments.getZoneId());
    }

    @Test(expected = ParseException.class)
    public void parse_incorrectTimezone_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addTimezone("UTC+").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test(expected = ParseException.class)
    public void parse_timezoneWithoutArgument_throwsParseException() throws Exception {
        String input = DEFAULT_INPUT_BUILDER.addTimezone("").build();
        ArgsParser.parse(translateCommandline(input));
    }

    @Test
    public void parse_withDatesAndTimezone_success() throws Exception {
        String input = DEFAULT_INPUT_BUILDER
                .addTimezone("UTC+11")
                .addSinceDate("01/07/2017")
                .addUntilDate("30/11/2017")
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        int[] expectedSinceTime = {21, 0, 0};
        Date expectedSinceDate = TestUtil.getDate(2017, Calendar.JUNE, 30, expectedSinceTime);
        int[] expectedUntilTime = {20, 59, 59};
        Date expectedUntilDate = TestUtil.getDate(2017, Calendar.NOVEMBER, 30, expectedUntilTime);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = DEFAULT_INPUT_BUILDER
                .addTimezone("UTC-0930")
                .addSinceDate("01/07/2017")
                .addUntilDate("30/11/2017")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceTime = new int[]{17, 30, 0};
        expectedSinceDate = TestUtil.getDate(2017, Calendar.JULY, 1, expectedSinceTime);
        expectedUntilTime = new int[]{17, 29, 59};
        expectedUntilDate = TestUtil.getDate(2017, Calendar.DECEMBER, 1, expectedUntilTime);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = DEFAULT_INPUT_BUILDER
                .addTimezone("UTC")
                .addSinceDate("01/07/2017")
                .addUntilDate("30/11/2017")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceTime = new int[]{8, 0, 0};
        expectedSinceDate = TestUtil.getDate(2017, Calendar.JULY, 1, expectedSinceTime);
        expectedUntilTime = new int[]{7, 59, 59};
        expectedUntilDate = TestUtil.getDate(2017, Calendar.DECEMBER, 1, expectedUntilTime);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void parse_reportConfigFolderOnly_success() throws Exception {
        String input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_ONE)
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Date expectedSinceDate = TestUtil.getSinceDate(2019, Calendar.MAY, 20);
        Date expectedUntilDate = TestUtil.getUntilDate(2020, Calendar.NOVEMBER, 20);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_TWO)
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2019, Calendar.MAY, 20);
        expectedUntilDate = TestUtil.getUntilDate(2019, Calendar.JUNE, 9);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_THREE)
                .addTimezone(DEFAULT_TIMEZONE)
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2020, Calendar.OCTOBER, 20);
        expectedUntilDate = TestUtil.getUntilDate(2020, Calendar.NOVEMBER, 20);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void parse_reportConfigAndCliNonConflict_success() throws Exception {
        String input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_TWO)
                .addTimezone(DEFAULT_TIMEZONE)
                .addPeriod("5d")
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Date expectedSinceDate = TestUtil.getSinceDate(2019, Calendar.MAY, 20);
        Date expectedUntilDate = TestUtil.getUntilDate(2019, Calendar.MAY, 24);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_THREE)
                .addTimezone(DEFAULT_TIMEZONE)
                .addSinceDate("30/07/2018")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2018, Calendar.JULY, 30);
        expectedUntilDate = TestUtil.getUntilDate(2020, Calendar.NOVEMBER, 20);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void parse_reportConfigAndCliConflict_ignoreReportConfig() throws Exception {
        String input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_ONE)
                .addTimezone(DEFAULT_TIMEZONE)
                .addSinceDate("16/06/2018")
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Date expectedSinceDate = TestUtil.getSinceDate(2018, Calendar.JUNE, 16);
        Date expectedUntilDate = TestUtil.getUntilDate(2020, Calendar.NOVEMBER, 20);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_TWO)
                .addTimezone(DEFAULT_TIMEZONE)
                .addPeriod("2w")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2019, Calendar.MAY, 20);
        expectedUntilDate = TestUtil.getUntilDate(2019, Calendar.JUNE, 2);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_THREE)
                .addTimezone(DEFAULT_TIMEZONE)
                .addSinceDate("08/05/2018")
                .addUntilDate("07/08/2018")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2018, Calendar.MAY, 8);
        expectedUntilDate = TestUtil.getUntilDate(2018, Calendar.AUGUST, 7);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void parse_reportConfigAndCliHaveSinceUntilDateAndPeriod_ignoreReportConfig() throws Exception {
        String input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_ONE)
                .addTimezone(DEFAULT_TIMEZONE)
                .addPeriod("2w")
                .addUntilDate("12/01/2019")
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Date expectedSinceDate = TestUtil.getSinceDate(2018, Calendar.DECEMBER, 30);
        Date expectedUntilDate = TestUtil.getUntilDate(2019, Calendar.JANUARY, 12);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_TWO)
                .addTimezone(DEFAULT_TIMEZONE)
                .addUntilDate("23/04/2019")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2019, Calendar.MARCH, 23);
        expectedUntilDate = TestUtil.getUntilDate(2019, Calendar.APRIL, 23);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());

        input = new InputBuilder()
                .addConfig(REPORT_CONFIG_FOLDER_ABSOLUTE_THREE)
                .addTimezone(DEFAULT_TIMEZONE)
                .addPeriod("5d")
                .addUntilDate("16/02/2019")
                .build();
        cliArguments = ArgsParser.parse(translateCommandline(input));
        expectedSinceDate = TestUtil.getSinceDate(2019, Calendar.FEBRUARY, 12);
        expectedUntilDate = TestUtil.getUntilDate(2019, Calendar.FEBRUARY, 16);

        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(expectedSinceDate, cliArguments.getSinceDate());
        Assert.assertEquals(expectedUntilDate, cliArguments.getUntilDate());
    }

    @Test
    public void parse_shallowCloning_success() throws Exception {
        String input = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addOutput(OUTPUT_DIRECTORY_ABSOLUTE)
                .build();
        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));
        Assert.assertTrue(cliArguments instanceof ConfigCliArguments);
        Assert.assertEquals(false, cliArguments.isShallowCloningPerformed());

        String inputShallow = new InputBuilder().addConfig(CONFIG_FOLDER_ABSOLUTE)
                .addOutput(OUTPUT_DIRECTORY_ABSOLUTE)
                .addShallowCloning()
                .build();
        CliArguments cliArgumentsShallow = ArgsParser.parse(translateCommandline(inputShallow));
        Assert.assertTrue(cliArgumentsShallow instanceof ConfigCliArguments);
        Assert.assertEquals(true, cliArgumentsShallow.isShallowCloningPerformed());
    }

    /**
     * Ensures that {@code actualSinceDate} is exactly one month before {@code untilDate}.
     * @throws AssertionError if {@code actualSinceDate} is not one month before {@code untilDate}.
     */
    private void assertDateDiffOneMonth(Date actualSinceDate, Date untilDate, ZoneId zoneId) {
        int zoneRawOffset = TimeUtil.getZoneRawOffset(zoneId);
        int systemRawOffset = TimeUtil.getZoneRawOffset(ZoneId.systemDefault());

        Calendar cal = new Calendar
                .Builder()
                .setInstant(untilDate.getTime())
                .build();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MILLISECOND, systemRawOffset - zoneRawOffset);
        cal.add(Calendar.MONTH, -1);
        Assert.assertTrue(actualSinceDate.equals(cal.getTime()));
    }

    /**
     * Ensures that {@code actualUntilDate} falls on the date of report generation with time at 23:59:59 for the given
     * {@code zoneId} time zone.
     * @throws AssertionError if {@code actualUntilDate} does not fall on the date of report generation
     * with time at 23:59:59.
     */
    private void assertDateDiffEndOfDay(Date actualUntilDate, ZoneId zoneId) {
        int zoneRawOffset = TimeUtil.getZoneRawOffset(zoneId);
        int systemRawOffset = TimeUtil.getZoneRawOffset(ZoneId.systemDefault());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MILLISECOND, systemRawOffset - zoneRawOffset);
        Assert.assertTrue(actualUntilDate.equals(cal.getTime()));
    }
}
