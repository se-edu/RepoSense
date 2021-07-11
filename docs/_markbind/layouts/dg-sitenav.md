{% set dg_sitenav_items = [
  {name: "Contributing", link: "dg/index.html"},
  {name: "Setting up", link: "dg/settingUp.html"},
  {name: "Learning the basics", link: "dg/learningBasics.html"},
  {name: "Workflow", link: "dg/workflow.html"},
  {name: "Design and implementation"},
  {level: 2, name: "Architecture", link: "dg/architecture.html"},
  {level: 2, name: "HTML report", link: "dg/report.html"},
  {name: "Project management", link: "dg/projectManagement.html"},
  {name: "DevOps guide", link: "dg/devOpsGuide.html"},
  {name: "Appendices"},
  {level: 2, name: "Style guides", link: "dg/styleGuides.html"}
]
%}
{% from "scripts/macros.njk" import show_sitenav_items with context %}
<site-nav>
{{ show_sitenav_items(dg_sitenav_items) }}
</site-nav>
