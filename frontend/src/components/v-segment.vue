<template lang="pug">
.segment(v-bind:class="{ untouched: !segment.authored, active: isOpen }")
  .closer(v-if="canOpen",
    v-on:click="toggleCode", ref="topButton")
    font-awesome-icon.icon(
      v-show="!isOpen",
      icon="plus-circle",
      v-bind:title="'Click to open code'"
    )
    font-awesome-icon.icon(
      v-show="isOpen",
      icon="chevron-circle-down",
      v-bind:title="'Click to hide code'"
    )
  div(v-if="isOpen", v-hljs="path")
    .code(v-for="(line, index) in segment.lines", v-bind:key="index")
      .line-number {{ segment.lineNumbers[index] + "\n" }}
      .line-content {{ line + "\n" }}
  .closer.bottom(v-if="canOpen", v-on:click="toggleCode")
    font-awesome-icon.icon(
      v-show="isOpen",
      icon="chevron-circle-up",
      v-bind:title="'Click to hide code'"
    )
</template>

<script>
export default {
  name: 'v-segment',
  props: ['segment', 'path'],
  data() {
    return {
      isOpen: this.segment.authored || this.segment.lines.length < 5,
      canOpen: !this.segment.authored && this.segment.lines.length > 4,
    };
  },
  methods: {
    toggleCode() {
      this.isOpen = !this.isOpen;
    },
  },
};
</script>
