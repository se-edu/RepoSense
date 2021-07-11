{% set title = "HTML report" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

<h1 class="display-4"><md>{{ title }}</md></h1>

The report's source files are located in [`frontend/src`](https://github.com/reposense/RepoSense/blob/master/frontend/src) and are built by [spuild](https://github.com/ongspxm/spuild2) before being packaged into the JAR file to be extracted as part of the report.

The main HTML file is generated from [`frontend/src/index.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/index.pug).

[Vue](https://vuejs.org/v2/api/) (pronounced /vjuː/, like view) is a progressive framework for building user interfaces. It is heavily utilized in the report to update the information in the various views dynamically. (Style guide available [here](https://vuejs.org/v2/style-guide/), Developer tool available [here](https://chrome.google.com/webstore/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)). Vue lifecycle hooks are the defined methods that get executed in a particular stage of the Vue object lifespan. The following is the Vue lifecycle diagram taken from [here](https://vuejs.org/v2/guide/instance.html#Lifecycle-Diagram) indicating the hook sequence:
![vue lifecycle diagram](../images/vue-lifecycle-diagram.png)

The following is a snapshot of the report:
![report screenshot](../images/report-summary.png)

<!-- ==================================================================================================== -->

## Report architecture

![report architecture](../images/report-architecture.png)

The main Vue object (`window.app`) is responsible for loading the report (through `summary.json`). Its `repos` attribute is tied to the global `window.REPOS`, and is passed into the various other modules when the information is needed.

`window.app` is broken down into two main parts
- the summary view
- and the tabbed interface

The summary view acts as the main report, which shows the various calculations. </br>
The tabbed interface is responsible for loading various modules such as authorship and zoom to display additional information.

<!-- ==================================================================================================== -->

## Javascript files

- [**main.js**](#main-main-js) - main controller that pushes content into different modules
- [**api.js**](#data-loader-api-js) - loading and parsing of the report content
- [**v_summary.js**](#summary-view-v-summary-js) - module that supports the summary view
- [**v_authorship.js**](#authorship-view-v-authorship-js) - module that supports the authorship tab view
- [**v_zoom.js**](#zoom-view-v-zoom-js) - module that supports the zoom tab view
- [**v_ramp.js**](#ramp-view-v-ramp-js) - module that supports the ramp chart view
- [**v_segment.js**](#segment-view-v-segment-js) - module that supports the code segment view

<!-- ==================================================================================================== -->

## JSON report files

- **summary.json** - a list of all the repositories and their respective details
- **projName/commits.json** - contains information of the users' commits information (e.g., line deletion, insertion, etc.), grouped by date
- **projName/authorship.json** - contains information from git blame, detailing the author of each line for all the processed files

<!-- ==================================================================================================== -->

## Main ([main.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/main.js))

This contains the logic for the main VueJS object, `window.app`, which is responsible for passing the necessary data into the relevant modules to be loaded.

`v_summary`, `v_authorship`, `v_zoom`, `v_segment`, and `v_ramp` are components embedded into the report and will render the corresponding content based on the data passed into it from the main `window.app`.

### Loading of report information
The main Vue object depends on the `summary.json` data to determine the right `commits.json` files to load into memory. This is handled by `api.js`, which loads the relevant file information from the network files if available; otherwise, a report archive, `archive.zip`, has to be used.

Once the relevant `commit.json` files are loaded, all the repo information will be passed into `v_summary` to be loaded in the summary view as the relevant ramp charts.

### Activating additional view modules
Most activity or actions should happen within the module itself, but in the case where there is a need to spawn or alter the view of another module, an event is emitted from the first module to the main Vue object (`window.app`), which then handles the data received and passes it along to the relevant modules.

### Hash link
Other than the global main Vue object, another global variable we have is the `window.hashParams`. This object is responsible for generating the relevant permalink for a specific view of the report's summary module.

## Data loader ([api.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/api.js))
This is the module that is in charge of loading and parsing the data files generated as part of the report.

### Loading from ZIP file
Due to security design, most modern browsers (e.g., Chrome) do not allow web pages to obtain local files using the directory alone. As such, a ZIP archive of the report information will be produced alongside the report generation.

This archive will be used in place of the network files to load information into the report when the network files are unavailable.

The API module will be handling all requests for all the JSON data files. If the network file is not available, the files will be obtained from the zip archive provided.

### Retrieving and parsing information
After the JSON files are loaded from their respective sources, the data will be parsed as objects and included inside the global storage object, `window.REPOS`,  in the right format.

For the basic skeleton of `window.REPOS`, refer to the generated `summary.json` file in the report for more details.

<!-- ==================================================================================================== -->

## Summary view ([v_summary.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/v_summary.js))

The `v_summary` module is in charge of loading the ramp charts from the corresponding `commits.json`.

<puml src="../diagrams/ReportArchitectureSummary.puml"/>

### Initializing the data for the ramp charts
The summary module is activated after the information is loaded from the main Vue.JS object. At creation, the `repo` attribute is populated with the `window.REPOS` object, which contains information loaded from `summary.json`.

### Filtering users and repositories
The commits information is retrieved from the corresponding project folders for each repository. This information will be filtered and sorted before being passed into the template to be displayed as ramp charts.

<!-- ==================================================================================================== -->

## Authorship view ([v_authorship.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/v_authorship.js))

The authorship module retrieves the relevant information from the corresponding `authorship.json` file if it is not yet loaded. If it has been loaded, the data will be written into `window.REPOS` and be read from there instead.

<puml src="../diagrams/ReportArchitectureAuthorship.puml"/>

### Showing relevant information by authors
The files will be filtered, picking only files the selected author has written in. The lines are then split into chunks of "touched" and "untouched" code segments displayed in the tab view which will be popped up on the right side of the screen.

<!-- ==================================================================================================== -->

## Zoom view ([v_zoom.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/v_zoom.js))

The `v_zoom` module is in charge of filtering and displaying the commits from the ramp chart's selected sub-range.

<!-- ==================================================================================================== -->

## Ramp view ([v_ramp.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/v_ramp.js))

The `v_ramp` module is responsible for receiving the relevant information from `v_summary` and generating ramp charts that contain ramp slices.

### Padding for dates
For ramps between the date ranges, the slices will be selected and it will be pre and post padded with empty slices to align the ramp slice between the `sinceDate` and `untilDate`. The ramps will then be rendered with the slices in the right position.

<!-- ==================================================================================================== -->

## Segment view ([v_segment.js](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/v_segment.js))

The `v-segment` module is used as a component in `v_authorship`. It separates the code in terms of "touched" and "untouched" segments and only loads each "untouched" segment when it is toggled.
