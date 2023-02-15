<script setup lang="ts">
import {FlunderClient} from "@/services/FlunderClient";
import {onMounted, ref, watch} from "vue";
import type {PreloadedTrackData} from "@/data/CommonTypes";
import {useToast} from "vue-toastification";
import {EVENT_REFRESH_PRELOAD, EVENT_USER_LOGIN, EVENT_USER_LOGOUT, EventBus} from "@/services/EventBus";
import PreloadCard from "@/components/PreloadCard.vue";
import TintedSVG from "@/components/base/TintedSVG.vue";
import AddIcon from "@/assets/icons/icon_add.svg"

const client = new FlunderClient()
const toast = useToast()
const props = defineProps(
    {
      isGlobal: Boolean
    })

const label = ref("")
const disabledClass = ref("")
const displayName = ref("")
const trackUrl = ref("")
const preloads = ref<PreloadedTrackData[]>([])

watch(props, newProps => {
  refreshValues()
})
EventBus.on(EVENT_REFRESH_PRELOAD, () => {
  refreshValues()
})
onMounted(() => {
  client.getUserData()
        .then(data => {
          if (data.name != undefined) {
            refreshValues()
          } else {}
        })
})

function refreshValues() {
  let req: Promise<PreloadedTrackData[]>
  if (props.isGlobal) {
    label.value = "Globale Sounds"
    disabledClass.value = "disabled"
    req = client.getGlobalPreloadedTracks()
  } else {
    label.value = "Deine Sounds"
    disabledClass.value = ""
    req = client.getPersonalPreloadedTracks()
  }

  req.then(res => {
       preloads.value = res
     })
     .catch(err => {
       preloads.value = []
       toast.error(`Fehler beim Laden der Sounds: ${err}`)
     })
}

function doPreload() {
  let input: PreloadedTrackData = {
    preloadUuid: "",
    displayName: displayName.value,
    preloadUrl: trackUrl.value,
    hasError: false
  }
  let req: Promise<PreloadedTrackData>
  if (props.isGlobal) {
    req = client.preloadGlobalTrack(input)
  } else {
    req = client.preloadPersonalTrack(input)
  }
  req.then(() => {
    displayName.value = ""
    trackUrl.value = ""
    refreshValues()
  }).catch(err => {
    toast.error(`Fehler beim Vorladen: ${err}`)
  })
}

</script>

<template>
  <div id="preloadDisplay">
    <h1>{{ label }}</h1>
    <div id="createForm">
      <input type="text" placeholder="Give a name" v-model="displayName" :class="'textbox ' + disabledClass">
      <input type="text" placeholder="Paste the URL" v-model="trackUrl" :class="'textbox ' + disabledClass">
      <div :class="'button ' + disabledClass" @click="doPreload()">
        <TintedSVG
            :src="AddIcon"
            alt="+"
            color-hex="--color-on-primary"/>
        <span>Hinzufügen</span>
      </div>
    </div>
    <div id="preloadsContainer">
      <div v-for="item in preloads">
        <PreloadCard
            :is-global="props.isGlobal"
            :uuid="item.preloadUuid"
            :display-name="item.displayName"
            :uri="item.preloadUrl"
            :error="item.hasError"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
#preloadDisplay {
  background-color : var(--color-secondary);
  padding          : 1.5rem;
  border-radius    : 2rem;
  margin-bottom    : 2rem;
}

h1 {
  margin      : 0;

  color       : var(--color-on-secondary);
  font-family : SourceSans, sans-serif;
  font-size   : xx-large;
}

#createForm {
  display        : flex;
  flex-direction : row;
  height         : 2rem;
  margin-bottom  : 0.5rem;
  align-items    : center;
}

#createForm > * {
  height         : 90%;
  margin         : 0.1rem 0.3rem 0.1rem 0;
  padding-top    : 0.1rem;
  padding-bottom : 0.1rem;
}

.button {
  background-color : var(--color-primary);
}

.textbox.disabled {
  border : 0;
}

.disabled:hover {
  cursor : no-drop;
}

.disabled {
  background-color : var(--color-background-popup);
  color            : var(--color-disabled-on-primary);
}

#preloadsContainer {
  display               : grid;

  gap                   : 0.4rem;
  grid-template-columns : repeat(auto-fill, minmax(min(10rem, 100%), 1fr));
}
</style>