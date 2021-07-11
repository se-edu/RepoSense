{% set title = "Home" %}
<frontmatter>
  title: "{{ title | safe }}"
</frontmatter>

{% from 'scripts/macros.njk' import show_sitenav_items, thumbnail with context %}

<header>
<div class="jumbotron jumbotron-fluid text-center" style="padding-top: inherit; padding-bottom: inherit">
  <div class="container">
  <h1 class="display-3">RepoSense</h1>
  <div class="lead">

Visualize programmer activities across git repositories...
<br><br>
<img src="images/reposenseOverview.png" width="909" alt="RepoSense overview"/>
<br><br>
  </div>
  </div>
</div>
</header>

<span id="overview">

**RepoSense can generate interactive visualizations of programmer activities, even across multiple repositories.** It's ideal for educators and managers to get insights into the programming activities of their mentees. The visualizations can be **easily shared** with others (e.g., as an online dashboard), and updating the visualizations periodically **can be automated**.

{% macro heading(icon, text) %}<h4>{{ thumbnail(icon) }} <span class="lead font-weight-bold text-green">{{ text }}</span></h4>{% endmacro %}

Some example insights RepoSense can provide:


{{ heading(":fas-code:", "Insights about the code") }}

* Which part of the code was written by Tom? How many lines? How many files?
* Which test cases were written by Kim?
* Which commit messages were written by Serene?

{{ heading(":fas-chart-pie:", "Insights about the type of work") }}

* Which portion of Jacob's code is documentation?
* Who hasn't written any test code yet?
* Which project did Jolene contribute to in the last month?

{{ heading(":fas-business-time:", "Insights about the timing of work") }}

* Who is putting in the consistent effort?
* Who waits till the deadline to do the work?
* Who hasn't started any work yet?

{{ heading(":fas-list-ol:", "Insights based on comparisons") }}

* Which programmers/teams are falling behind?
* How does everyone compare in their front-end coding work over the past two weeks?
* Who are the the top 10 code contributors?

</span>

<div class="jumbotron jumbotron-fluid pt-2">
<div class="container">

<div class="container pt-2">
  <div class="row">
  </div>
  <div class="row">
  <div class="col-sm">

[**SHOWCASE** of use cases](showcase.html)

[**ABOUT** us](about.html)

[**CONTACT** us](contact.html)

[**:fab-github: GitHub**](https://github.com/reposense/RepoSense)

----
<small>

This website was built using MarkBind.

[<img src="https://markbind.org/images/logo-lightbackground.png" width="150">](http://markbind.org)

Deploy previews are powered by Netlify and Surge.

[<img src="https://www.netlify.com/img/global/badges/netlify-color-bg.svg"/>](https://www.netlify.com)
[<img width="55px" src="https://surge.sh/images/logos/svg/surge-logo.svg">](https://surge.sh)
</small>

  </div>
  <div class="col-sm">

****USER GUIDE****

{% from "_markbind/layouts/ug-sitenav.md" import ug_sitenav_items %}
{{ show_sitenav_items(ug_sitenav_items, is_flat=true) }}


  </div>
  <div class="col-sm">


****DEVELOPER GUIDE****

{% from "_markbind/layouts/dg-sitenav.md" import dg_sitenav_items %}
{{ show_sitenav_items(dg_sitenav_items, is_flat=true) }}


  </div>
  </div>
</div>
</div>
</div>

