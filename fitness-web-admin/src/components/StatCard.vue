<template>
  <div class="fp-stat" :class="dimensionClass">
    <div class="fp-stat__label">
      <slot name="label">{{ label }}</slot>
    </div>
    <div class="fp-stat__value">
      <slot>{{ value }}</slot>
      <span v-if="unit" class="fp-stat__unit">{{ unit }}</span>
    </div>
    <div v-if="sub || $slots.sub" class="fp-stat__sub">
      <slot name="sub">{{ sub }}</slot>
    </div>
  </div>
</template>

<script setup>
/**
 * 通用指标卡
 * - 维度色：A / B / C / Bo / muted（设计契约 §5）
 * - 卡片左侧自动呈现对应维度色边条
 */
const props = defineProps({
  label: { type: String, default: '' },
  value:  { type: [String, Number], default: '' },
  unit:   { type: String, default: '' },
  sub:    { type: String, default: '' },
  /** 维度：A / B / C / Bo / muted */
  dim:    { type: String, default: 'B' }
})
import { computed } from 'vue'
const dimensionClass = computed(() => ({
  'fp-stat--A': props.dim === 'A',
  'fp-stat--B': props.dim === 'B',
  'fp-stat--C': props.dim === 'C',
  'fp-stat--Bo': props.dim === 'Bo',
  'fp-stat--muted': props.dim === 'muted'
}))
</script>
