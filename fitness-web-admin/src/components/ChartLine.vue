<template>
  <div class="fp-card fp-chart-wrap">
    <div v-if="title" class="fp-section-title">
      {{ title }}
    </div>
    <v-chart
      class="fp-chart"
      :option="fullOption"
      :loading="loading"
      autoresize
    />
  </div>
</template>

<script setup>
/**
 * 折线图封装（基于 vue-echarts）
 * Props:
 *   title     卡片标题
 *   series    [{ name, color, data:[number|{value, ...}] }] —— color 不传时按维度色取默认
 *   xData     横轴标签数组（与每条 series.data 对齐）
 *   yUnit     纵轴单位（如 kg / ml / kcal）
 *   dim       默认维度色（A/B/C/Bo），每条 series 可单独覆盖 color
 *   loading   加载态
 *   smooth    是否平滑曲线（默认 true）
 *   area      是否区域填充（默认 true）
 */
import { computed } from 'vue'
import VChart from '@/utils/echarts'

const props = defineProps({
  title:   { type: String, default: '' },
  series:  { type: Array, default: () => [] },
  xData:   { type: Array, default: () => [] },
  yUnit:   { type: String, default: '' },
  dim:     { type: String, default: 'B' },
  loading: { type: Boolean, default: false },
  smooth:  { type: Boolean, default: true },
  area:    { type: Boolean, default: true }
})

const DIM_COLORS = {
  A: '#8E24AA',
  B: '#1E88E5',
  C: '#43A047',
  Bo: '#FF6F00'
}

function colorOf(i, explicit) {
  if (explicit) return explicit
  // 单条无显式色：用默认维度色；多条：轮转维度色 + 品牌色
  const palette = [DIM_COLORS[props.dim] || DIM_COLORS.B, '#7c5cff', '#22d3ee', '#f59e0b', '#10b981']
  return palette[i % palette.length]
}

const fullOption = computed(() => {
  const series = props.series.map((s, i) => {
    const c = colorOf(i, s.color)
    return {
      name: s.name,
      type: 'line',
      smooth: props.smooth,
      showSymbol: s.data.length <= 30,
      symbolSize: 6,
      data: s.data,
      lineStyle: { color: c, width: 2 },
      itemStyle: { color: c },
      ...(props.area ? {
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: c + '40' },
              { offset: 1, color: c + '05' }
            ]
          }
        }
      } : {})
    }
  })
  return {
    grid: { left: 12, right: 16, top: 28, bottom: 8, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderWidth: 0,
      textStyle: { color: '#e5e7eb', fontSize: 12 },
      axisPointer: { type: 'line', lineStyle: { color: '#94a3b8', type: 'dashed' } },
      valueFormatter: v => v + (props.yUnit ? ' ' + props.yUnit : '')
    },
    legend: {
      show: series.length > 1,
      top: 0,
      right: 0,
      icon: 'roundRect',
      itemWidth: 12, itemHeight: 6,
      textStyle: { color: '#6b7280', fontSize: 11 }
    },
    xAxis: {
      type: 'category',
      data: props.xData,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#6b7280', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: props.yUnit || '',
      nameTextStyle: { color: '#9ca3af', fontSize: 11, padding: [0, 0, 4, -10] },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6b7280', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eef1f6', type: 'dashed' } }
    },
    series
  }
})
</script>

<style scoped>
.fp-chart-wrap { padding: 14px 16px 8px; }
.fp-chart { width: 100%; height: 260px; }
</style>
