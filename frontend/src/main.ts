import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import VueAxios from "vue-axios"
import type {PluginOptions} from "vue-toastification";
// @ts-ignore
import Toast, {POSITION} from "vue-toastification"
import axios from "axios"

import './assets/fonts.css'
import './assets/main.css'
// Import the CSS or use your own!
import "vue-toastification/dist/index.css";

const app = createApp(App)

const toastOptions: PluginOptions = {
    timeout: 5000,
    position: POSITION.TOP_CENTER
}

app.use(router)
app.use(VueAxios, axios)
app.use(Toast, toastOptions)

app.mount('#app')
