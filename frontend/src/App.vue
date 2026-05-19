<template>
  <div id="app">
    <RouterView />
    <AppNotification />
  </div>
</template>

<script setup lang="ts">
import AppNotification from './components/AppNotification.vue';
</script>

<style>
#app {
  min-height: 100vh;
  background: #f8fafc;
  color: #0f172a;
}
</style>
