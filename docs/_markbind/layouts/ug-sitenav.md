{% set ug_sitenav_items = [
  {name: "Overview", link: "ug/index.html"},
  {name: "Generating reports", link: "ug/generatingReports.html"},
  {name: "Using reports", link: "ug/usingReports.html"},
  {name: "Customizing reports", link: "ug/customizingReports.html"},
  {name: "Sharing reports", link: "ug/sharingReports.html"},
  {name: "Appendices"},
  {level: 2, name: "CLI syntax reference", link: "ug/cli.html"},
  {level: 2, name: "Config files format", link: "ug/configFiles.html"},
  {level: 2, name: "Using `@@author` tags", link: "ug/usingAuthorTags.html"},
  {level: 2, name: "RepoSense with Netlify", link: "ug/withNetlify.html"},
  {level: 2, name: "RepoSense with GitHub Actions", link: "ug/withGithubActions.html"},
  {level: 2, name: "RepoSense with Travis", link: "ug/withTravis.html"},
  {level: 2, name: "`run.sh` format", link: "ug/runSh.html"},
  {level: 2, name: "FAQ", link: "ug/faq.html"},
  {level: 2, name: "Troubleshooting", link: "ug/troubleshooting.html"}
]
%}
{% from "scripts/macros.njk" import show_sitenav_items with context %}
<site-nav>
{{ show_sitenav_items(ug_sitenav_items) }}
</site-nav>
