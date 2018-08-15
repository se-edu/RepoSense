package reposense;

import static org.apache.tools.ant.types.Commandline.translateCommandline;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import reposense.model.CliArguments;
import reposense.model.ConfigCliArguments;
import reposense.model.RepoConfiguration;
import reposense.parser.ArgsParser;
import reposense.parser.AuthorConfigCsvParser;
import reposense.parser.ParseException;
import reposense.parser.RepoConfigCsvParser;
import reposense.report.ReportGenerator;
import reposense.util.FileUtil;
import reposense.util.TestUtil;

public class Entry {
    private static final String FT_TEMP_DIR = "ft_temp";
    private static final String EXPECTED_FOLDER = "expected";
    private static final String TEST_REPORT_GENERATED_TIME = "Tue Jul 24 17:45:15 SGT 2018";

    @Before
    public void setUp() throws IOException {
        FileUtil.deleteDirectory(FT_TEMP_DIR);
    }

    @Test
    public void testNoDateRange() throws IOException, URISyntaxException, ParseException {
        generateReport();
        Path actualFiles = Paths.get(getClass().getClassLoader().getResource("noDateRange/expected").toURI());
        verifyAllJson(actualFiles, FT_TEMP_DIR);
    }

    @Test
    public void testDateRange() throws IOException, URISyntaxException, ParseException {
        generateReport(getInputWithDates("1/9/2017", "30/10/2017"));
        Path actualFiles = Paths.get(getClass().getClassLoader().getResource("dateRange/expected").toURI());
        verifyAllJson(actualFiles, FT_TEMP_DIR);
    }

    private String getInputWithDates(String sinceDate, String untilDate) {
        return String.format("-since %s -until %s", sinceDate, untilDate);
    }

    private void generateReport() throws IOException, URISyntaxException, ParseException {
        generateReport("");
    }

    private void generateReport(String inputDates) throws IOException, URISyntaxException, ParseException {
        Path configFolder = Paths.get(getClass().getClassLoader().getResource("repo-config.csv").toURI()).getParent();
        String input = String.format("-config %s ", configFolder) + inputDates;

        CliArguments cliArguments = ArgsParser.parse(translateCommandline(input));

        List<RepoConfiguration> repoConfigs =
                new RepoConfigCsvParser(((ConfigCliArguments) cliArguments).getRepoConfigFilePath()).parse();
        List<RepoConfiguration> authorConfigs =
                new AuthorConfigCsvParser(((ConfigCliArguments) cliArguments).getAuthorConfigFilePath()).parse();

        RepoConfiguration.merge(repoConfigs, authorConfigs);

        RepoConfiguration.setFormatsToRepoConfigs(repoConfigs, ArgsParser.DEFAULT_FORMATS);
        RepoConfiguration.setDatesToRepoConfigs(
                repoConfigs, cliArguments.getSinceDate(), cliArguments.getUntilDate());

        ReportGenerator.generateReposReport(repoConfigs, FT_TEMP_DIR, TEST_REPORT_GENERATED_TIME);
    }

    private void verifyAllJson(Path expectedDirectory, String actualRelative) {
        try (Stream<Path> pathStream = Files.list(expectedDirectory)) {
            for (Path filePath : pathStream.collect(Collectors.toList())) {
                if (Files.isDirectory(filePath)) {
                    verifyAllJson(filePath, actualRelative);
                }
                if (filePath.toString().endsWith(".json")) {
                    String relativeDirectory = filePath.toAbsolutePath().toString().split(EXPECTED_FOLDER)[1];
                    assertJson(filePath, relativeDirectory, actualRelative);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void assertJson(Path expectedJson, String expectedPosition, String actualRelative) {
        Path actualJson = Paths.get(actualRelative, expectedPosition);
        Assert.assertTrue(Files.exists(actualJson));
        try {
            Assert.assertTrue(TestUtil.compareFileContents(expectedJson, actualJson));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
