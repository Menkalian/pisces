<template>
  <div>
    <label @click="toggleOrSet(false)">
      <slot name="left"/>
    </label>
    <label class='switch'>
      <input type='checkbox' v-model="active">
      <span class='slider'></span>
    </label>
    <label @click="toggleOrSet(true)">
      <slot name="right"/>
    </label>
  </div>
</template>

<script setup lang="ts">
import {ref, useSlots, watch} from "vue";

const props = defineProps(
    {
      colorLeftActive: String,
      colorRightActive: String,
      colorDisabled: String,
      height: String,
      modelValue: Boolean,
      editable: Boolean
    })
const emit = defineEmits(['update:modelValue'])

const height = ref(props.height || "2.5rem")
const leftColor = ref(props.colorLeftActive || "var(--theme-neutral-color)")
const rightColor = ref(props.colorRightActive || "var(--theme-secondary-color)")
const lockedColor = ref(props.colorDisabled || "var(--theme-secondary-color-dark)")

const active = ref(props.modelValue)
const slots = useSlots()

function toggleOrSet(value: boolean) {
  if (slots.left && slots.right) {
    active.value = value
  } else {
    active.value = !active.value
  }
}

watch(active, (newValue) => {
  emit("update:modelValue", newValue);
})
</script>

<style scoped>
.switch {
  --height-control-element : v-bind(height);
  position                 : relative;
  display                  : inline-block;
  width                    : calc(1.923 * var(--height-control-element));
  height                   : var(--height-control-element);

  --slider-padding         : 0.15rem;
  --slider-size            : calc(var(--height-control-element) - 2 * var(--slider-padding));
}

/* Hide default HTML checkbox */
.switch input {
  opacity : 0;
  width   : 0;
  height  : 0;
}

/* The slider */
.slider {
  border-radius      : var(--height-control-element);
  position           : absolute;
  cursor             : pointer;
  top                : 0;
  left               : 0;
  right              : 0;
  bottom             : 0;
  background-color   : v-bind(leftColor);
  -webkit-transition : .4s;
  transition         : .4s;
}

.slider:before {
  border-radius      : 50%;
  position           : absolute;
  content            : "";
  height             : var(--slider-size);
  width              : var(--slider-size);
  left               : var(--slider-padding);
  bottom             : var(--slider-padding);
  background-color   : white;
  -webkit-transition : .4s;
  transition         : .4s;
}

input:checked + .slider {
  background-color : v-bind(rightColor);
}

input:checked:disabled + .slider {
  background-color : v-bind(lockedColor);
}

input:focus + .slider {
  box-shadow : 0 0 1px v-bind(rightColor);
}

input:checked + .slider:before {
  -webkit-transform : translateX(calc(var(--slider-size) + var(--slider-padding)));
  -ms-transform     : translateX(calc(var(--slider-size) + var(--slider-padding)));
  transform         : translateX(calc(var(--slider-size) + var(--slider-padding)));
}

:not(input:checked) + .slider:before {
  -webkit-transform : translateX(calc(var(--slider-padding)));
  -ms-transform     : translateX(calc(var(--slider-padding)));
  transform         : translateX(calc(var(--slider-padding)));
}

label:hover {
  cursor : pointer;
}

div {
  display     : inline-flex;
  align-items : center;
}
</style>