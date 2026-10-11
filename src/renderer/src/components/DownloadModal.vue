<script setup lang="ts">
/**
 * 统一下载弹窗：任何下载任务开始时自动弹出，集中显示阶段、进度、速度与剩余时间。
 * 数据源与「下载中心」一致（store.tasks），因此版本安装、整合包、模组、存档导入、
 * Java 下载、启动器更新都会出现在这里，不需要各自单独实现。
 */
import { computed, watch } from 'vue'
import { store } from '../store'
import { cancelTask } from '../api'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()

const running = computed(() =>
  store.tasks.filter((t) => t.status === 'running' || t.status === 'paused' || t.status === 'cancelling')
)
const finished = computed(() => store.tasks.filter((t) => t.status === 'error').slice(0, 3))
const fmtSpeed = (bps?: number) =>
  !bps || !Number.isFinite(bps) || bps <= 0
    ? ''
    : bps >= 1048576
      ? (bps / 1048576).toFixed(1) + ' MB/s'
      : Math.round(bps / 1024) + ' KB/s'
const fmtEta = (s?: number) => {
  if (!s || !Number.isFinite(s) || s <= 0) return ''
  if (s < 60) return s + ' 秒'
  const m = Math.floor(s / 60)
  return m + ' 分' + Math.round(s - m * 60) + ' 秒'
}
const percent = (v: number) => Math.max(0, Math.min(100, Math.round((Number.isFinite(v) ? v : 0) * 100)))

// 全部完成后 2.5 秒自动收起
let closeTimer: ReturnType<typeof setTimeout> | undefined
watch(
  () => running.value.length,
  (n) => {
    clearTimeout(closeTimer)
    if (n === 0 && props.open) closeTimer = setTimeout(() => emit('close'), 2500)
  }
)
</script>

<template>
  <Teleport to="body">
    <Transition name="dl-pop">
      <div v-if="open" class="dl-mask" @pointerdown.self="emit('close')">
        <section class="dl-modal" role="dialog" aria-label="下载进度">
          <header class="dl-head">
            <div>
              <p class="dl-eyebrow">QueAN · 下载</p>
              <h3>{{ running.length ? `正在处理 ${running.length} 项任务` : '任务已完成' }}</h3>
            </div>
            <button class="icon-btn" aria-label="关闭" @click="emit('close')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12"/></svg>
            </button>
          </header>

          <div class="dl-body">
            <article v-for="t in running" :key="t.id" class="dl-item">
              <div class="dl-item-head">
                <strong>{{ t.title }}</strong>
                <span class="dl-pct" v-if="!t.indeterminate">{{ percent(t.progress) }}%</span>
              </div>
              <div class="dl-bar" :class="{ ind: t.indeterminate }">
                <span :style="t.indeterminate ? {} : { width: percent(t.progress) + '%' }"></span>
              </div>
              <div class="dl-item-foot">
                <span class="dl-text">{{ t.text }}</span>
                <span class="dl-nums">
                  <i v-if="fmtSpeed(t.speed)">{{ fmtSpeed(t.speed) }}</i>
                  <i v-if="fmtEta(t.etaSeconds)">剩余 {{ fmtEta(t.etaSeconds) }}</i>
                  <button class="dl-cancel" @click="cancelTask(t.id)">取消</button>
                </span>
              </div>
            </article>

            <p v-if="!running.length" class="dl-empty">没有正在进行的下载任务。</p>

            <div v-if="finished.length" class="dl-errors">
              <p v-for="t in finished" :key="t.id" class="dl-error">{{ t.title }}：{{ t.error || '失败' }}</p>
            </div>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dl-mask { position: fixed; inset: 0; z-index: 90; display: flex; align-items: center; justify-content: center;
  padding: var(--space-5); background: var(--mask, rgba(0,0,0,.45)); backdrop-filter: blur(6px); }
.dl-modal { width: min(560px, 100%); max-height: min(76vh, 640px); overflow: auto;
  background: var(--card-solid, var(--card)); border: 1px solid var(--border); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg); padding: var(--card-pad); }
.dl-head { display: flex; align-items: flex-start; justify-content: space-between; gap: var(--space-4); margin-bottom: var(--space-4); }
.dl-eyebrow { margin: 0 0 4px; font-size: var(--text-xs); color: var(--text-dim); }
.dl-head h3 { margin: 0; font-size: var(--text-lg); }
.dl-body { display: flex; flex-direction: column; gap: var(--space-4); }
.dl-item { padding: var(--space-4); border: 1px solid var(--border); border-radius: var(--radius-md); background: var(--card-2, var(--card)); }
.dl-item-head { display: flex; align-items: baseline; justify-content: space-between; gap: var(--space-3); font-size: var(--text-sm); }
.dl-item-head strong { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dl-pct { font-size: var(--text-lg); font-weight: 700; font-variant-numeric: tabular-nums; }
.dl-bar { height: 6px; margin: 10px 0 8px; border-radius: 99px; background: var(--hover); overflow: hidden; }
.dl-bar > span { display: block; height: 100%; border-radius: 99px; background: var(--accent-grad);
  transition: width var(--motion-normal) var(--ease-out); }
.dl-bar.ind > span { width: 40%; animation: dl-slide 1.2s ease-in-out infinite; }
@keyframes dl-slide { 0% { margin-left: -40% } 100% { margin-left: 100% } }
.dl-item-foot { display: flex; align-items: center; justify-content: space-between; gap: var(--space-3); font-size: var(--text-xs); color: var(--text-dim); }
.dl-text { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dl-nums { display: inline-flex; align-items: center; gap: var(--space-3); flex: none; }
.dl-nums i { font-style: normal; font-variant-numeric: tabular-nums; }
.dl-cancel { border: 1px solid var(--border); background: none; color: var(--text); font: inherit; font-size: var(--text-xs);
  padding: 4px 10px; border-radius: var(--radius-sm); cursor: pointer; }
.dl-cancel:hover { background: var(--hover); }
.dl-empty { margin: 0; color: var(--text-dim); font-size: var(--text-sm); }
.dl-errors { display: grid; gap: 6px; }
.dl-error { margin: 0; color: var(--danger, #ff7278); font-size: var(--text-xs); }
.dl-pop-enter-active, .dl-pop-leave-active { transition: opacity var(--motion-normal) var(--ease-out); }
.dl-pop-enter-from, .dl-pop-leave-to { opacity: 0; }
</style>
