<template lang="pug">
#app-wrapper(
  v-bind:style="appStyles",
  @mousemove="mouseMove",
  @mouseup="deregisterMouseMove",
  @mouseleave="deregisterMouseMove"
)
  #tab-resize-guide(v-bind:style="guideStyles")
  .left-resize-container
    slot(name="left")
  #tab-resize(
    @mousedown.left="registerMouseMove",
    v-show="isTabActive"
  )
    .tab-close(v-on:click="closeTab")
      i.fas.fa-caret-right
  .right-resize-container(
    v-bind:style="rightContainerStyles",
    v-if="isTabActive"
  )
    slot(name="right")
</template>


<script>
import { mapState } from 'vuex';

const DRAG_BAR_WIDTH = 13.25;
const SCROLL_BAR_WIDTH = 17;
const GUIDE_BAR_WIDTH = 2;

const throttledEvent = (delay, handler) => {
  let lastCalled = 0;
  return (...args) => {
    if (Date.now() - lastCalled > delay) {
      lastCalled = Date.now();
      handler(...args);
    }
  };
};

export default {
  name: 'v-resizer',

  data() {
    return {
      guideWidth: (0.5 * window.innerWidth - (GUIDE_BAR_WIDTH / 2)) / window.innerWidth,
      flexWidth: 0.5,
      isResizing: false,
    };
  },

  methods: {
    registerMouseMove() {
      this.isResizing = true;
    },

    deregisterMouseMove() {
      this.isResizing = false;
      this.flexWidth = (this.guideWidth * window.innerWidth + (GUIDE_BAR_WIDTH / 2))
        / window.innerWidth;
    },

    closeTab() {
      this.$store.commit('updateTabState', false);
    },
  },

  computed: {
    appStyles() {
      return this.isResizing
        ? 'user-select: none; cursor: col-resize;'
        : '';
    },

    guideStyles() {
      return this.isResizing
        ? `display: block; right: ${this.guideWidth * 100}%;`
        : '';
    },

    rightContainerStyles() {
      return `flex: 0 0 ${this.flexWidth * 100}%;`;
    },

    mouseMove() {
      if (this.isResizing) {
        return throttledEvent(25, (event) => {
          this.guideWidth = (
            Math.min(
                Math.max(
                    window.innerWidth - event.clientX,
                    SCROLL_BAR_WIDTH + DRAG_BAR_WIDTH,
                ),
                window.innerWidth - SCROLL_BAR_WIDTH,
            )
            - (GUIDE_BAR_WIDTH / 2)
          ) / window.innerWidth;
        });
      }
      return () => {};
    },

    ...mapState(['isTabActive']),
  },
};
</script>
