{% set title %}Appendix: run.sh format{% endset %}
<frontmatter>
  title "{{ title | safe }}"
</frontmatter>

{% from 'scripts/macros.njk' import embed, step with context %}

<h1 class="display-4"><md>{{ title }}</md></h1>

<div class="lead">

[`run.sh`](https://github.com/reposense/publish-RepoSense/blob/master/run.sh) is a script used for automating RepoSense report generation.
</div>

<!-- ------------------------------------------------------------------------------------------------------ -->

### Customizing the RepoSense command

You can update the RepoSense command (i.e., the last line) in the `run.sh` to match your needs.

{{ embed("Appendix: **CLI syntax reference**", "cli.md") }}

<!-- ------------------------------------------------------------------------------------------------------ -->

### Specifying which version of RepoSense to use

Depending on which version you wish to use for report generation, add one of the following flags to the line `./get-reposense.py` in `run.sh` (e.g., `./get-reposense.py --release`):
* `--release`: Use the latest release (Stable)
* `--master`: Use the latest version of the master branch
* `--tag TAG` (e.g. `--tag v1.6.1`): Use the version identified by the Git tag given
* `--commit COMMIT` (e.g. `--commit abc123`): Use the version identified by the Git commit SHA given
