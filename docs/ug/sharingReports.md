<frontmatter>
  title: "Sharing reports"
  pageNav: 3
</frontmatter>

{% from 'scripts/macros.njk' import embed with context %}

<h1 class="display-4">Sharing reports</h1>

<div class="lead">

**Often, you would want to share the RepoSense report with others.** For example, a teacher using RepoSense for a programming class might want to share the report privately with tutors or publish it so that everyone can see it.
</div>

The sections below explains various ways of sharing a RepoSense report.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Share privately

To share a RepoSense report privately, simply find a way to share the folder containing the report (by default, it will be in a folder named `reposense-report`). For example, you can zip that folder and share it with the intended recipients.

You can point the recipients to the [_**Using reports**_](usingReports.html) section for guidance on how to view reports.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Publish on the web

As RepoSense reports are in a web page format, you can publish a report by simply uploading it onto any web hosting service. Given below are several options that not only allows publishing reports, but **also allows various levels of automating** the whole process (example: automatically update the report daily).

{{ embed("Appendix: **Using RepoSense with GitHub Actions**", "withGithubActions.md") }}

{{ embed("Appendix: **Using RepoSense with Travis**", "withTravis.md") }}

{{ embed("Appendix: **Using RepoSense with Netlify**", "withNetlify.md") }}

