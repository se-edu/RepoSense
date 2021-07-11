{% set title = "Appendix: CLI syntax reference" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

<h1 class="display-4"><md>{{ title }}</md></h1>

<div class="lead">

The command `java -jar RepoSense.jar` takes several flags.
</div>

<box>

**Examples**:

An example of a command using most parameters:<br>
`java -jar RepoSense.jar --repos https://github.com/reposense/RepoSense.git --output ./report_folder --since 31/1/2017 --until 31/12/2018 --formats java adoc xml --view --ignore-standalone-config --last-modified-date --timezone UTC+08`

Same command as above but using most parameters in alias format:<br>
`java -jar RepoSense.jar -r https://github.com/reposense/RepoSense.git -o ./report_folder -s 31/1/2017 -u 31/12/2018 -f java adoc xml -v -i -l -t UTC+08`
</box>

The section below provides explanations for each of the flags.

<!-- --------------------------◘---------------------------------------------------------------------------- -->

### `--assets`, `-a`

<div id="section-config">

**`--assets ASSETS_DIRECTORY`**: Specifies where to place assets for report generation.
* Parameter: `ASSETS_DIRECTORY` The directory containing the assets files. A `favicon.ico` file can be placed here to customize the favicon of the dashboard.
* Alias: `-a`
* Example: `--assets ./assets` or `-a ./assets`

<box type="info" seamless>

* If `--assets` is not specified, RepoSense looks for assets in the `./assets` directory.
</box>
</div>

<!-- --------------------------◘---------------------------------------------------------------------------- -->

### `--config`, `-c`

<div id="section-config">

**`--config CONFIG_DIRECTORY`**: Specifies that config files located in `CONFIG_DIRECTORY` should be used to customize the report.
* Parameter: `CONFIG_DIRECTORY` The directory containing the config files. Should contain a `repo-config.csv` file. Optionally, can contain an `author-config.csv` file or/and a `group-config.csv` file or/and a `report-config.json` file.
* Alias: `-c`
* Example: `java -jar RepoSense.jar --config  ./config`

<box type="info" seamless>

* Cannot be used with `--repos`.
* If both `--repos` and `--config` are not specified, RepoSense looks for config files in the `./config` directory.
</box>
</div>

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--formats`, `-f`

**`--formats LIST_OF_FORMATS`**: Specifies which file extensions to be included in the analysis.
* Parameter: `LIST_OF_FORMATS` A space-separated list of file extensions that should be included in the analysis.<br>
  Default: all file formats
* Alias: `-f`
* Example:`--formats css fxml gradle` or `-f css fxml gradle`

<box type="info" seamless>

Binary file formats, such as `jpg`, `png`,`exe`,`zip`, `rar`, `docx`, and `pptx`, all will be labelled as the file type `binary` in the generated report.
</box>
<!-- ------------------------------------------------------------------------------------------------------ -->

### `--help`, `-h`

**`--help`**: Shows the help message.
* Alias: `-h`

<box type="info" seamless>

Cannot be used with any other flags.
</box>
<!-- ------------------------------------------------------------------------------------------------------ -->

### `--ignore-standalone-config`, `-i`

**`--ignore-standalone-config`**: Specifies that the standalone config file in the repo should be ignored.
* Default: the standalone config file is not ignored
* Alias: `-i`
* Example:`--ignore-standalone-config` or `-i`

<box type="info" seamless>

This flag overrides the `Ignore standalone config` field in the CSV config file.
</box>

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--last-modified-date`, `-l`

**`--last-modified-date`**: Specifies that the last modified date of each line of code should be added to `authorship.json`.
* Default: the last modified date of each line of code will not be added to `authorship.json`
* Alias: `-l` (lowercase L)
* Example:`--last-modified-date` or `-l`

<box type="info" seamless>

* Cannot be used with `--shallow-cloning`.
* The last modified dates will be in the same timezone specified with the `--timezone` flag.
</box>

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--output`, `-o`

**`--output OUTPUT_DIRECTORY`**: Indicates where to save the report generated.
* Parameter: `OUTPUT_DIRECTORY` The location for the generated `reposense-report` folder.<br>
  Default: current directory
* Alias: `-o`
* Example: `--output ./foo` or `-o ./foo` (the report will be in the `./foo/reposense-report` folder)

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--period`, `-p`

**`--period PERIOD`**: Specifies the period of analysis window.
* Parameter: `PERIOD` The period of analysis window, in the format `nd` (for n days) or `nw` (for n weeks). It is used to calculate end date if only start date is specified, or calculate end date if only start date is specified.
* Alias: `-p`
* Example: `--period 30d` or `--period 4w`

<box type="info" seamless>

* If both start date and end date are not specified, the date of generating the report will be taken as the end date.
* Cannot be used with both `--since` and `--until`.
</box>
<!-- ------------------------------------------------------------------------------------------------------ -->

### `--repos`, `-r`

**`--repos REPO_LOCATION`**: Specifies which repositories to analyze.
* Parameter: `REPO_LOCATION` A list of URLs or the disk location of the git repositories to analyze, separated by spaces.
* Alias: `-r`
* Examples:
  * `--repos https://github.com/reposense/RepoSense.git`
  * `--repos https://github.com/reposense/RepoSense.git c:/myRepose/foo/bar`: analyzes the two specified repos (one remote, one local) and generates one report containing details of both.

<box type="info" seamless>

Cannot be used with `--config`.
</box>
<!-- ------------------------------------------------------------------------------------------------------ -->

### `--shallow-cloning`, `-S`

**`--shallow-cloning`**: Clones repositories using Git's shallow cloning functionality, which can significantly reduce the time taken to clone large repositories. However, the flag should not be used for smaller repositories where the `.git` file is smaller than 500 MB, as it would create overhead.
* Default: RepoSense does not clone repositories using Git's shallow cloning functionality.
* Alias: `-S` (uppercase S)
* Example:`--shallow-cloning` or `-S`

<box type="info" seamless>

Cannot be used with `--last-modified-date`.
</box>

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--since`, `-s`

**`--since START_DATE`**: Specifies the start date for the period to be analyzed.
* Parameter: `START_DATE` The first day of the period to be analyzed, in the format `DD/MM/YYYY`.<br>
  Default: one month before the current date
* Alias: `-s`
* Example:`--since 21/10/2017` or `-s 21/10/2017`

<box type="info" seamless>

* If the start date is not specified, only commits made one month before the end date (if specified) or the date of generating the report, will be captured and analyzed.
* If `d1` is specified as the start date (`--since d1` or `-s d1`), then the earliest commit date of all repositories will be taken as the since date.
</box>
<!-- ------------------------------------------------------------------------------------------------------ -->

### `--timezone`, `-t`

**`--timezone ZONE_ID`**: Indicates the timezone to be used for the analysis.
* Parameter: `ZONE_ID` The timezone in the format `ZONE_ID[±hh[mm]]`.<br>
  Default: system's default timezone
* Alias: `-t`
* Example:`--timezone UTC+08` or `-t UTC-1030`

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--until`, `-u`

**`--until END_DATE`**: Specifies the end date of the analysis period.
* Parameter: `END_DATE` The last date of the period to be analyzed, in the format `DD/MM/YYYY`.<br>
  Default: current date
* Alias: `-u`
* Example:`--until 21/10/2017` or `-u 21/10/2017`

<box type="info" seamless>

Note: If the end date is not specified, the date of generating the report will be taken as the end date.
</box>

<!-- ------------------------------------------------------------------------------------------------------ -->

### `--version`, `-V`

**`--version`**: Shows the version of RepoSense.
* Alias: `-V` (upper case)

<box type="info" seamless>

Cannot be used with any other flags.
</box>
<!-- ------------------------------------------------------------------------------------------------------ -->

### `--view`, `-v`

**`--view [REPORT_FOLDER]`**: Specifies that the report should be opened in the default browser.
* Parameter: `REPORT_FOLDER` Optional. If specified, no analysis will be performed and the report specified by the argument will be opened.<br>
  Default: `./reposense-report`
* Alias: `-v`
* Example:`--view` or `-v`
