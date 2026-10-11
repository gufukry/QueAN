/**
 * 内置更新日志：下拉面板数据源（无网络依赖）
 */
export interface UpdateNote {
  version: string
  /** 发布时间：YYYY-MM-DD HH:mm（24 小时制） */
  date: string
  changes: string[]
}

/** 按版本倒序；latest 为当前版本。只写版本级重点，不记流水账 */
export const updateNotes: UpdateNote[] = [
  {
    "version": "1.0.0",
    "date": "2026-10-11 20:26",
    "changes": [
      "首个正式版本，面向国内网络的 Minecraft Java 版启动器",
      "全新浅色苹果风界面，多套主题可选",
      "中文社区资源与多线程分片下载，国内镜像自动兜底",
      "实例隔离、整合包导入，支持正版／离线／外置登录",
      "启动器窗口可选「一直居中」或「可自由移动」",
      "自带更新：下载并通过 SHA256 校验后自动安装并重启"
    ]
  }
]

export const latestUpdateNote = (): UpdateNote => updateNotes[0]
