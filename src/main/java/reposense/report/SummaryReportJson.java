package reposense.report;

import java.util.Date;
import java.util.List;
import java.util.Map;

import reposense.model.RepoConfiguration;

/**
 * Represents the structure of summary.json file in reposense-report folder.
 */
public class SummaryReportJson {
    private final String repoSenseVersion;
    private final String reportGeneratedTime;
    private final List<RepoConfiguration> repos;
    private final List<Map<String, String>> errorList;
    private final Date sinceDate;
    private final Date untilDate;

    public SummaryReportJson(List<RepoConfiguration> repos, String reportGeneratedTime, Date sinceDate, Date untilDate,
            String repoSenseVersion, List<Map<String, String>> errorList) {
        this.repos = repos;
        this.reportGeneratedTime = reportGeneratedTime;
        this.sinceDate = sinceDate;
        this.untilDate = untilDate;
        this.repoSenseVersion = repoSenseVersion;
        this.errorList = errorList;
    }
}
