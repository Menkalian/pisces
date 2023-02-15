<script setup lang="ts">
import TintedSVG from "@/components/base/TintedSVG.vue";
import CancelIcon from "@/assets/icons/icon_cancel.svg"
import SubmitIcon from "@/assets/icons/icon_submit.svg"
import {ref, watch} from "vue";

const props = defineProps(
    {
      open: Boolean,
      title: String,
      classes: String,
      autoCloseOnSubmit: Boolean,
      showCancel: Boolean,
      showFooter: Boolean,
      submitActive: Boolean,
    }
)
const emit = defineEmits(["close", "submit", "update:open"])

const visible = ref(props.open)
const submitButtonClasses = ref("button");
if (!props.submitActive) {
  submitButtonClasses.value = "button disabled"
}

watch(props, updatedProps => {
  visible.value = updatedProps.open;
  if (updatedProps.submitActive) {
    submitButtonClasses.value = "button"
  } else {
    submitButtonClasses.value = "button disabled"
  }
})

function closePopup() {
  emit('close')
  emit("update:open", false)
  visible.value = false
}

function submitPopup() {
  if (!props.submitActive) {
    return
  }

  emit('submit')
  if (props.autoCloseOnSubmit) {
    closePopup()
  }
}

</script>

<template>
  <Transition mode="in-out">
    <div v-if="visible" class="modal">
      <div :class="'modalcontent ' + props.classes">
        <div class="header">
          <span>{{ props.title }}</span>
          <div class="close" v-if="props.showCancel" @click="closePopup()">
            <TintedSVG
                :src="CancelIcon"
                alt="&times;"
                color-hex="--color-on-error"/>
          </div>
        </div>
        <div class="content">
          <slot/>
        </div>
        <div v-if="showFooter" class="footer">
          <div class="spacer"/>
          <div v-if="props.showCancel" class="button abort" @click="closePopup()">
            <TintedSVG
                :src="CancelIcon"
                alt="&times;"
                color-hex="--color-on-error"/>
            <span>Cancel</span>
          </div>
          <div :class="submitButtonClasses" @click="submitPopup()">
            <TintedSVG
                :src="SubmitIcon"
                alt="&check;"
                color-hex="--color-on-secondary"/>
            <span>Confirm</span>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal {
  position         : fixed; /* Stay in place */
  z-index          : 15; /* Sit on top */
  left             : 0;
  top              : 0;
  width            : 100%; /* Full width */
  height           : 100%; /* Full height */
  overflow         : auto; /* Enable scroll if needed */
  background-color : rgb(0, 0, 0); /* Fallback color */
  background-color : rgba(0, 0, 0, 0.1); /* Black w/ opacity */
}

.modalcontent {
  background-color : var(--color-background-popup);
  position         : fixed;
  left             : 30%;
  top              : 10%;
  border-radius    : 1rem;
  border           : 1px solid var(--color-border-popup);
  width            : 40%;
  padding          : 0.5rem
}

.header {
  width         : 100%;
  padding       : 0.5rem 0;
  display       : inline-flex;
}

.header > * {
  align-self  : flex-start;
  color: var(--color-on-primary);
  text-align: center;

  font-family : SourceSans, sans-serif;
  font-size   : xxx-large;
  font-weight : bold;
}

.header span {
  flex-grow : 1;
}

.header .close {
  background-color : var(--color-error);
  color            : var(--color-on-error);
  align-self       : flex-end;
  display          : inline-flex;

  border-radius    : 1rem;
  padding          : 0.5rem;
}

.footer {
  width          : 100%;
  padding        : 0.5rem 0;
  display        : inline-flex;
  flex-direction : row;
  align-items    : flex-end;
}

.spacer {
  flex-grow : 1;
}

.abort {
  background-color : var(--color-error);
  color            : var(--color-on-error);
}

.disabled {
  background-color : var(--color-neutral);
  color            : var(--color-on-secondary);
}

.disabled:hover {
  cursor : no-drop;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition : all 0.25s ease-out;
}

.slide-up-enter-from {
  opacity   : 0;
  transform : translateY(30px);
}

.slide-up-leave-to {
  opacity   : 0;
  transform : translateY(30px);
}
</style>