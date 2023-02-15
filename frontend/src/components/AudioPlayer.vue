<script setup lang="ts">
import {EVENT_USER_LOGIN, EventBus} from "@/services/EventBus";
import {ref} from "vue";
import type {AudioPlayerStateData, TrackMessageData} from "@/data/CommonTypes";
import {FlunderClient} from "@/services/FlunderClient";
import TintedSVG from "@/components/base/TintedSVG.vue";
import PlayIcon from "@/assets/icons/icon_audio_play.svg"
import SkipIcon from "@/assets/icons/icon_audio_skip.svg"
import PauseIcon from "@/assets/icons/icon_audio_pause.svg"
import RepeatIcon from "@/assets/icons/icon_audio_repeat.svg"
import ShuffleIcon from "@/assets/icons/icon_audio_shuffle.svg"
import {useToast} from "vue-toastification";

const client = new FlunderClient()
const inGuild = ref(false)
const audioInfo = ref<TrackMessageData | null>(null)
const audioInfoUnsubscribe = ref<(() => void) | null>(null)
const playerState = ref<AudioPlayerStateData | null>(null)
const playerStateUnsubscribe = ref<(() => void) | null>(null)
const loggedIn = ref(false)
const lastGuild = ref("")
const toast = useToast()

EventBus.on(EVENT_USER_LOGIN, () => {
  loggedIn.value = true
  const queryGuildState = () => {
    client.getConnectedGuild()
          .then(guildId => {
            if (guildId === lastGuild.value) {
              return
            }

            lastGuild.value = guildId
            inGuild.value = true
            if (audioInfoUnsubscribe.value != null) {
              audioInfoUnsubscribe.value()
            }
            audioInfoUnsubscribe.value = client.subscribeAudioInfo(guildId, info => {
              audioInfo.value = info
            })
            if (playerStateUnsubscribe.value != null) {
              playerStateUnsubscribe.value()
            }
            playerStateUnsubscribe.value = client.subscribePlayerInfo(guildId, info => {
              playerState.value = info
            })
          })
          .catch(() => {
            lastGuild.value = ""
            inGuild.value = false
            if (audioInfoUnsubscribe.value != null) {
              audioInfoUnsubscribe.value()
            }
            if (playerStateUnsubscribe.value != null) {
              playerStateUnsubscribe.value()
            }
          })
  }

  setInterval(queryGuildState, 1000)
  queryGuildState()
})

function showError(action: string, reason: any) {
  let msg = `Ein Fehler ist beim ${action} aufgetreten: ${reason}`
  console.error(msg)
  toast.error(msg)
}

function sendSkip() {
  client.skipTrack()
        .then(res => {})
        .catch(err => {
    showError("Überspringen des Tracks", err)
  });
}

function togglePause() {
  client.togglePause()
        .then(res => {})
        .catch(err => {
    showError("Pausieren/Fortsetzen der Wiedergabe", err)
  });
}

function toggleShuffle() {
  client.toggleShuffle()
        .then(res => {})
        .catch(err => {
    showError("De-/Aktivieren der Zufallswiedergabe", err)
  });
}

function toggleRepeat() {
  client.toggleLoop()
        .then(res => {})
        .catch(err => {
    showError("De-/Aktivieren der Wiederholung", err)
  });
}
</script>

<template>
  <div v-if="playerState != null && audioInfo != null && inGuild" class="audiocontrols">
    <div id="thumbnail">
      <img v-if="audioInfo.sourcetype === 'YOUTUBE'" :src="`https://img.youtube.com/vi/${audioInfo.sourceIdentifier}/default.jpg`" alt="thumbnail">
      <div class="background-gray"></div>
    </div>
    <div id="title"><span @scroll.stop class="autoscroll">{{ audioInfo.title }}</span></div>
    <div id="artist"><span @scroll.stop class="autoscroll">{{ audioInfo.author }}</span></div>
    <div id="playicon" @click="togglePause()">
      <TintedSVG
          v-if="!playerState.isPaused"
          :src="PauseIcon"
          alt="⏸️"
          color-hex="--color-on-primary"/>
      <TintedSVG
          v-else
          :src="PlayIcon"
          alt="▶️"
          color-hex="--color-on-primary"/>
    </div>
    <div id="skip" @click="sendSkip()">
      <TintedSVG
          :src="SkipIcon"
          alt="⏭️"
          color-hex="--color-on-primary"/>
    </div>
    <progress :max="audioInfo.length" :value="audioInfo.position"/>
    <div id="shuffle" @click="toggleShuffle()">
      <TintedSVG
          v-if="playerState.isShuffle"
          :src="RepeatIcon"
          alt="🔀"
          color-hex="--color-on-primary"/>
      <TintedSVG
          v-else
          :src="RepeatIcon"
          alt="❌🔀❌"
          color-hex="--color-disabled-on-primary"/>
    </div>
    <div id="repeat" @click="toggleRepeat()">
      <TintedSVG
          v-if="playerState.isRepeating"
          :src="ShuffleIcon"
          alt="🔁"
          color-hex="--color-on-primary"/>
      <TintedSVG
          v-else
          :src="ShuffleIcon"
          alt="❌🔁❌"
          color-hex="--color-disabled-on-primary"/>
    </div>
  </div>
  <div v-if="!inGuild && loggedIn">
    <span>Tritt einem VoiceChannel auf einem Server bei!</span>
  </div>
</template>

<style scoped>
span {
  font-family : SourceSans, sans-serif;
  font-size   : medium;
}

.audiocontrols {
  display           : grid;
  width             : auto;
  height            : 100%;

  grid-auto-columns : minmax(0, 1fr);
  grid-auto-rows    : minmax(0, 1fr);
}

.audiocontrols #title {
  display       : flex;
  margin        : 0;

  height        : 100%;

  grid-column   : 3/9;
  grid-row      : 1;

  padding-right : 0.2rem;
  overflow      : hidden;

  font-family   : SourceSans, sans-serif;
  font-size     : large;
}

.audiocontrols #artist {
  display     : flex;
  margin      : 0;

  height      : 100%;

  grid-column : 9/11;
  grid-row    : 1;

  overflow    : hidden;
  color       : var(--color-disabled-on-primary);

  font-family : SourceSans, sans-serif;
  font-size   : small;
}

.audiocontrols > div > span {
  align-self         : flex-end;

  white-space        : nowrap;
  overflow           : scroll;

  -ms-overflow-style : none; /* IE and Edge */
  scrollbar-width    : none; /* Firefox */
}

.audiocontrols > div > span::-webkit-scrollbar {
  display : none;
}

.audiocontrols > * {
  display : flex;
  margin  : 4%
}

.audiocontrols #thumbnail {
  margin      : 0;
  grid-column : 1/3;
  grid-row    : 1/3;

  width       : 100%;
  height      : 100%;
}

.audiocontrols #thumbnail img {
  margin     : auto;
  max-width  : 100%;
  max-height : 100%;
  width      : auto;
  height     : auto;
}

.audiocontrols #playicon {
  grid-column : 3;
  grid-row    : 2;
}

.audiocontrols #skip {
  grid-column : 4;
  grid-row    : 2;
}

.audiocontrols progress {
  grid-column : 5 / 9;
  grid-row    : 2;
}

.audiocontrols #shuffle {
  grid-column : 9;
  grid-row    : 2;
}

.audiocontrols #repeat {
  grid-column : 10;
  grid-row    : 2;
}
</style>