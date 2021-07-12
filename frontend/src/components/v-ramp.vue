<template lang="pug">
.ramp
  template(v-if="tframe === 'commit'")
    template(v-for="(slice, j) in user.commits")
      a.ramp__slice(
        draggable="false",
        v-on:click="rampClick",
        v-for="(commit, k) in slice.commitResults",
        v-if="commit.insertions>0",
        v-bind:href="getLink(user, commit)", target="_blank",
        v-bind:title="getContributionMessage(slice, commit)",
        v-bind:class="'ramp__slice--color' + getSliceColor(slice.date)",
        v-bind:style="{\
          zIndex: user.commits.length - j,\
          borderLeftWidth: getWidth(commit) + 'em',\
          right: ((getSlicePos(slice.date)\
            + (getCommitPos(k, slice.commitResults.length))) * 100) + '%'\
          }"
      )

  template(v-else)
    a.ramp__slice(
      draggable="false",
      v-for="(slice, j) in user.commits",
      v-if="slice.insertions > 0",
      v-bind:title="getContributionMessage(slice)",
      v-on:click="openTabZoom(user, slice, $event)",
      v-bind:class="'ramp__slice--color' + getSliceColor(slice.date)",
      v-bind:style="{\
        zIndex: user.commits.length - j,\
        borderLeftWidth: getWidth(slice) + 'em',\
        right: (getSlicePos(tframe === 'day' ? slice.date : slice.endDate) * 100) + '%' \
        }"
    )
</template>

<script>
export default {
  name: 'v-ramp',
  props: ['groupby', 'user', 'tframe', 'avgsize', 'sdate', 'udate', 'mergegroup', 'fromramp', 'filtersearch'],
  data() {
    return {
      rampSize: 0.01,
    };
  },

  methods: {
    getLink(user, slice) {
      if (this.mergegroup) {
        return `${window.getBaseLink(slice.repoId)}/commit/${slice.hash}`;
      }

      return `${window.getBaseLink(user.repoId)}/commit/${slice.hash}`;
    },
    getWidth(slice) {
      if (slice.insertions === 0) {
        return 0;
      }

      const newSize = 100 * (slice.insertions / this.avgsize);
      return Math.max(newSize * this.rampSize, 0.5);
    },
    getContributionMessage(slice, commit) {
      let title = '';
      if (this.tframe === 'commit') {
        return `[${slice.date}] ${commit.messageTitle}: ${commit.insertions} lines`;
      }

      title = this.tframe === 'day'
            ? `[${slice.date}] Daily `
            : `[${slice.date} till ${slice.endDate}] Weekly `;
      title += `contribution: ${slice.insertions} lines`;
      return title;
    },
    openTabZoom(user, slice, evt) {
      // prevent opening of zoom tab when cmd/ctrl click
      if (window.isMacintosh ? evt.metaKey : evt.ctrlKey) {
        return;
      }

      const zoomUser = { ...user };
      zoomUser.commits = user.dailyCommits;

      const info = {
        zRepo: user.repoName,
        zAuthor: user.name,
        zFilterGroup: this.groupby,
        zTimeFrame: 'commit',
        zAvgCommitSize: slice.insertions,
        zUser: zoomUser,
        zLocation: window.getBaseLink(user.repoId),
        zSince: slice.date,
        zUntil: this.tframe === 'day' ? slice.date : slice.endDate,
        zIsMerged: this.mergegroup,
        zFromRamp: true,
        zFilterSearch: this.filtersearch,
      };
      window.deactivateAllOverlays();

      this.$store.commit('updateTabZoomInfo', info);
    },

    // position for commit granularity
    getCommitPos(i, total) {
      return (total - i - 1) * window.DAY_IN_MS / total
          / (this.getTotalForPos(this.sdate, this.udate) + window.DAY_IN_MS);
    },
    // position for day granularity
    getSlicePos(date) {
      const total = this.getTotalForPos(this.sdate, this.udate);
      return (new Date(this.udate) - new Date(date)) / (total + window.DAY_IN_MS);
    },

    // get duration in miliseconds between 2 date
    getTotalForPos(sinceDate, untilDate) {
      return new Date(untilDate) - new Date(sinceDate);
    },
    getSliceColor(date) {
      const timeMs = this.fromramp
          ? (new Date(this.sdate)).getTime()
          : (new Date(date)).getTime();

      return (timeMs / window.DAY_IN_MS) % 5;
    },

    // Prevent browser from switching to new tab when clicking ramp
    rampClick(evt) {
      const isKeyPressed = window.isMacintosh ? evt.metaKey : evt.ctrlKey;
      if (isKeyPressed) {
        evt.preventDefault();
      }
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
@import '../styles/_colors.scss';

/* Ramp */
.ramp {
  $height: 3rem;
  background-color: mui-color('blue', '50');
  font-size: 100%;
  height: $height;
  overflow: hidden;
  position: relative;

  &__slice {
    border-left-color: rgba(0, 0, 0, 0);
    border-left-style: solid;
    display: block;
    height: 0;
    position: absolute;
    width: 0;

    &--color0 {
      border-bottom: $height rgba(mui-color('orange'), .5) solid;
    }

    &--color1 {
      border-bottom: $height rgba(mui-color('light-blue'), .5) solid;
    }

    &--color2 {
      border-bottom: $height rgba(mui-color('green'), .5) solid;
    }

    &--color3 {
      border-bottom: $height rgba(mui-color('indigo'), .5) solid;
    }

    &--color4 {
      border-bottom: $height rgba(mui-color('pink'), .5) solid;
    }
  }
}
</style>
