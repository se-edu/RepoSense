package reposense.parser;

import static reposense.util.TestUtil.loadResource;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import reposense.model.ReportConfiguration;
import reposense.util.TestUtil;

public class ReportConfigJsonParserTest {

    private static final Path VALID_TITLE_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-validTitle.json");
    private static final Path INVALID_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-invalid.json");
    private static final Path EMPTY_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-empty.json");
    private static final Path VALID_SINCE_DATE_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-sinceDate.json");
    private static final Path VALID_UNTIL_DATE_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-untilDate.json");
    private static final Path VALID_PERIOD_DAYS_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-periodDays.json");
    private static final Path VALID_PERIOD_WEEKS_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-periodWeeks.json");
    private static final Path VALID_FIRST_COMMIT_DATE_REPORT_CONFIG = loadResource(
            ReportConfigJsonParserTest.class, "ReportConfigJsonParserTest/report-config-firstCommit.json");
    private static final String DEFAULT_TITLE = "RepoSense Report";

    @Test
    public void reportConfig_parseEmptyJsonFile_getDefaultTitle() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(EMPTY_REPORT_CONFIG);
        Assert.assertEquals(reportConfig.getTitle(), DEFAULT_TITLE);
        Assert.assertEquals(reportConfig.getSinceDate(), Optional.empty());
        Assert.assertEquals(reportConfig.getUntilDate(), Optional.empty());
        Assert.assertEquals(reportConfig.getPeriod(), Optional.empty());
    }
    @Test
    public void reportConfig_parseInvalidJsonFile_getDefaultTitle() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(INVALID_REPORT_CONFIG);
        Assert.assertEquals(reportConfig.getTitle(), DEFAULT_TITLE);
        Assert.assertEquals(reportConfig.getSinceDate(), Optional.empty());
        Assert.assertEquals(reportConfig.getUntilDate(), Optional.empty());
        Assert.assertEquals(reportConfig.getPeriod(), Optional.empty());
    }

    @Test
    public void reportConfig_parseValidTitleJsonFile_getCustomTitle() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_TITLE_REPORT_CONFIG);
        Assert.assertNotEquals(reportConfig.getTitle(), DEFAULT_TITLE);
    }

    @Test
    public void reportConfig_parseValidSinceDateJsonFile_getCustomStartDate() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_SINCE_DATE_REPORT_CONFIG);
        Optional<Date> expectedDate = Optional.of(TestUtil.getLocalSinceDate(2019, Calendar.MAY, 20));
        Assert.assertEquals(expectedDate, reportConfig.getSinceDate());
    }

    @Test
    public void reportConfig_parseValidUntilDateJsonFile_getCustomEndDate() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_UNTIL_DATE_REPORT_CONFIG);
        Optional<Date> expectedDate = Optional.of(TestUtil.getLocalUntilDate(2020, Calendar.JULY, 9));
        Assert.assertEquals(expectedDate, reportConfig.getUntilDate());
    }

    @Test
    public void reportConfig_parseValidPeriodDaysJsonFile_getCustomPeriod() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_PERIOD_DAYS_REPORT_CONFIG);
        Optional<Integer> expectedPeriod = Optional.of(15);
        Assert.assertEquals(expectedPeriod, reportConfig.getPeriod());
    }

    @Test
    public void reportConfig_parseValidPeriodWeeksJsonFile_getCustomPeriod() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_PERIOD_WEEKS_REPORT_CONFIG);
        Optional<Integer> expectedPeriod = Optional.of(3 * 7);
        Assert.assertEquals(expectedPeriod, reportConfig.getPeriod());
    }

    @Test
    public void reportConfig_parseFirstCommitDateJsonFile_getFirstCommitDate() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_FIRST_COMMIT_DATE_REPORT_CONFIG);
        Optional<Date> expectedDate = Optional.of(SinceDateArgumentType.ARBITRARY_FIRST_COMMIT_DATE);
        Assert.assertEquals(expectedDate, reportConfig.getSinceDate());
    }

    @Test
    public void reportConfig_ignoreSinceDate_success() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_SINCE_DATE_REPORT_CONFIG);
        reportConfig.ignoreSinceUntilDateAndPeriod();
        Assert.assertEquals(Optional.empty(), reportConfig.getSinceDate());
    }

    @Test
    public void reportConfig_ignoreUntilDate_success() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_UNTIL_DATE_REPORT_CONFIG);
        reportConfig.ignoreSinceUntilDateAndPeriod();
        Assert.assertEquals(Optional.empty(), reportConfig.getUntilDate());
    }

    @Test
    public void reportConfig_ignorePeriod_success() throws Exception {
        ReportConfiguration reportConfig = new ReportConfigJsonParser().parse(VALID_PERIOD_DAYS_REPORT_CONFIG);
        reportConfig.ignoreSinceUntilDateAndPeriod();
        Assert.assertEquals(Optional.empty(), reportConfig.getPeriod());
    }
}
