<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import '@/assets/styles/main.css'

onMounted(() => {
  // 应用启动时的全局初始化
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}
</style>
