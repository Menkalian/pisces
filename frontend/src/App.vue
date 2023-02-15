<script setup lang="ts">
import {RouterView} from 'vue-router'
import ThemeChanger from "@/components/ThemeChanger.vue"
import {EVENT_USER_LOGIN, EVENT_USER_LOGOUT, EventBus} from "@/services/EventBus";
import TintedSVG from "@/components/base/TintedSVG.vue";
import LoginIcon from "@/assets/icons/icon_login.svg"
import {onMounted, ref} from "vue";
import PopupWindow from "@/components/base/PopupWindow.vue";
import UserInfo from "@/components/UserInfo.vue";
import AudioPlayer from "@/components/AudioPlayer.vue";
import SearchBox from "@/components/SearchBox.vue";

const loggedIn = ref(false)
EventBus.on(EVENT_USER_LOGIN, () => {
  loggedIn.value = true
})
EventBus.on(EVENT_USER_LOGOUT, () => {
  loggedIn.value = false
})

let navigate = () => {
}

onMounted(() => {
  navigate = () => {
    let link = `${window.location.protocol}//${window.location.host}/oauth2/authorization/discord`;
    console.log(link)
    window.location.href = link
  }
})

const SCROLL_DIR_ATTRIBUTE_NAME = "pisces-scroll";
const SCROLL_COUNT = 100;

onMounted(() => {
  let direction = true
  let step = 0
  let changeCount = 10

  setInterval(() => {
    if (direction && (step >= SCROLL_COUNT)) {
      changeCount--
      if (changeCount <= 0) {
        direction = false
        changeCount = 10
      }
    }
    if (!direction && (step <= 0)) {
      changeCount--
      if (changeCount <= 0) {
        direction = true
        changeCount = 10
      }
    }

    if (direction) {
      step++
    } else {
      step--
    }

    let elements = document.getElementsByClassName("autoscroll");
    for (let i = 0 ; i < elements.length ; i++) {
      let e = elements.item(i);
      if (e == null) {
        continue
      }

      let direction = true
      if (e.hasAttribute(SCROLL_DIR_ATTRIBUTE_NAME)) {
        direction = e.getAttribute(SCROLL_DIR_ATTRIBUTE_NAME) === 'true'
      } else {
        e.scrollTo({left: 0})
        e.setAttribute(SCROLL_DIR_ATTRIBUTE_NAME, 'true')
      }

      if (direction && (e.scrollLeft + e.clientWidth) >= e.scrollWidth) {
        direction = false
        e.setAttribute(SCROLL_DIR_ATTRIBUTE_NAME, 'false')
      }
      if (!direction && e.scrollLeft <= 0) {
        direction = true
        e.setAttribute(SCROLL_DIR_ATTRIBUTE_NAME, 'true')
      }

      let scrollTo = (e.scrollWidth - e.clientWidth) * (step / SCROLL_COUNT)
      e.scrollTo({behavior: "smooth", left: scrollTo})
    }
  }, 50)
})
</script>

<template>
  <header>
    <nav>
      <div class="left">
        <AudioPlayer/>
      </div>
      <div class="center">
        <SearchBox/>
      </div>
      <div class="right">
        <ThemeChanger/>
        <UserInfo/>
      </div>
    </nav>
  </header>

  <div id="main">
    <div v-if="loggedIn">
      <RouterView/>
    </div>
    <div v-else>
      <PopupWindow :open="!loggedIn" title="⛔ Access denied ⛔">
        <div class="denied-message">
          <span>
            Der Zugriff auf die Seite ist nur angemeldet möglich
          </span>
          <div class="button" @click="navigate()">
            <TintedSVG color-hex="--color-on-primary" :src="LoginIcon" alt="🔑"/>
            <div>Anmelden</div>
          </div>
        </div>
      </PopupWindow>
    </div>
  </div>
</template>

<style scoped>
html, body, p, div {
  margin  : 0;
  padding : 0;
}

span {
  color         : var(--color-on-primary);
  font-family   : SourceSans, sans-serif;
  font-size     : x-large;
  margin-bottom : 1rem;
  text-align    : center;
}

.button div {
  text-decoration : none;
  color           : var(--color-on-primary);
}

.denied-message {
  display        : flex;
  flex-direction : column;
}

#main {
  padding-top : 4rem;
}

nav {
  background-color  : var(--color-primary);
  color             : var(--color-on-primary);

  display           : grid;
  grid-auto-columns : minmax(0, 1fr);
  grid-auto-rows    : minmax(0, 1fr);

  height            : 4rem;
  width             : 100%;
  min-width         : min-content;

  margin            : 0;
  padding           : 0;

  position          : fixed;
  z-index           : 10;
  top               : 0;
  left              : 0;
}

nav > div {
  display : inline-block;
  height  : 100%;
}

nav div {
  display : inline-flex;
  height  : 100%;
}

.left {
  grid-column     : 1;
  grid-row        : 1;
  justify-content : left;
  display         : block;
}

.center {
  grid-column     : 2/4;
  grid-row        : 1;
  justify-content : center;
}

.right {
  grid-column     : 4;
  grid-row        : 1;
  justify-content : right;
}

html {
  background-color : var(--color-background);
}
</style>
