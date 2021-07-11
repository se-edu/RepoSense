{% set title = "Showcase" %}
<frontmatter>
  title: "{{ title | safe }}"
</frontmatter>

<h1 class="display-3"><md>{{ title }}</md></h1>


### Case 1: Monitoring student programmers (**individual** projects)

* **Scenario:** RepoSense is used to monitor a Software Engineering course in which students build a project over 8 weeks.

* **Links:** [report](https://nus-cs2103-ay2021s1.github.io/ip-dashboard/#sort=groupTitle&groupSelect=groupByAuthors&search=&sortWithin=title&since=2020-08-14&until=2020-09-27&timeframe=commit&mergegroup=&breakdown=false) | [repo containing the settings](https://github.com/nus-cs2103-AY2021S1/ip-dashboard)

* **Example usages:**
  * To compare students based on the amount of code written, we can sort by contribution, as done in [this view](https://nus-cs2103-ay2021s1.github.io/ip-dashboard/#sort=totalCommits%20dsc&groupSelect=groupByAuthors&search=&sortWithin=title&since=2020-08-14&until=2020-09-27&timeframe=day&mergegroup=&breakdown=false).
  * [This view](https://nus-cs2103-ay2021s1.github.io/ip-dashboard/#sort=groupTitle&groupSelect=groupByAuthors&search=&sortWithin=title&since=2020-08-14&until=2020-09-27&timeframe=commit&mergegroup=&breakdown=false&tabOpen=true&tabType=authorship&tabAuthor=sc-arecrow&tabRepo=sc-arecrow%2Fip%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=java~md~fxml~sh~bat~gradle~txt) shows us code written by a specific student.

<!-- ------------------------------------------------------------------------------------------------------ -->

### Case 2: Monitoring student programmers (**team** projects)

* **Scenario:** Similar to case 1 above, but this time students are doing team projects.

* **Links:** [report](https://nus-cs2103-ay2021s1.github.io/tp-dashboard/#breakdown=true&search=&sort=groupTitle&sortWithin=title&since=2020-08-14&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other) | [settings](https://github.com/nus-cs2103-AY2021S1/tp-dashboard)

* **Example usages:**
  * To find the breakdown of the work done, we can tick the `breakdown by file type` checkbox, as shown in [this view](https://nus-cs2103-ay2021s1.github.io/tp-dashboard/#breakdown=true&search=&sort=groupTitle&sortWithin=title&since=2020-08-14&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=test-code~other~functional-code~docs). After that, we can filter out certain file types by un-ticking the file type.
  * To find how teams compare in terms of total work done, we can tick the `merge group` check-box and sort groups by `Contribution`, as seen in [this view](https://nus-cs2103-ay2021s1.github.io/tp-dashboard/#breakdown=false&search=&sort=totalCommits%20dsc&sortWithin=title&since=2020-08-14&timeframe=week&mergegroup=AY2021S1-CS2103-F09-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F09-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F09-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F09-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F10-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F10-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F10-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103-F10-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T14-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T14-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T14-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T14-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T16-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T16-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T16-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103-T16-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103-W14-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103-W14-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103-W14-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103-W14-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F11-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F11-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F11-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F11-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F12-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F12-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F12-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F12-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F13-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F13-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F13-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-F13-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T09-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T09-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T09-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T09-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T10-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T10-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T10-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T10-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T11-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T11-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T11-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T11-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T12-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T12-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T12-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T12-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T13-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T13-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T13-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T13-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T15-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T15-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T15-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T15-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T17-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T17-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T17-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-T17-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W10-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W10-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W10-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W10-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W11-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W11-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W11-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W11-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W12-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W12-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W12-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W12-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W13-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W13-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W13-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W13-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W15-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W15-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W15-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W15-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W16-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W16-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W16-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W16-4%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W17-1%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W17-2%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W17-3%2Ftp%5Bmaster%5D~AY2021S1-CS2103T-W17-4%2Ftp%5Bmaster%5D&groupSelect=groupByRepos). Also, note how <tooltip content="i.e., each ramp represents the work done by the entire team in the whole week">the `granularity` of the ramps is set to `Week`</tooltip> to reduce clutter.
  * [This view](https://nus-cs2113-ay1920s2.github.io/tp-dashboard/#search=&sort=groupTitle&sortWithin=title&since=2020-04-06&timeframe=commit&mergegroup=false&groupSelect=groupByRepos&breakdown=true) shows the activities near the submission deadline (note how some have overshot the deadline and some others show a frenzy of activities very near to the deadline).


<!-- ------------------------------------------------------------------------------------------------------ -->

### Case 3: Monitoring student programmers (**multiple** external projects)

* **Scenario:** Similar to cases 1 and 2 above, but this time, each student works on multiple projects. Furthermore, most projects are external OSS projects, not within the control of the teacher.

* **Links:** [report](https://nus-cs3281.github.io/2020-dashboard/#search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=false&groupSelect=groupByAuthors&breakdown=false&since=2019-12-01) | [settings](https://github.com/nus-cs2113-AY1920S2/tp-dashboard)

* **Example usages:**
  * [This view](https://nus-cs3281.github.io/2020-dashboard/#search=&sort=groupTitle&sortWithin=title&since=2019-12-01&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=false&tabOpen=true&tabType=zoom&zA=anubh-v&zR=CATcher-org%2FCATcher%5Bmaster%5D&zACS=118.09969788519638&zS=2019-12-01&zFS=&zU=2020-06-05&zMG=false&zFTF=commit&zFGS=groupByRepos) shows the commit messages written by a specific student.
  * Note how we can use the `group by` drop-down to organize activities around projects or individual authors.
  * Similarly, we can use the `merge all groups` check-box to see the sum of activities in a specific project or by a specific student.
