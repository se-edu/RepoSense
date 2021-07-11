{% set title = "Learning the Basics" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

{% from 'scripts/macros.njk' import embed, step with context %}

<h1 class="display-4"><md>{{ title }}</md></h1>

<div class="lead">

This is a learning guide for developers who are new to RepoSense.
</div>

<box type="warning" seamless>

Depending on what you know already and what you would like to work on (i.e., backend or frontend), you may find certain sections irrelevant to you and you can skip them accordingly.
</box>

<!-- ==================================================================================================== -->

## Backend

<box type="info" seamless>

This section is for developers who want to contribute to the backend of RepoSense. You may skip this section if you want to contribute as a pure frontend developer. Before you get started, you should have set up the project on your computer according to [the _Setting up_ page](settingUp.html).
</box>

The backend implementation of RepoSense is located in `src/main`.

{{ step(1) }} **Know Java**

The RepoSense backend is mostly written in `Java 8`. 

1. You need to have a basic knowledge of Java before getting started, including its syntax, [API](https://docs.oracle.com/javase/8/docs/api/), and certain frameworks such as [JUnit](https://se-education.org/learningresources/contents/java/JUnit.html).
1. Once you are familiar with the basic syntax, you may wish to learn more advanced topics such as [concurrency](https://se-education.org/learningresources/contents/java/JavaConcurrency.html), [synchronization](https://se-education.org/learningresources/contents/java/JavaSynchronization.html), and [streams](https://se-education.org/learningresources/contents/java/streams-an-introduction.html). These topics can help you to understand certain part of the backend implementation (concurrent cloning and analysis of multiple repositories, etc). They are optional but you may find them useful when working on certain issues.

{{ step(2) }} **Learn the RepoSense backend architecture**

You may want to refer to the [backend architecture](architecture.html) to understand the RepoSense backend implementation logic.

To gain a more concrete idea of how the backend works, you can use the IDE Debugger and run RepoSense under the debugging mode to trace through the steps of how arguments from command line and CSV files are parsed, how repositories are cloned and analyzed, and how the JSON files are generated.

<box type="warning" seamless>

The information below is for **Intellij**. If you are using a different IDE, you may need to check the documentation of how to use the debugger separately.
</box>

* Check the [debugging guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) if you are not familiar with debugging in **Intelij**.
* In `RepoSense.java`, the main class, set appropriate break points. Here are some relevant method calls in the `main` method at which you can set the breakpoints:
  * `ArgsParser.parse(args)`: RepoSense parses the CLI arguments from the command given by the user.
  * `getRepoConfigurations(cliArguments)`: RepoSense gets the configuration for each repository by parsing the CSV files.
  * `getReportConfigurations(cliArguments)`: RepoSense gets the report configuration (report title) by parsing the JSON files.
  * `ReportGenerator.generateReposReport(...)` This is where the bulk of the work is conducted, including cloning repositories, analyzing repositories, and generating the JSON files to be used by the report.

<box type="info" seamless>

When tracing through the program execution, you can cross reference the architecture diagram and Javadoc of the class and method to check your understanding of the procedure.
</box>

{{ step(3) }} **Gain some hands-on experience**

Here are some small tasks for you to gain some basic knowledge of the code related to the RepoSense backend. You can do each in a separate branch in your local copy of the code.

<panel header="**Task 1: Add a flag to pretty-print the JSON file**" type="primary">

  **Task 1: Add a flag to pretty-print the JSON file**

  1. [Generate a report locally](../ug/generatingReports.html#generating-reports-locally).
  2. Open a generated JSON file. For example, you can open `summary.json` of the generated report.
  3. You should see that the content in the JSON is compactly organized in 1 single line. This may affect readability for developer who wants to investigate the content in the JSON file.

  **Your Task**

  Add a new CLI argument `--use-json-pretty-printing`, such that when a user runs the command `java -jar RepoSense.jar --repos LIST_OF_REPO_URLS --view --use-json-pretty-printing`, the JSON files `summary.json`, `authorship.json`, and `commits.json` will be printed in a more readable way.

  <panel header="Hint 1">

  Try to understand the parsing process of the Command Line Arguments, which starts from `ArgsParser.parse(args)` in `RepoSense.java` and uses `ArgumentParser` to capture the arguments in the command string.

  Therefore, the first step you can take is to add the following to `ArgsParser`.

  ```
  public static final String[] JSON_PRINT_MODE_FLAGS = new String[]{"--use-json-pretty-printing", "-j"};
  ```

  In `getArgumentParser` method, add the following content to make `ArgumentParser` capture the new argument.

  ```
  parser.addArgument(JSON_PRINT_MODE_FLAGS)
      .dest(JSON_PRINT_MODE_FLAGS[0])
      .action(Arguments.storeTrue())
      .help("A flag to use json pretty printing when generating the json files.");
  ```
  </panel>

  <panel header="Hint 2">

  After the step in hint 1, the argument is captured by `ArgumentParser`. Now make corresponding changes to `CliArguments.java`, `ConfigCliArguments.java`, and the `parse` method in `ArgsParser.java` to make the return result of `parse` include the new argument. 

  1. Add the following content to `CliArguments` to include `isPrettyPrintingUsed` as a new attribute to the class.

  ```
  protected boolean isPrettyPrintingUsed;

  public boolean isPrettyPrintingUsed() {
          return isPrettyPrintingUsed;
  }
  ```

  2. In the constructor of `ConfigCliArguments`, add `isPrettyPrintingUsed` as a new parameter of the method, and add the following instruction to the method body.

  ```
  this.isPrettyPrintingUsed = isPrettyPrintingUsed;
  ```

  3. In the `parse` method of `ArgsParser`, add the following instruction to get `isJsonPrettyPrintingUsed` from `ArgmentParser`.

  ```
  boolean isJsonPrettyPrintingUsed = results.get(JSON_PRINT_MODE_FLAGS[0]);
  ``` 

  4. Additionally, change the return statement of the `parse` method so that the `ConfigCliArguments` object returned will now include `isJsonPrettyPrintingUsed`.
  </panel>

  <panel header="Hint 3">

  After the steps in hint 1 and hint 2, the result returned from `ArgsParser.parse(args)` in `RepoSense.java` should be able to capture the new argument when it is specified in the command. 

  The next step is to extract the argument from the `CliArguments` object, and pass it to JSON file writer to notify it of the specified printing mode.

  Note that the creation and writing of JSON file is invoked in `ReportGenerator.generateReposReport`, which calls `FileUtil.writeJsonFile` directly to write the `summary.json` file or indirectly (Check `generateIndividualRepoReport` and `generateEmptyRepoReport`) to write the `commits.json` or `authorship.json` files of individual repositories.

  Therefore, the task now is to make `FileUtil.writeJsonFile` switch between different printing mode.
 
  1. You can find out what [`Gson.setPrettyPrinting`](https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5/com/google/gson/GsonBuilder.html#setPrettyPrinting--) does and how it can be used in the `writeJsonFile` method of [`FileUtil.java`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/util/FileUtil.java).

  2. Add the following content to `FileUtil`.

  ```
  private static boolean isPrettyPrintingUsed = false;
  ```

  3. In the `writeJsonFile` method, Replace the creation of the `Gson` object with the following instructions.

  ```
  GsonBuilder gsonBuilder = new GsonBuilder()
          .setDateFormat(GITHUB_API_DATE_FORMAT)
          .registerTypeAdapter(FileType.class, new FileType.FileTypeSerializer());
  Gson gson;
  if (isPrettyPrintingUsed) {
      gson = gsonBuilder.setPrettyPrinting().create();
  } else {
      gson = gsonBuilder.create();
  }
  ```

  4. To notify `FileUtil` of the switch between different printing mode, add the following method to `FileUtil`.

  ```
  public static void setPrettyPrintingMode(boolean isPrettyPrintingAdopted) {
      isPrettyPrintingUsed = isPrettyPrintingAdopted;
  }
  ```

  5. It is now possible to notify `FileUtil` of the printing mode switch by extracting the argument from the `CliArguments` object in the `main` method of `RepoSense.java` and passing it to the corresponding method in `FileUtil`.

  ```
  FileUtil.setPrettyPrintingMode(cliArguments.isPrettyPrintingUsed());
  ```

  Now the parsing of argument and changing of printing mode should have been completed.
  </panel>

  <panel header="Suggested solution">

  There is more than 1 way to achieve this. By combining the changes in hint 1, hint 2, and hint 3, you should be able to get a possible solution.
  
  Try the command `java -jar RepoSense.jar --repos https://github.com/reposense/RepoSense.git --view --use-json-pretty-printing` and check the generated JSON files to see if it works.
  </panel>

</panel>

<panel header="**Task 2: Add exception message during repository cloning to the summary view**" type="primary">

  **Task 2: Add exception message during repository cloning to the summary view**

  1. Open a [report](https://dashboard-1507-pr-reposense-reposense.surge.sh/).
  2. You should see that there is a red panel in the summary view containing the following message, indicating that there is an issue in the cloning process of the repository `reposense/testrepo-Empty`.

  ```
  reposense/testrepo-Empty[master]
          Failed to clone from https://github.com/reposense/testrepo-Empty.git
  ```

  **Your task**

  For the repository with the message `Failed to clone from ...`, find out what exception causes this cloning error, and add that exception message to the panel as well.

  <panel header="Hint 1">

  You can find out what [`ErrorSummary.java`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/report/ErrorSummary.java) and [`RepoCloner.java`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/report/RepoCloner.java) do, and where they are used.
  </panel>

  <panel header="Hint 2">

  Try to understand the cloning process. 

  * The cloning process is invoked by [`RepoGenerator.java`](https://github.com/reposense/RepoSense/blob/master/src/main/java/reposense/report/ReportGenerator.java) in the `cloneAndAnalyzeRepos` method, which subsequently calls `cloneBare` in `RepoCloner.java` to start the cloning. 
  * The `cloneAndAnalyzeRepos` method will then call `getRepoLocation`  in `RepoCloner.java` to try to get the repository location. 
  * Beneath the surface, `RepoCloner` will first execute `spawnCloneProcess` and then execute `waitForCloneProcess` when it is invoked by `RepoGenerator` for the first and second time respectively.
  </panel>

  <panel header="Hint 3">

  In `RepoCloner`, the potential exceptions in `spawnCloneProcess` and `waitForCloneProcess` are caught but not recorded by `ErrorSummary`. 
  </panel>

  <panel header="Suggested solution">

  There is more than 1 way to achieve this. One solution is shown as the following:
  
  Add this to the catch block of `spawnCloneProcess` and `waitForCloneProcess`, so that the message will be captured in `summary.json`.
  
  ``` 
  ErrorSummary.getInstance().addErrorMessage(config.getDisplayName(), e.getMessage());
  ```
  </panel>

</panel>

<box type="info" seamless>

This is only for your practice. There is no need for you to commit this change and submit it in a pull request.
</box>

{{ step(4) }} **Next Step**

You can now proceed to learn the [contributing workflow](workflow.html).

<!-- ==================================================================================================== -->

## Frontend

<box type="info" seamless>

This section is for developers who want to contribute to the frontend of RepoSense. You may skip this section if you want to contribute as a pure backend developer.
</box>

The frontend implementation of RepoSense is located in `frontend/src`.

{{ step(1) }} **Learn the necessary tools**

It is necessary for you to learn the basics of Vue.js, Pug, and SCSS before working on the project. 

<!-- ------------------------------------------------------------------------------------------------------ -->

### Vue.js

<box type="info" seamless>

Vue.js uses JavaScript as its programming language. Before learning **Vue.js**, you may need to first get yourself familiar with JavaScript syntax first.
You can refer to the [Javascript documentation](https://devdocs.io/javascript/) to learn the basic syntax. There are plenty of other resources available and please feel free to find the resource most suitable for you.
</box>

RepoSense uses **Vue.js** (Vue2) in its front-end implementation. In particular, major user interface components, such as [summary view](report.html#summary-view-v-summary-js), [authorship view](report.html#authorship-view-v-authorship-js), and [zoom view](report.html#zoom-view-v-zoom-js), are implemented as Vue components. The corresponding source files are in `frontend/src/static/js`.

* If you are new to Vue.js, you may want to start learning by looking at the [the beginner tutorial](https://www.vuemastery.com/courses/intro-to-vue-js/).
* You can dive deeper later by checking the [Vue.js documentation](https://vuejs.org/v2/guide/index.html) to learn about essential concepts such as component life cycle hooks, and component properties.
* It is recommended if you can work on some small projects first to gain more solid understanding of Vue.js.

<box type="warning" seamless>

The guide above uses HTML as the component template, which is not the case with RepoSense. You may wish to learn more about [Pug](#pug) and its connection with HTML.
</box>

#### Vuex

RepoSense uses **Vuex** for the state management of the Vue components.

* You can check the [Vuex guide](https://vuex.vuejs.org/guide/#the-simplest-store) to find out how Vuex can be used in a Vue project.
* There is also a [course](https://vueschool.io/courses/vuex-for-everyone) available that will walk you through an example of creating Vue application with Vuex.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Pug

RepoSense uses Pug files as the template of each Vue component. The corresponding HTML templates will later be generated from the Pug files by [spuild](https://github.com/ongspxm/spuild2) when generating the report.

<box type="info" seamless>

Since Pug is used to generate the HTML template, it is recommended that you have a basic knowledge of HTML before starting to learn Pug. Once you understand how HTML works, you can proceed to focus on how Pug is translated into HTML.
</box>

* You can refer to the [official documentation](https://pugjs.org/api/getting-started.html) or [this tutorial](https://www.youtube.com/watch?v=kt3cEjjkCZA) to learn about the syntax of pug and how it is translated into HTML.
* To get a hands-on experience, here is a [Pug to HTML converter](https://pughtml.com/). Feel free to try out a couple of examples on your own.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Scss

SCSS is used for styling the Pug template. The corresponding CSS will later be generated from the SCSS files by [spuild](https://github.com/ongspxm/spuild2) when generating the report. The corresponding source files are in `frontend/src/static/css`.

<box type="info" seamless>

It is recommended that you have a basic knowledge of CSS before starting to learn SCSS. Once you understand how CSS works, you can proceed to focus on how SCSS is translated into CSS.
</box>

* You can refer to the [style rules](https://sass-lang.com/documentation/style-rules) to learn about the similarities and differences between SCSS and CSS.

{{ step(2) }} **Learn the RepoSense frontend architecture**

* You may want to refer to the [frontend architecture](report.html#report-architecture) to understand the implementation.
* Another way for you to understand the frontend is to use **Vue.js devtools** to learn how the various Vue Components interact with each other. You can refer to the [frontend debugging guide](workflow.html#debugging-front-end) for more information.

{{ step(3) }} **Gain some hands-on experience**

Here are some small tasks for you to gain some basic knowledge of the code related to the RepoSense frontend. You can do each in a separate branch in your local copy of the code.

<panel header="**Task 1: Highlight the selected author name in the summary view**" type="primary">

  **Task 1: Highlight the selected author name in the summary view**

  1. Open a [report](https://dashboard-1507-pr-reposense-reposense.surge.sh/).
  2. Randomly open the authorship contribution panel of an author (The icon is `</>`).
  3. You should see that, after you open the panel, the author title background on the chart panel becomes yellow, and the background of the `</>` icon you clicked becomes green.

  **Your Task**

  Make corresponding changes to `summary charts` so that in step 3, after opening the panel, the font colour of the author title (in the form of `authorDisplayName(authorName)`) on the chart panel also becomes green.

  <panel header="Hint 1">

  Try to locate where the author title is in [`summary_charts.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/summary_charts.pug).
  </panel>

  <panel header="Hint 2">

  You can check what `activeUser` and `activeRepo` do in [`v_summary_charts.js`](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/js/v_summary_charts.js).
  </panel>

  <panel header="Hint 3">

  Refer to how changes are made to the title background and icon background in [`summary_charts.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/summary_charts.pug).
  </panel>

  <panel header="Hint 4">

  Some of the CSS styling for `summart_charts.pug` is in [`v_summary.scss`](https://github.com/reposense/RepoSense/blob/master/frontend/src/static/css/v_summary.scss). You can add corresponding class selector if necessary.
  </panel>

  <panel header="Suggested solution">

  There is more than 1 way to achieve this. One solution is shown as the following:

  Add this to `v_summary.scss`.

  ```
  .active-text {
    color: mui-color('green');
  }
  ```

  In `summary_charts.pug`, locate `summary-chart__title--name`, and add the following to its `v-bind:class` attribute map.

  ```
  'active-text': user.name === activeUser && user.repoName === activeRepo
  ```
  </panel>

</panel>

<panel header="**Task 2: Add tooltip for file path in authorship panel**" type="primary">

  **Task 2: Add tooltip for file path in authorship panel**

  1. Open a [report](https://dashboard-1507-pr-reposense-reposense.surge.sh/).
  2. Randomly open the authorship contribution panel of an author (The icon is `</>`).
  3. Select a random file in the authorship contribution panel, and hover your mouse on an icon on the file title, a corresponding tooltip will show up, suggesting what the purpose of the icon is. However, when you hover the mouse over the file path on the file title, there is no tool tip shown, even if clicking the path itself will also trigger some event.

  <box type="info" seamless>

  For example, if you open the authorship contribution panel of an author in `reposense/RepoSense[master]` and hover the mouse over the triangular icon beside the file path `src/main/java/reposense/model/Author.java`, you should see a tooltip saying `Click to hide file details` above the icon. However, when you hover the mouse over the file path `src/main/java/reposense/model/Author.java`, there is no corresponding tooltip shown.
  </box>

  **Your Task**

  Make corresponding `authorship contribution panel` so that in step 3, when hovering your mouse over the file path:
  * A tip saying `This is the file path. Click to hide file details` will show up when the file details are shown
  * A tip saying `This is the file path. Click to show file details` will show up when the file details are not shown.

  <panel header="Hint 1">

  Try to locate where the file title and the file path are in [`authorship.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/tabs/authorship.pug).
  </panel>

  <panel header="Hint 2">

  You can check how tooltip is added for the triangular icon in the file title in [`authorship.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/tabs/authorship.pug).
  </panel>

  <panel header="Hint 3">

  You can check what `file.active` does and how it is used to switch between different tooltip messages when hovering the mouse on the corresponding icon.
  </panel>

  <panel header="Suggested solution">

  There is more than 1 way to achieve this. One solution is shown as the following:

  1. In `authorship.pug`, locate the section that iterates through each file in `selectedFiles`.
  2. There is a specific portion of the section that renders the toggle icon, the file index, and the file path of the file title.
  3. Try to locate the `span` tag that renders `file.path`, and wraps it inside a new `tooptip`.
  4. In the `tooltip`, use the following instructions to handle the switch of tooltip message.

  ```
  span.tooltip-text(v-show="file.active") This is the file path. Click to hide file details
  span.tooltip-text(v-show="!file.active") This is the file path. Click to show file details
  ```
  </panel>

</panel>

<panel header="**Task 3: Add tooltip for commit message title in zoom panel**" type="primary">

  **Task 3: Add tooltip for commit message title in zoom panel**

  1. Open a [report](https://dashboard-1507-pr-reposense-reposense.surge.sh/).
  2. Randomly open the commits panel of an author.
  3. Select a random commit in the commits panel, and hover your mouse on the icons on the commit title, there is no tooltip shown saying that it will redirect you to a different site.

  <box type="info" seamless>

  For example, given the [report](https://dashboard-1507-pr-reposense-reposense.surge.sh/?search=&sort=groupTitle&sortWithin=title&since=&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=false&tabOpen=true&tabType=zoom&zA=eugenepeh&zR=reposense%2FRepoSense%5Bmaster%5D&zACS=99.28792569659443&zS=2017-10-09&zFS=&zU=2021-04-04&zMG=undefined&zFTF=commit&zFGS=groupByRepos&zFR=false), if you hover the mouse over the commit title `README: add acknowledgements section (#978)`, there is no corresponding tooltip shown, but when you click on the commit title, you will be redirected to a different site to see the commit details.
  </box>

  **Your Task**

  Make corresponding changes to `zoom panel` so that in step 3, when hovering your mouse over the commit title, a tooltip saying `Click to view the detailed file changes in the commit` will show up on the commit title.

  <panel header="Hint 1">

  Try to locate where the commit title is in [`zoom.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/tabs/zoom.pug).
  </panel>

  <panel header="Hint 2">

  You can check how tooltip is added for other icons in [`zoom.pug`](https://github.com/reposense/RepoSense/blob/master/frontend/src/tabs/zoom.pug).
  </panel>

  <panel header="Hint 3">

  Check what `selectedCommits` does and how the link and commit title of each commit is retrieved.
  </panel>

  <panel header="Suggested solution">

  There is more than 1 way to achieve this. One solution is shown as the following:

  1. In `zoom.pug`, locate the section that iterates througth each `day` in `selectedCommits`.
  2. The component that helps render the commit message title should be an `a` tag which uses the `getSliceLink` method to set the link to the commit details and uses `slice.messageTitle` to show the commit message title.
  3. Wrap the `a` tag in a new `tooltip`.
  4. In the `tooltip`, add the following content to show the tooltip message.

  ```
  span.tooltip-text Click to view the detailed file changes in the commit
  ```
  </panel>

</panel>

<box type="info" seamless>

This is only for your practice. There is no need for you to commit this change and submit it in a pull request.
</box>

{{ step(4) }} **Next Step**

You can now proceed to learn the [contributing workflow](workflow.html).

<!-- ==================================================================================================== -->

## DevOps

If you want to understand and contribute to the DevOps aspect of RepoSense, you can refer to the [DevOps guide](https://github.com/reposense/RepoSense/wiki/DevOps-guide) for more information.
