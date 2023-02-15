<script setup lang="ts">
import {FlunderClient} from "@/services/FlunderClient";
import TintedSVG from "@/components/base/TintedSVG.vue";
import DeleteIcon from "@/assets/icons/icon_delete.svg"
import PlayIcon from "@/assets/icons/icon_audio_play.svg"
import {useToast} from "vue-toastification";
import {EVENT_REFRESH_PRELOAD, EventBus} from "@/services/EventBus";
import {ref, watch} from "vue";

const client = new FlunderClient()
const toast = useToast()
const props = defineProps({
                            uuid: String,
                            isGlobal: Boolean,
                            displayName: String,
                            uri: String,
                            error: Boolean
                          })

const buttonClass = ref("button")
if (props.error) {
  buttonClass.value = "button disabled"
}

watch(props, p => {
  if (props.error) {
    buttonClass.value = "button disabled"
  } else {
    buttonClass.value = "button"
  }
})

function sendPlay() {
  if (props.uuid != null) {
    let promise: Promise<boolean>
    if (props.isGlobal) {
      promise = client.playGlobalPreloadedTrack(props.uuid, false, true)
    } else {
      promise = client.playPersonalPreloadedTrack(props.uuid, false, true)
    }

    promise.then(res => {
             if (res) {
               toast.success("Erfolg", {timeout: 1500})
             } else {
               toast.error(`Fehler beim Abspielen des Sounds`)
             }
           })
           .catch(err => {
             toast.error(`Fehler beim Abspielen des Sounds: ${err}`)
           })
  }
}

function sendDeleteAndRefresh() {
  if (props.uuid != null) {
    let promise: Promise<boolean>
    if (props.isGlobal) {
      promise = client.deleteGlobalPreloadedTrack(props.uuid)
    } else {
      promise = client.deletePersonalPreloadedTrack(props.uuid)
    }

    promise.then(res => {
             if (res) {
               toast.success("Erfolg", {timeout: 1500})
               EventBus.emit(EVENT_REFRESH_PRELOAD)
             } else {
               toast.error(`Fehler beim Löschen`)
             }
           })
           .catch(err => {
             toast.error(`Fehler beim Löschen: ${err}`)
           })
  }
}
</script>

<template>
  <div class="card">
    <span id="title" class="autoscroll">{{ props.displayName }} </span>
    <div id="sourcelink"><a :href="props.uri" target="_blank">Quelle</a></div>
    <div id="playbutton">
      <div id="deletebutton" class="button" @click="sendDeleteAndRefresh()">
        <TintedSVG
            :src="DeleteIcon"
            alt="🗑️"
            color-hex="--color-on-error"/>
      </div>
      <div :class="buttonClass" @click="sendPlay()">
        <TintedSVG
            :src="PlayIcon"
            alt="▶️"
            color-hex="--color-on-secondary"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  display           : grid;
  width             : 9rem;
  height            : 5rem;
  grid-auto-columns : minmax(0, 1fr);
  grid-auto-rows    : minmax(0, 1fr);

  background-color  : var(--color-primary);

  border-radius     : 1rem;
  padding           : 1rem;
}

#title {
  color              : var(--color-on-primary);

  grid-column        : 1/5;
  grid-row           : 1;

  white-space        : nowrap;
  overflow           : scroll;

  -ms-overflow-style : none; /* IE and Edge */
  scrollbar-width    : none; /* Firefox */

  font-family        : SourceSans, sans-serif;
  font-size          : medium;
}

#title::-webkit-scrollbar {
  display : none;
}

#sourcelink {
  grid-row    : 2;
  grid-column : 1/3;
}

#sourcelink > a {
  color       : var(--color-on-primary);

  font-family : SourceSans, sans-serif;
  font-size   : medium;
}

#playbutton {
  grid-row    : 2/4;
  grid-column : 1/5;

  width       : 100%;

  display     : block;
  align-self  : end;
  text-align: end;
}

#playbutton > div {
  display        : inline-block;
  vertical-align : bottom;

  border-radius  : 50%;
  width          : 1.5rem;
  height         : 1.5rem;
}

#playbutton > #deletebutton {
  background-color : var(--color-error);

  width            : 1rem;
  height           : 1rem;
}

.disabled:hover {
  cursor : no-drop;
}

.disabled {
  background-color : var(--color-background-popup);
  color            : var(--color-disabled-on-primary);
}

</style>