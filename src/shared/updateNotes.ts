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
    "date": "2026-10-09 11:24",
    "changes": [
      "首个正式版本：基于 KAMUCL v1.1.15（MIT）二次开发（二开）的 QueAN，保留原始版权与许可声明；本项目为第三方非官方版本",
      "社区资源：中文关键词直接列出全部相关模组（双源去重、按下载量排序），无需输入完整名称",
      "社区资源默认「全部版本 / 全部加载器」，不再自动跟随当前实例",
      "外置登录新增 LittleSkin 预设，默认选中并自动填入 API 地址",
      "下载提速：使用设置的线程数（默认 64）、1MB 分片，并按吞吐自适应增加连接",
      "启动器更新改动：改用 Release API 自带的 SHA256 摘要、多个 GitHub 镜像分片并行，并在更新完成后清理安装目录残留",
      "内置 Mod 中文名总表（含 PCL 帮助库词条），冷门模组也能用中文名搜到"
    ]
  }
]

export const latestUpdateNote = (): UpdateNote => updateNotes[0]
