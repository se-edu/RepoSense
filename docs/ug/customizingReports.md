{% set title = "Customizing reports" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

{% from 'scripts/macros.njk' import embed, step with context %}

<h1 class="display-4"><md>{{ title }}</md></h1>

<div class="lead">

The report can be customized using several ways, as explained below.
</div>

<!-- ------------------------------------------------------------------------------------------------------ -->

### Customize using CLI flags

**The simplest approach is to provide additional flags when running RepoSense.** The various flags are given in the panel below.

  {{ embed("Appendix: **CLI syntax reference**", "cli.md") }}

<!-- ------------------------------------------------------------------------------------------------------ -->

### Customize using CSV config files

**Another, more powerful, way to customize the report is by using dedicated config files.** In this case you need to use the `--config` flag instead of the `--repo` flag when running RepoSense, as follows:

  {{ embed("Appendix: **CLI syntax reference → `config` flag**", "cli.md#section-config") }}

<box type="tip" seamless>

**Managing config files collaboratively**: If you use RepoSense to monitor a large number of programmers, it may be more practical to get the programmers to submit PRs to update the config files as necessary (<tooltip content="a coder realizes some of her code is missing from the report because she used multiple git usernames, and wants to add the additional usernames to the config file">example use case</tooltip>).

To ensure that their PRs are correct, you can use [Netlify _deploy previews_](https://www.netlify.com/blog/2016/07/20/introducing-deploy-previews-in-netlify/) to preview how the report would look like after the PR has been merged. More details are in the panels below.

  {{ embed("Appendix: **Using RepoSense with Netlify → Setting up**", "withNetlify.md#section-setting-up") }}

  {{ embed("Appendix: **Using RepoSense with Netlify → PR previews**", "withNetlify.md#section-pr-previews") }}

</box>

<!-- ------------------------------------------------------------------------------------------------------ -->

### Get target repos to provide more info
**If feasible, you can also customize the target repos to play well with RepoSense** in the following two ways:

1\. Add a _stand-alone config file_ to the repo to provide more config details to RepoSense. The format of the file is given below.
  {{ embed("Appendix: **Standalone config file format**", "configFiles.md#section-standalone") }}

2\. To have more precise control over which code segment is attributed to which author, authors can annotate their code using `@@author` tags, as explained below.
  {{ embed("Appendix: **Using `@@author` tags**", "usingAuthorTags.md") }}
