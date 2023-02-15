<script setup lang="ts">
import PlayIcon from "@/assets/icons/icon_audio_play.svg"
import TintedSVG from "@/components/base/TintedSVG.vue"
import {ref} from "vue";
import {FlunderClient} from "@/services/FlunderClient";
import {useToast} from "vue-toastification";

const client = new FlunderClient()
const toast = useToast()

const searchterm = ref("")

function queueSong() {
  if (searchterm.value == "") {
    toast.warning("Bitte gib einen Suchbegriff ein.")
    return
  }

  client.playTrack(searchterm.value)
        .then(result => {
          toast.success(`Track eingefügt: ${result.title}`)
        })
        .catch(err => {
          toast.error(`Fehler beim Abspielen des Tracks: ${err}`)
        })
  searchterm.value = ""
}

</script>

<template>
  <div>
    <input type="text" placeholder="Play a song..." v-model="searchterm" class="textbox">
    <div class="button" @click="queueSong()">
      <TintedSVG
          :src="PlayIcon"
          alt="🔍"
          color-hex="--color-on-primary"/>
      <span>Play</span>
    </div>
  </div>
</template>

<style scoped>
.textbox {
  width  : 30rem;
  height : 30%;

  margin : auto;
}

textarea:focus, input:focus {
  outline : none;
}
</style>