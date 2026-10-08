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
