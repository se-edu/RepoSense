{% set title = "Appendix: Troubleshooting" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

<h1 class="display-4"><md>{{ title }}</md></h1>

<!-- ------------------------------------------------------------------------------------------------------ -->

### Contributions missing in the ramp chart (but appear in the contribution bar and code panel)

This is probably a case of giving an incorrect author name alias (or GitHub ID) in your [author-config file](#author-config-csv).<br>
Please refer to [A Note About Git Author Name](#a-note-about-git-author-name) above on how to find out the correct author name you are using and how to change it.<br>
Also, ensure that you have added all author name aliases you may be using (if you are using multiple computers or have previously changed your author name).<br>
Alternatively, you may choose to configure *RepoSense* to track using your GitHub email instead of in your [standalone config file](#provide-data-using-a-json-config-file) or [author-config file](#author-config-csv), which is more accurate compared to author name aliases. The associated GitHub email you are using can be found in your [GitHub settings](https://github.com/settings/emails).


<!-- ------------------------------------------------------------------------------------------------------ -->

### Contribution bar and code panel is empty (despite a non-empty ramp chart)

The contribution bar and code panel records the lines you have authored to the **latest** commit of the repository and branch you are analyzing.  As such, it is possible that while you have lots of committed contributions, your final authorship contribution is low. This happens if you have only deleted lines or someone else has overwritten your code and taken authorship for it (currently, RepoSense does not have the functionality to track overwritten lines)..<br>
It is also possible that another user has overridden the authorship of your lines using the [@@author tags](#provide-data-using-author-tags).

<!-- ------------------------------------------------------------------------------------------------------ -->

### RepoSense is not using the standalone config file in my local repository

Ensure that you have committed the changes to your standalone config file first before running the analysis, as *RepoSense* is unable to detect uncommitted changes to your local repository.

<!-- ------------------------------------------------------------------------------------------------------ -->

### RepoSense fails on Windows (but works on Linux/Mac OS)

Possibly, you may have some file names with [special characters](https://docs.microsoft.com/en-us/windows/desktop/FileIO/naming-a-file#naming-conventions) in them, which is disallowed in Windows OS. As such, *RepoSense* is unable to clone your repository fully, thus failing the analysis.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Some file types are not shown in the file type filter even if I have included them in the file formats when generating the report

The files of these types may be [binary files](https://en.wikipedia.org/wiki/Binary_file). *RepoSense* will group binary files under one single file type `binary`. Common binary files include images (`.jpg`, `.png`), applications (`.exe`), zip files (`.zip`, `.rar`) and certain document types (`.docx`, `.pptx`).
