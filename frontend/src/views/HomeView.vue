<script setup lang="ts">
import PreloadDisplay from "@/components/PreloadDisplay.vue";</script>

<template>
  <main>
    <PreloadDisplay :is-global="true"/>
    <PreloadDisplay :is-global="false"/>
  </main>
</template>

<style scoped>
main {
  margin : 3rem 10%;
}
</style>
