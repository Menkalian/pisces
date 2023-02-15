<script setup lang="ts">
import {onMounted, ref} from "vue";
import {EVENT_USER_LOGIN, EVENT_USER_LOGOUT, EventBus} from "@/services/EventBus";
import type {UserData} from "@/data/CommonTypes";
import TintedSVG from "@/components/base/TintedSVG.vue";
import LoginIcon from "@/assets/icons/icon_login.svg";
import {useCookies} from "vue3-cookies";
import router from "@/router";
import {FlunderClient} from "@/services/FlunderClient";

const client = new FlunderClient()
const loggedIn = ref(false)
const loggedInUser = ref<UserData | null>(null)
const userName = ref("")
const imageUrl = ref("")

EventBus.on(EVENT_USER_LOGIN, user => {
  loggedIn.value = true
  let userData = user as UserData;
  loggedInUser.value = userData
  userName.value = userData.name
  imageUrl.value = userData.avatarUrl
})
EventBus.on(EVENT_USER_LOGOUT, () => {
  loggedIn.value = false
  loggedInUser.value = null
  userName.value = ""
  imageUrl.value = ""
})

onMounted(() => {
  client.getUserData()
        .then(data => {
          if (data.name != undefined) {
            EventBus.emit(EVENT_USER_LOGIN, data)
          } else {
            EventBus.emit(EVENT_USER_LOGOUT)
          }
        })
        .catch((_) => {
          EventBus.emit(EVENT_USER_LOGOUT)
        })
})

function logout() {
  useCookies().cookies.remove("SESSION")
  EventBus.emit(EVENT_USER_LOGOUT)
}
</script>

<template>
  <div v-if="loggedIn" id="userInfo" @click="logout()">
    <h3>{{ userName }}</h3>
    <div class="cropper">
      <img :src="imageUrl" alt="Avatar" class="fitimage">
    </div>
  </div>
</template>

<style scoped>

a {
  text-decoration : none;
  margin          : auto 1rem;
}

h3 {
  font-family : SourceSans, sans-serif;
  font-size   : x-large;
}

#userInfo {
  margin-left  : 4.5rem;
  margin-right : 1rem;
  padding-left : 0.5rem;
  alignment    : center;
  height       : 100%;
}

.cropper {
  width          : 3.4rem;
  margin         : auto auto auto 0.5rem;
  vertical-align : center;
  position       : relative;
  overflow       : hidden;
}

.cropper img {
  display               : block;
  height                : auto;
  background-color      : var(--color-neutral);
  width                 : 100%;
  margin                : 0;
  -webkit-border-radius : 50%;
  -moz-border-radius    : 50%;
  -ms-border-radius     : 50%;
  -o-border-radius      : 50%;
  border-radius         : 50%;
}

</style>