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
    "date": "2026-10-10 03:36",
    "changes": [
      "镜像不再被自己限频：BMCLAPI 并发压到 8 并错峰 100ms（对齐 PCL 的「减少 BMCLAPI 请求频率」），高频请求的 403/429 不再把安装拖死",
      "安装器依赖（NeoForge 的 cli-utils 等）可复用游戏目录 libraries 里已有的相同文件：先用 PCL/HMCL 装过的话，本启动器不再重下",
      "差网络不再卡死：官方源失败后自动回退 BMCLAPI 镜像（同一份文件、仍校验哈希），maven.neoforged.net 这类国内不通的主机也能下",
      "停滞判定放宽到 30 秒无数据才断开（对齐 PCL，原先 15 秒），与「慢但不断」的连接共存",
      "游戏目录改为 PCL 风格：默认位置变为 %APPDATA%\\.minecraft，可与官方启动器 / PCL 共用同一份游戏数据",
      "启动器私有数据（依赖原版、自定义图标、实例缩略图、整合包缓存）移到 %APPDATA%\\QueAN\\game\\ 下，游戏目录只保留标准结构",
      "老数据首次启动自动迁移：原 .quean 目录整体改名为 .minecraft，已装版本/模组/存档原样保留；若已有 .minecraft 则不覆盖，旧目录继续登记在列表中",
      "移除点击左上角头像触发的彩蛋互动（头像恢复为静态图片，点击无反应）",
      "下载更耐差网络：过慢断线阈值对齐 PCL（约 1KB/s 才断，原先 16KB/s），换源阈值降到 32KB/s，重试次数提高到 6 次",
      "镜像失败冷却放缓：连续 3 次失败才冷、最长 60 秒（原先 2 次 / 最长 10 分钟）",
      "任务栏图标显示总体下载进度",
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
