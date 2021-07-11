{% set title = "Project management" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

<h1 class="display-4"><md>{{ title }}</md></h1>

<div class="lead">

This page contains information about project management tasks. The target audience is senior developers (and above).
</div>

<!-- ==================================================================================================== -->


## Merging PRs

* **Use the 'squash and merge' option** unless the situation warrants a different option.
* **For the merge commit**, follow conventions at [_GitHub conventions_ @SE-EDU](https://se-education.org/guides/conventions/github.html).

<!-- ==================================================================================================== -->

## Making a release on Github

Before making a release, please check the following prerequisites:

* Ensure that you have **JDK `1.8.0`** installed (==Not other major release versions such as **JDK `9`** or **JDK `11`**==).
* Ensure that the `JAVA_HOME` environment variable is correctly set to your JDK installation directory. You can refer to the [JDK Installation Guide](https://docs.oracle.com/cd/E19182-01/821-0917/inst_jdk_javahome_t/index.html).

To make a release for RepoSense on Github, please follow the `Creating a release` section in the [Github Docs](https://docs.github.com/en/github/administering-a-repository/managing-releases-in-a-repository).<br>

Take note of the following when making the release according to the above guide:
* When entering a release version number, use semantic versioning with some small tweaks:
  * Use `MAJOR.MINOR` as the version number when the release includes new features and/or major changes.
  * Use `MAJOR.MINOR.PATCH` as the version number when the release only includes bug fixes and/or minor changes.
  * Append `rc` to the version number to indicate that the release is a pre-release that is not ready to be used in production.
* Enter the release title as `RepoSense vxxx` where `xxx` is the version number. Enter the release description by referring to the previous [RepoSense releases](https://github.com/reposense/RepoSense/releases).
* Before launching the release, generate the `RepoSense.jar` file and attach it to the release.
  * Change the directory to the project root directory.
  * In the terminal, run `gradlew --version` to check that the JDK version is 1.8.0.
  * Run `gradlew shadowJar`, and the Jar file will be generated at `{buildDir}/jar/`.
  * Check that the Jar file is working. You may need to check that the report can be generated from the Jar file both locally and remotely by following the [Generating Reports Guide](../ug/generatingReports.html).

After making the release, please also remember to deploy the production website using the [deploy guide](#deploying-the-production-website).

<!-- ==================================================================================================== -->

## Deploying the production website

We have two versions of the website:

1. **Production website** at https://reposense.org
   * matches the latest released version
   * deployed manually after each new release
1. **Dev website** at https://reposense.org/RepoSense
   * matches the latest `master` branch
   * deployed automatically by Travis whenever the `master` branch is updated

The production website differs from the dev website in some ways, e.g.,

* It has a `CNAME` file (to indicate that it is the target destination for the `reposense.org` domain name)
* Its DG pages show a warning that it is not the latest version of the DG.

MarkBind mainly manages these variations via the `site.config` file. That is why the `site.config` file in the `release` branch is slightly different from the one in the `master` branch.

After each release, do the following steps to deploy the production website:
1. Switch to the `release` branch
1. `cd docs`
1. `markbind build`
1. `markbind deploy`
1. After a few minutes, check https://reposense.org to ensure it has been updated as intended.
