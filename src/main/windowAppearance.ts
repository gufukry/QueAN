import type { BrowserWindowConstructorOptions } from 'electron'

/**
 * 主窗口的固定外观基线。
 *
 * renderer 只负责绘制带 alpha 的界面层；Windows 11 22H2+ 的桌面模糊由
 * DWM acrylic material 完成，旧版 Windows 则稳定降级为透明窗口 + 半透明色板。
 */
export function windowAppearance(
  platform: NodeJS.Platform = process.platform
): Pick<
  BrowserWindowConstructorOptions,
  | 'width'
  | 'height'
  | 'minWidth'
  | 'minHeight'
  | 'frame'
  | 'transparent'
  | 'backgroundColor'
  | 'backgroundMaterial'
  | 'roundedCorners'
  | 'hasShadow'
  | 'thickFrame'
  | 'titleBarStyle'
  | 'trafficLightPosition'
  | 'vibrancy'
  | 'visualEffectState'
> {
  return {
    width: 1360,
    height: 860,
    minWidth: 820,
    minHeight: 560,
    frame: platform === 'darwin',
    titleBarStyle: platform === 'darwin' ? 'hiddenInset' : undefined,
    trafficLightPosition: platform === 'darwin' ? { x: 16, y: 16 } : undefined,
    vibrancy: platform === 'darwin' ? 'sidebar' : undefined,
    visualEffectState: platform === 'darwin' ? 'active' : undefined,
    // DWM 为普通 HWND 合成桌面 Acrylic；transparent:true 会创建 layered
    // window，绕过该合成路径并影响 Windows 原生缩放/最大化。
    // WebContents 仍通过 backgroundColor 的 alpha 保持透明。
    // macOS 必须同时清空 NSWindow 底色，才能透出 behind-window vibrancy。
    // hiddenInset 保留系统红绿灯；不要用整个窗口 opacity 淡化界面内容。
    // Windows 用不透明窗口 + 主题底色（不做圆角），避免任何黑底/黑边。
    // thickFrame/hasShadow 必须为 true：Electron 明确「thickFrame=false 会连窗口阴影与
    // 系统窗口动画一起去掉」，最小化/还原就没有 Windows 那个收到任务栏的动画了。
    // 保留它们后仍是 frame:false 的无边框自绘窗口，只是恢复了系统动画/阴影/吸附。
    transparent: platform !== 'win32',
    backgroundColor: '#00000000',
    backgroundMaterial: undefined,
    roundedCorners: true,
    hasShadow: true,
    thickFrame: true
  }
}
