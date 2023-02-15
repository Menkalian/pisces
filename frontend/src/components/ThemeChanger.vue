<template>
  <ToggleSwitch
      id="colorSwitch"
      height="1.5rem"
      v-model="darkThemeActive"
      color-left-active="var(--theme-secondary-color-light)"
      color-right-active="var(--theme-secondary-color-dark)">
    <template #left>
      <TintedSVG :src="iconLightMode"
                 alt="Light Mode"
                 color-hex="--color-on-primary"
      />
    </template>
    <template #right>
      <TintedSVG :src="iconDarkMode"
                 alt="Dark Mode"
                 color-hex="--color-on-primary"
      />
    </template>
  </ToggleSwitch>
</template>

<script setup lang="ts">
import ToggleSwitch from "@/components/base/ToggleSwitch.vue";
import {useCookies} from "vue3-cookies";
import {onMounted, ref, watch} from "vue";
import TintedSVG from "@/components/base/TintedSVG.vue";
import iconLightMode from "@/assets/icons/icon_light_mode.svg"
import iconDarkMode from "@/assets/icons/icon_dark_mode.svg"
import {EVENT_THEME_CHANGED, EventBus} from "@/services/EventBus";

const THEME_KEY_NAME = "theme";
const DARK_THEME_NAME = "dark";
const LIGHT_THEME_NAME = "light"

const cookies = useCookies()
if (!cookies.cookies.isKey(THEME_KEY_NAME)) {
  cookies.cookies.set(THEME_KEY_NAME, DARK_THEME_NAME)
}
const currentThemeName = ref(cookies.cookies.get(THEME_KEY_NAME));
const darkThemeActive = ref(currentThemeName.value === DARK_THEME_NAME)

watch(darkThemeActive, (newValue: boolean) => {
  if (newValue) {
    currentThemeName.value = DARK_THEME_NAME;
  } else {
    currentThemeName.value = LIGHT_THEME_NAME;
  }
})

onMounted(() => {
  configureTheme(currentThemeName.value)
  EventBus.emit(EVENT_THEME_CHANGED)

  watch(currentThemeName, changedName => {
    configureTheme(changedName)
    EventBus.emit(EVENT_THEME_CHANGED)
  })
})

class Theme {
  public colorPrimary ?: string
  public colorOnPrimary ?: string
  public colorDisabledOnPrimary ?: string
  public colorSecondary ?: string
  public colorOnSecondary ?: string
  public colorNeutral ?: string
  public colorBackground ?: string
  public colorBackgroundPopup ?: string
  public colorBorderPopup ?: string
  public colorError ?: string
  public colorOnError ?: string


  constructor(
      colorPrimary: string,
      colorOnPrimary: string,
      colorDisabledOnPrimary: string,
      colorSecondary: string,
      colorOnSecondary: string,
      colorNeutral: string,
      colorBackground: string,
      colorBackgroundPopup: string,
      colorBorderPopup: string,
      colorError: string,
      colorOnError: string
  ) {
    this.colorPrimary = colorPrimary;
    this.colorOnPrimary = colorOnPrimary;
    this.colorDisabledOnPrimary = colorDisabledOnPrimary;
    this.colorSecondary = colorSecondary;
    this.colorOnSecondary = colorOnSecondary;
    this.colorNeutral = colorNeutral;
    this.colorBackground = colorBackground;
    this.colorBackgroundPopup = colorBackgroundPopup;
    this.colorBorderPopup = colorBorderPopup;
    this.colorError = colorError;
    this.colorOnError = colorOnError;
  }

  applyTheme(style: CSSStyleDeclaration) {
    style.setProperty("--color-primary", this.colorPrimary || null);
    style.setProperty("--color-on-primary", this.colorOnPrimary || null);
    style.setProperty("--color-disabled-on-primary", this.colorDisabledOnPrimary || null);
    style.setProperty("--color-secondary", this.colorSecondary || null);
    style.setProperty("--color-on-secondary", this.colorOnSecondary || null);
    style.setProperty("--color-neutral", this.colorNeutral || null);
    style.setProperty("--color-background", this.colorBackground || null);
    style.setProperty("--color-background-popup", this.colorBackgroundPopup || null);
    style.setProperty("--color-border-popup", this.colorBorderPopup || null);
    style.setProperty("--color-error", this.colorError || null);
    style.setProperty("--color-on-error", this.colorOnError || null);
  }
}

function configureTheme(name: string) {
  cookies.cookies.set(THEME_KEY_NAME, name)
  const style = document.documentElement.style;
  switch (name) {
    case LIGHT_THEME_NAME:
      new Theme(
          getCssVariable("--theme-primary-color-light"),
          getCssVariable("--theme-on-primary-color-light"),
          getCssVariable("--theme-disabled-on-primary-color-light"),
          getCssVariable("--theme-secondary-color-light"),
          getCssVariable("--theme-on-secondary-color-light"),
          getCssVariable("--theme-neutral-color-light"),
          getCssVariable("--theme-background-light"),
          getCssVariable("--theme-background-popup-light"),
          getCssVariable("--theme-border-popup-light"),
          "#FF6969",
          "#000000",
      ).applyTheme(style)
      break;
    case DARK_THEME_NAME:
    default:
      new Theme(
          getCssVariable("--theme-primary-color-dark"),
          getCssVariable("--theme-on-primary-color-dark"),
          getCssVariable("--theme-disabled-on-primary-color-dark"),
          getCssVariable("--theme-secondary-color-dark"),
          getCssVariable("--theme-on-secondary-color-dark"),
          getCssVariable("--theme-neutral-color-dark"),
          getCssVariable("--theme-background-dark"),
          getCssVariable("--theme-background-popup-dark"),
          getCssVariable("--theme-border-popup-dark"),
          "#A10000",
          "#FFFFFF",
      ).applyTheme(style)
      break;
  }
}

function getCssVariable(variable: string): string {
  return getComputedStyle(document.getElementById("colorSwitch")!).getPropertyValue(variable)
}
</script>

<!-- Basic Theme colors (real theming is derived from here).-->
<!-- Primary (blue): #01579B -->
<!-- Secondary/Accent (yellow): #ffe400 -->
<!--https://m2.material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=466ba5&secondary.color=00ACC1 -->
<style>
:root {
  --color-white                           : #F5F5F5;
  --color-black                           : #0A0A0A;
  --theme-primary-color                   : #466BA5;
  --theme-on-primary-color                : var(--color-white);
  --theme-disabled-on-primary-color       : #A6A6A6;
  --theme-primary-color-light             : #79A7F7;
  --theme-on-primary-color-light          : var(--color-black);
  --theme-disabled-on-primary-color-light : #5C5C5C;
  --theme-primary-color-dark              : #074176;
  --theme-on-primary-color-dark           : var(--color-white);
  --theme-disabled-on-primary-color-dark  : #A6A6A6;

  --theme-secondary-color                 : #00ACC1;
  --theme-on-secondary-color              : var(--color-black);
  --theme-secondary-color-light           : #5DDEF4;
  --theme-on-secondary-color-light        : var(--color-black);
  --theme-secondary-color-dark            : #007C91;
  --theme-on-secondary-color-dark         : var(--color-white);

  --theme-neutral-color                   : #CCCCCC;
  --theme-neutral-color-light             : #D9D9D9;
  --theme-neutral-color-dark              : #A9A9A9;

  --theme-background-dark                 : #1C1D4D;
  --theme-background-light                : #C6CBFF;
  --theme-background-popup-dark           : #1F1F1F;
  --theme-border-popup-dark               : #D9D9D9;
  --theme-background-popup-light          : #E0E0E0;
  --theme-border-popup-light              : #545454;
}

/*
 * Dummy values, which are set by the theme. these are declared here to satisfy the IDE.
 */
:root {
  --color-primary             : #000;
  --color-on-primary          : #000;
  --color-disabled-on-primary : #000;
  --color-secondary           : #000;
  --color-on-secondary        : #000;
  --color-neutral             : #000;
  --color-background          : #000;
  --color-background-popup    : #000;
  --color-border-popup        : #000;
  --color-error               : #000;
  --color-on-error            : #000;
}
</style>