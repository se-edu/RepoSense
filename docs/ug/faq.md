{% set title = "Appendix: FAQ" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

<h1 class="display-4"><md>{{ title }}</md></h1>

<!-- ------------------------------------------------------------------------------------------------------ -->

### Q: Does RepoSense work on private repositories?
**A:** *RepoSense* will first clone the git repository to be analyzed; thus, if you do not have access to the repository, we cannot run the analysis.<br>
To enable *RepoSense* to work on private repositories, ensure that you have enabled access to your private repository in your git terminal first before running the analysis.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Q: How do formats work?
**A:** **Formats** are the [file extensions](https://techterms.com/definition/fileextension), which is the **suffix** at the end of a filename that indicates what type of file it is.<br>
The formats/file extensions to be analyzed by *RepoSense* can be specified through the [standalone config file](#provide-data-using-a-json-config-file), [repo-config file](#repo-configcsv), and [command line](#customize-using-command-line-parameters).

<!-- ------------------------------------------------------------------------------------------------------ -->

### Q: How does ignore glob list work?
**A:** [Glob](https://en.wikipedia.org/wiki/Glob_(programming)) is the pattern to specify a set of filenames with [wildcard characters](https://www.computerhope.com/jargon/w/wildcard.htm). **Ignore glob list** is the list of patterns to specify all the files in the repository which should be ignored from analysis.<br>
The ignore glob list can be specified through the [standalone config file](#provide-data-using-a-json-config-file), [repo-config file](#repo-config-csv), and [author-config file](#author-config-csv).
