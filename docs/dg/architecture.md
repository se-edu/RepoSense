{% set title = "Architecture" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

<h1 class="display-4"><md>{{ title }}</md></h1>

 ![architecture](../images/architecture.png)
*Figure 1. Overall architecture of RepoSense*

<!-- ==================================================================================================== -->

## Parser(ConfigParser)

[`Parser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser) contains three components:
 * [`ArgsParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/ArgsParser.java): Parses the user-supplied command line arguments into a `CliArguments` object.
 * [`CsvParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/CsvParser.java): Abstract generic class for CSV parsing functionality. The following three classes extend `CsvParser`.
   * [`AuthorConfigCsvParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/AuthorConfigCsvParser.java): Parses the `author-config.csv` config file into a list of `AuthorConfiguration` for each repository to analyze.
   * [`GroupConfigCsvParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/GroupConfigCsvParser.java): Parses the `group-config.csv` config file into a list of `GroupConfiguration` for each repository to analyze.
   * [`RepoConfigCsvParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/RepoConfigCsvParser.java): Parses the `repo-config.csv` config file into a list of `RepoConfiguration` for each repository to analyze.
 * [`JsonParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/JsonParser.java): Abstract generic class for JSON parsing functionality. The following class extends `JsonParser` class:
   * [`StandaloneConfigJsonParser`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/parser/StandaloneConfigJsonParser.java): Parses the `_reposense/config.json` config file into a `StandaloneConfig`.

<!-- ==================================================================================================== -->

## Git

[`Git`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git) package contains the wrapper classes for respective *git* commands.
 * [`GitBlame`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitBlame.java): Wrapper class for `git blame` functionality. Traces the revision and author last modified each line of a file.
 * [`GitBranch`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitBranch.java): Wrapper class for `git branch` functionality. Gets the name of the working branch of the target repo.
 * [`GitCheckout`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitCheckout.java): Wrapper class for `git checkout` functionality. Checks out the repository by branch name or commit hash.
 * [`GitClone`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitClone.java): Wrapper class for `git clone` functionality. Clones the repository from *GitHub* into a temporary folder in order to run the analysis.
 * [`GitDiff`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitDiff.java): Wrapper class for `git diff` functionality. Obtains the changes between commits.
 * [`GitLog`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitLog.java): Wrapper class for `git log` functionality. Obtains the commit logs and the authors' info.
 * [`GitLsTree`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitLsTree.java): Wrapper class for `git ls-tree` functionality. Ensures that the tracked files do not contain any paths with illegal characters for Windows users.
 * [`GitRevList`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitRevList.java): Wrapper class for `git rev-list` functionality. Retrieves the commit objects in reverse chronological order.
 * [`GitRevParse`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitRevParse.java): Wrapper class for `git rev-parse` functionality. Ensures that the branch of the repo is to be analyzed exists.
 * [`GitShortlog`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitShortlog.java): Wrapper class for `git shortlog` functionality. Obtains the list of authors who have contributed to the target repo.
 * [`GitUtil`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/git/GitUtil.java): Contains helper functions used by the other Git classes above.

<!-- ==================================================================================================== -->

## CommitsReporter

[`CommitsReporter`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/CommitsReporter.java) is responsible for analyzing the **commit** history and generating a [`CommitContributionSummary`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/model/CommitContributionSummary.java) for each repository. `CommitContributionSummary` contains information such as each author's daily and weekly contribution and the variance of their contribution. `CommitsReporter`
 1. uses [`CommitInfoExtractor`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/CommitInfoExtractor.java) to run the `git log` command, which generates each commit's statistics within the date range.
 1. generates a [`CommitInfo`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/model/CommitInfo.java) for each commit, which contains the `infoLine` and `statLine`.
 1. uses [`CommitInfoAnalyzer`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/CommitInfoAnalyzer.java) to extract the relevant data from `CommitInfo` into a [`CommitResult`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/model/CommitResult.java), such as the number of line insertions and deletions in the commit and the author of the commit.
 1. uses [`CommitResultAggregator`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/CommitResultAggregator.java) to aggregate all `CommitResult` into a [`CommitContributionSummary`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/commits/model/CommitContributionSummary.java).

<!-- ==================================================================================================== -->

## AuthorshipReporter

[`AuthorshipReporter`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/AuthorshipReporter.java) is responsible for analyzing the whitelisted **files**, traces the original author for each line of text/code, and generating an [`AuthorshipSummary`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/model/AuthorshipSummary.java) for each repository. `AuthorshipSummary` contains the analysis results of the whitelisted files and the number of line contributions each author made. `AuthorshipReporter`
 1. uses [`FileInfoExtractor`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/FileInfoExtractor.java) to traverse the repository to find all relevant files.
 1. generates a [`FileInfo`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/model/FileInfo.java) for each relevant file, which contains the path to the file and a list of [`LineInfo`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/model/LineInfo.java) representing each line of the file.
 1. uses [`FileInfoAnalyzer`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/FileInfoAnalyzer.java) to analyze each file, using `git blame` or annotations, and finds the `Author` for each `LineInfo`.
 1. generates a [`FileResult`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/model/FileResult.java) for each file, which consolidates the authorship results into a *Map* of each author's line contribution to the file.
 1. uses [`FileResultAggregator`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/authorship/FileResultAggregator.java) to aggregate all `FileResult` into an `AuthorshipSummary`.

<!-- ==================================================================================================== -->

## ReportGenerator(Main)

[`ReportGenerator`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/report/ReportGenerator.java)
 1. clones repositories using the `GitClone` API in a multi-threaded fashion.
    * By default, 4 threads are used for cloning; the number of threads can be specified using the CLI argument `--cloning-threads <threads>`.
 1. analyzes the repositories using the `CommitReporter` and `AuthorshipReporter` in a multi-threaded fashion.
    * First, copies the template files into the designated output directory.
    * Then, uses `CommitReporter` and `AuthorshipReporter` to produce the commit and authorship summary, respectively.
    * By default, the number of threads used for analysis is equal to the number of CPU cores available; the number of threads can be specified using the CLI argument `--analysis-threads <threads>`.
 1. generates the `JSON` files needed to generate the `HTML` report.

<!-- ==================================================================================================== -->

## System

[`System`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/system) contains the classes that interact with the Operating System and external processes.
 * [`CommandRunner`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/system/CommandRunner.java) creates processes that execute commands on the terminal. It consists of many *git* commands.
 * [`LogsManager`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/system/LogsManager.java) uses the `java.util.logging` package for logging. The `LogsManager` class is used to manage the logging levels and logging destinations. Log messages are output through: `Console` and to a `.log` file.
 * [`ReportServer`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/system/ReportServer.java) starts a server to display the report on the browser. It depends on the `net.freeutils.httpserver` package.

<!-- ==================================================================================================== -->

## Model

[`Model`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/model) holds the data structures that are commonly used by the different aspects of *RepoSense*.
 * [`Author`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/model/Author.java) stores the `GitHub ID` of an author. Any contributions or commits made by the author, using his/her `GitHub ID` or aliases, will be attributed to the same `Author` object. `AuthorshipReporter` and `CommitsReporter` use it to attribute the commit and file contributions to the respective authors.
 * [`CliArguments`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/model/CliArguments.java) stores the parsed command-line arguments supplied by the user. It contains the configuration settings such as the CSV config file to read from, the directory to output the report to, and the date range of commits to analyze. These configuration settings are passed into `RepoConfiguration`.
 * [`FileTypeManager`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/model/FileTypeManager.java) stores the file format to be analyzed and the custom groups specified by the user for any repository.
 * [`RepoConfiguration`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/model/RepoConfiguration.java) stores the configuration information from the CSV config file for a single repository: the repository's organization, name, branch, list of authors to analyse, date range to analyze commits, and files from `CliArguments`.
 This configuration information is used by:
    - `GitClone` to determine the location to clone the repository from and which branch to check out to.
    - `AuthorshipReporter` and `CommitsReporter` to determine the range of commits and files to analyze.
    - `ReportGenerator` to determine the directory to output the report.
