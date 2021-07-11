package reposense.model;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import reposense.parser.ParseException;
import reposense.parser.PeriodArgumentType;
import reposense.parser.SinceDateArgumentType;
import reposense.system.LogsManager;
import reposense.util.TimeUtil;

/**
 * Represents configuration information from JSON config file for generated report.
 */
public class ReportConfiguration {
    private static final Logger logger = LogsManager.getLogger(ReportConfiguration.class);
    private static final String DEFAULT_TITLE = "RepoSense Report";
    private static final String MESSAGE_INVALID_DATE_PROVIDED = "Invalid date provided in report config: %s.";
    private static final String MESSAGE_IGNORING_REPORT_CONFIG = "Ignoring the report config provided.";
    private String title;
    private String sinceDate;
    private String untilDate;
    private String period;

    public String getTitle() {
        if (this.title == null) {
            return DEFAULT_TITLE;
        }

        return this.title;
    }

    public Optional<Date> getSinceDate() {
        if (SinceDateArgumentType.FIRST_COMMIT_DATE_SHORTHAND.equals(this.sinceDate)) {
            return Optional.of(SinceDateArgumentType.ARBITRARY_FIRST_COMMIT_DATE);
        }

        return this.getDate(this.sinceDate, "00:00:00");
    }

    public Optional<Date> getUntilDate() {
        return this.getDate(this.untilDate, "23:59:59");
    }

    public Optional<Integer> getPeriod() {
        if (this.period == null) {
            return Optional.empty();
        }

        try {
            return PeriodArgumentType.parse(this.period);
        } catch (ParseException pe) {
            logger.warning(pe.getMessage() + " " + MESSAGE_IGNORING_REPORT_CONFIG);
            return Optional.empty();
        }
    }

    /**
     * This function resets the values of the {@code sinceDate}, {@code untilDate} and {@code period} to null, such that
     * RepoSense ignores the configuration provided in the report config provided.
     */
    public void ignoreSinceUntilDateAndPeriod() {
        this.sinceDate = null;
        this.untilDate = null;
        this.period = null;
    }

    /**
     * This function parses the given {@code date} String into a Date object.
     */
    private Optional<Date> getDate(String date, String time) {
        if (date == null) {
            return Optional.empty();
        }

        try {
            String value = TimeUtil.extractDate(date);
            return Optional.of(TimeUtil.parseDate(value + " " + time));
        } catch (java.text.ParseException pe) {
            logger.warning(String.format(MESSAGE_INVALID_DATE_PROVIDED, date) + " " + MESSAGE_IGNORING_REPORT_CONFIG);
            return Optional.empty();
        }
    }
}
