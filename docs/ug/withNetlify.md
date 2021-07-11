{% set title = "Appendix: RepoSense with Netlify" %}
<frontmatter>
  title: "{{ title | safe }}"
  pageNav: 3
</frontmatter>

{% from 'scripts/macros.njk' import embed, step with context %}

<h1 class="display-4"><md>{{ title }}</md></h1>

<div id="section-setting-up">

<box type="warning" seamless>

Note that Netlify has a low limit for free tier users (only 300 _build minutes_ per month as at June 2020 -- a single report generation can take 2-3 build minutes, longer if your report includes many/big repositories).
</box>

<!-- ==================================================================================================== -->

## Setting up

{{ step(1) }} **Fork the _publish-RepoSense_ repository** using this [link](https://github.com/RepoSense/publish-RepoSense/fork). Optionally, you can rename the fork to match your RepoSense report e.g., `project-code-dashboard`.

{{ step(2) }} **Set up Netlify for your fork** as described in this [guide](https://www.netlify.com/blog/2016/09/29/a-step-by-step-guide-deploying-on-netlify/).<br>
   ==You will need to use the following in `Step 5: Configure Your Settings` of that guide==:
   * build command: `pip install requests && ./run.sh`<br>
   * publish directory: `./reposense-report`

   After Netlify finishes building the site, you should be able to see a dummy report at the URL of your Netlify site.

{{ step(3)}} **Generate the report you want** by updating the settings in your fork.

   1. Go to the `run.sh` file of your fork (on GitHub).
   1. Update the last line (i.e., the command for running RepoSense) to match the report you want to generate:<br>
      `java -jar RepoSense.jar --repos FULL_REPO_URL` (assuming you want to generate a default report for just one repo)<br>
     e.g., `java -jar RepoSense.jar --repos https://github.com/reposense/RepoSense.git` (==note the .git at the end of the repo URL==)
   1. Commit the file. This will trigger Netlify to rebuild the report.
   1. Go to the URL of your Netlify site to see the updated RepoSense report (it might take about 2-5 minutes for Netlify to generate the report).
</div>

<!-- ==================================================================================================== -->

<div id="section-pr-previews">

## PR previews

After setting up Netlify for your repo containing RepoSense settings, when a PR comes in to that repo to update any setting, you can scroll down the PR page and in `All checks have passed`, click on the `Details` beside `deploy/netlify — Deploy preview ready!` to see a preview of the report as per the changes in the PR.
![Netlify Preview](../images/publishingguide-netlifypreview.png "Netlify Preview")
</div>

<!-- ==================================================================================================== -->

## Updating the report

**Manual:** Netlify UI has a way for you to trigger a build, using which you can cause the report to be updated.

**Automated:** Netlify's can be set up to update the report whenever a target repo of your report is updated, provided you are able to update the target repos in a certain way.

1. Click on **Settings** in the top, choose **Build & deploy** from the left panel and scroll to **Build hooks**.
   ![Build hooks](../images/using-netlify-build-hooks.png)
1. Click **Add build hook**, give your webhook a name, and choose the `master` branch to build. A Netlify URL will be generated.
1. Go to your target repository (the repository you want to analyze) and click on **Settings**.
1. Select **Webhooks** on left panel and click on **Add webhook**.
   ![Add webhook](../images/using-netlify-add-hook.png)
1. Copy the Netlify URL and paste it in the URL form field.
   ![Webhook url](../images/using-netlify-url.png)

   <box type="info" seamless>

   Note: Although the build url is not that secretive, it should be kept safe to prevent any misuse.
   </box>
1. Select **application.json** as content type.
1. Select **Let me select individual events** and based on your requirements check the checkboxes.
1. Leave the **Active** checkbox checked.
1. Click on **Add webhook** to save the webhook and add it.
