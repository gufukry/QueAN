import faceUrl from './assets/splash-face.png'
import { canAssembleBoot, type BootState } from '@shared/startup'
import './splash.css'

const bridge = window.queanSplash
const canvas = document.querySelector<HTMLCanvasElement>('#glass')!
const ctx = canvas.getContext('2d', { alpha: true })!
const caption = document.querySelector<HTMLSpanElement>('#stage')!
const face = new Image()
let state: BootState = { completed: [], ready: false }
let start = 0, raf = 0, assembled = false
const reduced = matchMedia('(prefers-reduced-motion: reduce)').matches
const labels = ['读取配置', '加载账户', '扫描游戏实例', '加载首页图片、Java 与皮肤', '准备首帧']
const FADE_MS = 480
const HALO_MS = 900
const MIN_VISIBLE_MS = 520
const HOLD_MS = 160

function layout() {
  const dpr = Math.min(devicePixelRatio || 1, 2)
  canvas.width = Math.round(innerWidth * dpr)
  canvas.height = Math.round(innerHeight * dpr)
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  ctx.imageSmoothingEnabled = true
}
function logoSize() {
  return Math.max(96, Math.min(184, Math.min(innerWidth, innerHeight) * 0.28))
}
const easeOut = (t: number) => 1 - Math.pow(1 - t, 3)

function frame(now: number) {
  if (!start) start = now
  const elapsed = now - start
  const t = reduced ? 1 : Math.min(1, elapsed / FADE_MS)
  const e = reduced ? 1 : easeOut(t)
  const size = logoSize(), cx = innerWidth / 2, cy = innerHeight / 2
  ctx.clearRect(0, 0, innerWidth, innerHeight)
  if (!reduced) {
    // 淡出的柔和光晕，随 logo 淡入向外扩散。
    const hp = Math.min(1, elapsed / HALO_MS)
    if (hp < 1) {
      const r = size * (0.75 + 0.85 * hp)
      const g = ctx.createRadialGradient(cx, cy, size * 0.25, cx, cy, r)
      g.addColorStop(0, `rgba(10,132,255,${0.30 * (1 - hp)})`)
      g.addColorStop(1, 'rgba(10,132,255,0)')
      ctx.fillStyle = g
      ctx.beginPath()
      ctx.arc(cx, cy, r, 0, Math.PI * 2)
      ctx.fill()
    }
  }
  const drawn = size * (reduced ? 1 : 0.94 + 0.06 * e)
  ctx.globalAlpha = e
  ctx.drawImage(face, cx - drawn / 2, cy - drawn / 2, drawn, drawn)
  ctx.globalAlpha = 1
  if (!assembled && canAssembleBoot(state) && elapsed >= MIN_VISIBLE_MS && t >= 1) {
    assembled = true
    setTimeout(() => bridge.assembled(), reduced ? 0 : HOLD_MS)
    return
  }
  raf = requestAnimationFrame(frame)
}

const offState = bridge.onState(next => {
  state = next
  caption.textContent = state.ready ? '准备就绪' : `${labels[Math.min(4, state.completed.length)]}…`
})
const offReveal = bridge.onReveal(() => {
  document.body.classList.add('leaving')
  document.body.addEventListener('transitionend', () => bridge.finished(), { once: true })
})
face.onload = () => {
  document.body.dataset.motion = reduced ? 'system-reduced' : 'full'
  layout()
  bridge.ready()
  raf = requestAnimationFrame(frame)
}
face.onerror = () => bridge.failed('启动 logo 资源无法加载')
face.src = faceUrl
window.addEventListener('resize', layout)
window.addEventListener('error', () => bridge.failed('启动动画渲染失败'))
window.addEventListener('unload', () => {
  cancelAnimationFrame(raf)
  offState()
  offReveal()
  window.removeEventListener('resize', layout)
})
