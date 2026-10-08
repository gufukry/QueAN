/**
 * 内置更新日志：下拉面板数据源（无网络依赖）
 */
export interface UpdateNote {
  version: string
  /** 发布时间：YYYY-MM-DD HH:mm（24 小时制） */
  date: string
  changes: string[]
}

/** 按版本倒序；latest 为当前版本 */
export const updateNotes: UpdateNote[] = [
  {
    "version": "1.0.2",
    "date": "2026-10-08 12:09",
    "changes": [
      "云更新测试版：产物名不带版本号（QueAN.exe），验证自动更新链路"
    ]
  },
  {
    "version": "1.0.1",
    "date": "2026-10-08 11:44",
    "changes": [
      "首页改为 macOS 仪表盘：顶部整条启动栏 + 下方左右两栏，新增快捷入口方块",
      "移除内置启动插画与右下角创作者卡片；启动动画改为简洁淡入",
      "窗口改为方角、停用会发黑的 DWM 亚克力；最小化/最大化/关闭回到右上角",
      "修复浅色 macOS 主题被回退成深色导致的界面偏暗、控件不匹配",
      "首页窄窗口自动合并分栏；最小窗口放宽到 820×560",
      "新增 build/icon.ico，Windows 打包不再因图标转换内存不足而失败"
    ]
  },
  {
    "version": "1.0.0",
    "date": "2026-10-08 00:03",
    "changes": [
      "全新品牌 QueAN：应用名、窗口标题、数据目录与内部标识全面更名",
      "界面默认采用 macOS 风格主题：系统蓝强调色、浅灰玻璃与系统字体",
      "非 macOS 平台窗口控制改为左上角红黄绿交通灯，窗口圆角更柔和",
      "基于 KAMUCL v1.1.15 二开，保留账号、实例、模组、联机与更新等全部功能"
    ]
  }
]

export const latestUpdateNote = (): UpdateNote => updateNotes[0]
