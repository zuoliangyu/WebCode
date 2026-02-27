<template>
  <div class="layout-wrapper">
    <Header />
    <div style="display: flex">
      <Aside />
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import Aside from "../components/Aside";

export default {
  name: "Layout",
  components: {
    Header,
    Aside,
  },
};
</script>

<style scoped>
.layout-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

.main-content {
  flex: 1;
  min-height: calc(100vh - 50px);
  overflow-y: auto;
  background: transparent;
}
</style>
