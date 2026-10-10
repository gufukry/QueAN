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
    "date": "2026-10-10 09:00",
    "changes": [
      "首个正式版本：基于 KAMUCL v1.1.15（MIT）二次开发的 QueAN，保留原始版权与许可声明；本项目为第三方非官方版本",
      "中文社区资源：中文关键词直接列出全部相关模组，无需输入完整名称",
      "下载提速与容错：多线程分片下载、国内镜像自动兜底，差网络也能装完",
      "多游戏文件夹与实例隔离，支持整合包导入安装",
      "外置登录支持 LittleSkin 预设"
    ]
  }
]

export const latestUpdateNote = (): UpdateNote => updateNotes[0]
