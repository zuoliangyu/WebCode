<template>
  <div>
    <Header />
    <div style="display: flex">
      <Aside />
      <div style="flex: 1; background-color: #F5F7FA; min-height: calc(100vh - 50px); overflow-y: auto">
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
