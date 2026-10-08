import { nativeTheme, type BrowserWindow } from 'electron'
import type { Settings } from '../shared/types'
import { THEME_PRESETS } from '../shared/types'
import { launcherLog } from './core/launcherLog'
import { trackMaterialLifecycle } from './materialLifecycle'
import { createDwmFrameRestorer } from './dwmFrame'
const appliedMaterial = new WeakMap<BrowserWindow, () => void>()

/** 当前主题的界面背景色（用于窗口底色兜底，避免非透明窗口露出黑底）。 */
export function themeBackground(settings: Settings): string {
  const colors = settings.theme === 'custom'
    ? settings.custom.colors
    : (THEME_PRESETS[settings.theme] ?? THEME_PRESETS.transparent).colors
  return /^#[0-9a-f]{6}$/i.test(colors.bg) ? colors.bg : '#1c1c1c'
}

/** DWM 的染色也要跟随应用主题，否则浅色系统会在暗色页面下叠一层白灰色。 */
export function applyNativeAppearance(window: BrowserWindow | null, settings: Settings): void {
  const light = settings.theme === 'macos' || settings.theme === 'blue-white' || settings.theme === 'white-pink' ||
    (settings.theme === 'custom' && /^#[0-9a-f]{6}$/i.test(settings.custom.colors.bg) &&
      parseInt(settings.custom.colors.bg.slice(1, 3), 16) * .299 +
      parseInt(settings.custom.colors.bg.slice(3, 5), 16) * .587 +
      parseInt(settings.custom.colors.bg.slice(5, 7), 16) * .114 > 128)
  const theme = light ? 'light' : 'dark'
  const changed = nativeTheme.themeSource !== theme
  if (changed) nativeTheme.themeSource = theme
  if (!window || window.isDestroyed()) return
  if (process.platform === 'win32' && !appliedMaterial.has(window)) {
    try {
      // 窗口底色跟随主题，不透出黑底；亚克力可用时由材质覆盖。
      window.setBackgroundColor(themeBackground(settings))
      window.setBackgroundMaterial('acrylic')
      appliedMaterial.set(window, trackMaterialLifecycle(window, launcherLog, createDwmFrameRestorer(window)))
    }
    catch (error) { launcherLog(`Desktop acrylic unavailable: ${String(error)}`) }
  }
  else if (process.platform === 'win32') window.setBackgroundColor(themeBackground(settings))
  else if (changed) appliedMaterial.get(window)?.()
}
