{% set title = "Setting up" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

{% from 'scripts/macros.njk' import embed with context %}

<h1 class="display-4"><md>{{ title }}</md></h1>

**Prerequisites:**
* **JDK `1.8.0_60`** or later ([download :fas-download:](https://www.oracle.com/technetwork/java/javase/downloads/index.html)).
* **npm** `6.0` or later ([download :fas-download:](https://www.npmjs.com/get-npm)).
* **git `2.14`** or later ([download :fas-download:](https://git-scm.com/downloads)).

  <box type="info" seamless>

  Type `java -version`, `npm -v` and `git --version` respectively on your OS terminal and ensure that you have the correct version of each prerequisite installed.
  </box>

<!-- ==================================================================================================== -->

## Getting the code

1. **Fork** the [reposense/reposense](https://github.com/reposense/RepoSense) repo.
1. **Clone** the fork to your computer.

## Setting up the IDE

<box type="warning" seamless>

The recommended IDE is **Intellij IDEA**. While it is not compulsory to use it, note that we will not be able to help you troubleshoot IDE problems if you use any other IDE.
</box>

1. **Ensure you have configured IDEA for the correct JDK**, as explained in [this tutorial](https://se-education.org/guides/tutorials/intellijJdk.html).
1. **Import the project as a Gradle project**, as explained in [this tutorial](https://se-education.org/guides/tutorials/intellijImportGradleProject.html).

<!-- ==================================================================================================== -->

## Verifying the setup

<box type="info" seamless>

This project is already configured to use Gradle for build automation. If you are new to Gradle, see [this tutorial](https://se-education.org/guides/tutorials/gradle.html) to learn how to use it.
</box>

1. Open a command prompt and navigate to the project root.
1. Run `gradlew clean build` (`./gradlew clean build` if you on a Unix-like OS), and ensure that it finishes with a `BUILD SUCCESSFUL` message.
1. Run the tests using the `gradlew test systemtest` command and ensure it succeeds too.
1. You can also try running the app using code, as given in the panel below.

{{ embed('Developer guide → **Workflow → Running the app from code**', 'workflow.md#section-running-from-code', level=2) }}

## Before you start coding

... read the [Workflow](workflow.html) section.
