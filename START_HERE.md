# START HERE — KAMUCL 1.1.15 Windows

## What this package contains

- Project: KAMUCL，Minecraft 启动器。
- Deliverable: 本批 Windows x64 服务器地址隐私、版本分类、披风加载及启动合成与导航残影修复。
- Packaged artifact: KAMUCL-1.1.15.exe、Windows 紧凑 ZIP、展开 ZIP；精确摘要见 CURRENT_STATUS.md。
- Intended receiver: 使用者及接续开发者；用户自行提供自己的账号和游戏目录。

## Prerequisites

- Operating system: Windows x64；本轮实际桌面验收环境为 Windows 11 10.0.26200。
- Runtime/tool versions: 成品包含 Electron 44.3.0；开发使用 Node.js 24、npm 锁定依赖、JDK 17+，原生 Windows 工具按构建脚本要求准备。
- Required external services: 登录和游戏下载按所选服务联网；交接包许可检查不需要登录。
- Secrets that the receiver must provide separately: 自己的登录授权；不包含发送者凭据、账号配置、存档或整合包。

## Setup

1. 核对公开 SHA256SUMS.txt；ZIP 解压至普通目录，运行 EXE。展开 ZIP 运行其中 KAMUCL.exe。
2. 开发源码使用 `npm ci`、`node scripts/build-bridge.cjs`；生产构建为 `npm run build`。
3. 设置自己的默认下载位置并登录自己的账号；旧实例无需迁移。

## Verify

交接包中精确的便携验证命令数组为 `["node", "source/scripts/check-licenses.cjs"]`：

```text
node source/scripts/check-licenses.cjs
```

Expected result: `License check passed`，退出 0。此命令仅验证许可，不能替代真实桌面、账号、游戏、动画或功能验收。本版全量1208项中1207通过、1平台专属跳过；完整源码验证入口为 `npm test`、`npx tsc --noEmit`、`npm run license:check`；成品及独立评审见 docs/validation-1.1.15/README.md。

## Use the deliverable

1. 服务器默认“地址已隐藏”，点击眼睛才显示；切换服务器、离开页面或失焦后重新隐藏。
2. 游戏版本的已安装列表使用“管理分类”和各实例的归类入口；“收藏”独立显示，删除分类不删除实例。
3. 披风失败时提供刷新/错误提示；更换后同步缩略图和预览。截图中具体账号披风未取得真实服务验证。

## Important paths

- Source: 交接包 source/；独立源码 ZIP 为原始 Git blob 字节，不包含工作区未提交文件。
- Validation: source/scripts/ 及 source/docs/validation-1.1.15/。
- Artifact: _handoff/artifacts/；外部附件 SHA256 在公开 SHA256SUMS.txt。
- Current state: CURRENT_STATUS.md；最终提交、标签和附件身份由外部 DELIVERY-1.1.15.json 记录，避免归档自引用。

## Known machine-specific assumptions

最小外框配置为 960×620；本机 125% 缩放下实际最小外框为 962×623、内容区 961×622，不冒称精确 960×620 内容区已测。原灰色主题反馈没有在本机复现。Mac ARM64 1.1.14 候选的游戏/帧率失败仍保留，本批 Windows 放行不替代 Mac 验收。资源优化仍停止，Intel/Linux/鸿蒙不在本批成品范围。

---

# Historical START HERE — KAMUCL 1.1.14 Mac ARM64 接续候选

2026-10-06 14:28（Asia/Hong_Kong）补充：第三轮原生运行 37421862931 的 DMG 四主题和实际 900 高度通过；APP 原始皮肤行走录屏为 12 FPS，收藏 APP/DMG 的世界及显示恢复仍失败。页面重载身份检查的 QA 修复单独复验，不能替代这些失败或完整放行；最终成品身份与哈希以交付清单为准。

2026-10-06 13:52（Asia/Hong_Kong）：用户恢复 Mac ARM64，资源占用优化仍停止。Windows x64 / Mac ARM64 继续共用 Electron 44.3.0、当前界面、素材及收藏模组安装修复；Mac 陶瓦启动修复使本批递增至 1.1.14。Intel、Linux、鸿蒙未恢复。1.1.13 Windows 已公开发布，原附件与标签保持不变。

Mac 使用 ZIP 中的 KAMUCL.app，或挂载 DMG 后复制到自己的应用目录；系统要求 macOS 13 或以上、Apple Silicon。现阶段是候选，尚未满足完整放行：原生构建、启动、更新、目录和陶瓦检查通过，DMG 四主题界面通过，但最新 APP 图库及操作坐标验收须复验，1.20.1 收藏安装的游戏世界检查在虚拟图形测试机仍未通过。具体证据和历史失败见 `docs/validation-1.1.14/README.md`。ad-hoc 签名校验不等于 Developer ID 签名或 Apple 公证，人工听感尚未覆盖。

接收者自行登录并选择自己的游戏目录。源码开发需要 Node.js 24、锁定依赖及 JDK 17+；使用 `npm ci`、`node scripts/build-bridge.cjs`、`npm test`、`npx tsc --noEmit`、`npm run license:check`、`npm run build`。交接包便携验证命令为 `["node", "source/scripts/check-licenses.cjs"]`，仅验证许可，不替代原生功能或游戏验收。源码、交接包不含用户凭据、图片、整合包、存档、依赖缓存或 `pelican-bicycle.html`。每份成品与真实源码身份、SHA256、最终验收和未覆盖项由对应交付清单注明，不用旧包或跨运行证据拼成通过。

---

# Historical START HERE — KAMUCL 1.1.13

2026-10-06 11:54（Asia/Hong_Kong）：本次交付范围已按用户最新要求缩为 Windows x64，继续完成收藏模组漏装修复。Mac ARM64 制作暂停，不提供本版 Mac 附件；资源优化和完整占用对照停止，已有改动保留，不宣称整机资源收益。下方此前双平台及资源门控计划保留为历史，不能作为当前放行规则。

Windows 使用 `KAMUCL-1.1.13.exe` 或紧凑 `KAMUCL-1.1.13-windows-x64.zip`；无需便携包装器时可用 `KAMUCL-1.1.13-windows-x64-unpacked.zip`。接收者自行登录并选择自己的游戏目录。原成品对应产品输入 `25eada75831eb4567ca629149d13a7c39f91e67b`，最终提交仅增加 QA、文档及暂停 Mac 自动任务的配置，不能描述为重新构建的成品。

验证入口为 `npm test`、`npx tsc --noEmit`、`npm run license:check`、`npm run build`，收藏/界面/便携原生证明和独立评分见版本交付记录。交接包的便携验证命令为 `["node", "source/scripts/check-licenses.cjs"]`，成功只证明许可检查，不能替代真实游戏、原始画面或功能验收。源码开发需要 Node.js 24、锁定依赖及 JDK 17+；Electron 保持 44.3.0。源码包和交接包排除用户账号、凭据、图片、整合包、存档、依赖缓存及 `pelican-bicycle.html`。

---

## 此前计划（历史）

2026-10-05 20:09（Asia/Hong_Kong）：当前工作为收藏模组漏装修复和资源占用优化，仅交付 Windows x64 / Mac ARM64，Electron 保持 44.3.0。实现及局部回归已完成，最终原生成品、十组基线与十组交替对照、独立三项评分正在验证；尚未发布 1.1.13。下方 1.1.12 与 1.1.11 保留为历史，不能替代本轮验收。

源码验证：`npm ci`、`node scripts/build-bridge.cjs`、`npx tsc --noEmit`、`npm test`、`npm run license:check`、`npm run build`。原生收藏、资源与外观验证入口及真实服务/夹具区分见 `docs/validation-1.1.13/README.md`。原始素材、用户目录、全部历史计数及未提交文件保留；源码和交接包排除个人资料、整合包、世界及 `pelican-bicycle.html`。

收藏查询失败必须重试或明确跳过，零项必须明确选择仅安装基础实例；完成摘要核对实际 MOD 文件及哈希。资源收益和弃用候选见 `docs/validation-1.1.13/OPTIMIZATIONS.md`，算法微测试不能冒充完整应用内存结论。macOS 13+，ARM64 原生；ad-hoc 签名不等于 Developer ID 签名及公证。

---

# Historical START HERE — KAMUCL 1.1.12

2026-10-05 16:51（Asia/Hong_Kong）：当前工作范围已由用户改为整合包文件名兼容与默认下载位置修复，只制作 Windows x64 和 Mac ARM64。下方 1.1.11 记录保留为历史，不是本轮发布的验收结论。当前成品与最终独立评分以 1.1.12 交付回执为准；完整多平台资格及历史未覆盖项不因本轮限定修复转为通过。

默认下载位置入口：「设置 → 启动器设置 → 下载 → 默认下载位置」。修改后新游戏版本、整合包和共享下载使用该目录，旧实例和存档不迁移。已接受的任务保持原目标；系统临时压缩包缓存仍可能使用系统盘。

源码验证：`npm ci`、`node scripts/build-bridge.cjs`、`npx tsc --noEmit`、`npm test`、`npm run license:check`、`npm run build`。成品验证入口见 `docs/validation-1.1.12/README.md`；交接包的便携验证命令为 ["node", "source/scripts/check-licenses.cjs"]，只验证许可，不替代真实成品测试。

本轮保留用户图片、收藏、计数、设置和未提交文件。个人账号、密钥、整合包、存档及 `pelican-bicycle.html` 不进入发布包。Mac ARM64 的系统要求为 macOS 13 或以上，ad-hoc 本地签名不等于 Developer ID 签名及公证。

---

# 历史：KAMUCL 1.1.11 接续候选

2026-10-04 16:22（Asia/Hong_Kong）：验收观察器最终冻结检查为 994 项，993 通过、0 失败、1 项平台专属跳过，原耗时 170461.1027 ms；类型、许可证、语法及差异格式检查通过。本批仅改 QA 与文档，实际公共产品和 Windows 成品来源仍为 85d811f27e2bd119e2d93ed6583a847b9b93b425，不重标已验证成品。Windows 三包冷／热启动、中文路径干净解压及四主题实际交互已独立通过；599 个步行和 916 个队列原帧已逐项核对，人工听感及完整矩阵仍未覆盖。

Mac 85 两架构实际游戏正常退出、ARM APP／DMG 限定原生动画已通过，752 完整帧／792 回调保留；Intel 图形失败和两架构安装观察器 Proxy 序列化首错原样保留。只读 DTO 投影保留唯一实例、文件哈希、完整目标及计划条件。Linux 85 的实际 ASAR、工具及 X11 验证通过；更新测试停于正常被消费的通知文件及早期页面上下文读取，原失败不改写。新观察器要求本次持久拒绝日志、精确失败事务和实际改动哈希，并等待所属页面真实就绪；退出、销毁和同编号复用永久拒绝，不重复 IPC、不放宽原截止。新版 Mac 安装与 Linux 完整更新仍待下一原生实际执行。

完整平台资格、三项至少 9 分评分及鸿蒙原生游戏链仍未完成，没有正式 v1.1.11 标签或 Release；最终候选交付与交接文件按各自实际源码、成品和证据身份绑定。


2026-10-04 15:46（Asia/Hong_Kong）：上述物理归档修复和 Mac 真实目标选择已完成最终冻结检查：984 项，983 通过、0 失败、1 项平台专属跳过，原耗时 174065.1826 ms；类型、许可证、语法及差异格式检查通过。实际 Electron 归档合同记录保留，Windows 本机符号链接 EPERM 的未覆盖范围单列；此合同只证明限定归档边界，不替代 Linux 完整更新交易或原生桌面。新产品成品和全部原生验证仍须按下一提交重构建。


2026-10-04 15:42（Asia/Hong_Kong）：本轮确认并修复 Linux 更新的物理 ASAR 校验与回退备份复制缺陷，实际 Electron 44.3.0 私有合同已验证原缺陷和修复后的归档字节一致；不改全局归档模式。Mac 安装验收改为真实坐标选择联机验证实例，等待对应 1.20.1／Fabric 请求、文件哈希与目标目录一致，兼容性规则和原时限保持不变。完整检查与所有桌面平台重构建尚待本批最终冻结执行，不以旧成品替代新产品提交。

原 b596 原生记录保持原样：Mac 两架构真实 Demo 正常退出和存档通过，Intel WebGL 未通过；Linux 实际窗口控制已通过，更新验收遇到物理归档读取首错。本轮不回填旧失败，未具备完整原生桌面、人工听感及鸿蒙游戏链的证据，不创建正式版本标签或 Release。


2026-10-04 15:10（Asia/Hong_Kong）：本批最终冻结公开检查 979 项（978 通过、0 失败、1 项平台专属跳过），类型及许可证通过。源码归档使用提交原始 Git blob，拒绝未提交源码；旧 d7 工作树换行归档的严格原字节失败保留。Linux ASAR 路径映射和 Mac 安装观察器事务仅属验收修正，独立复核通过；新的原生执行、最终归档及运输验证需要各自实际回执，不能引用旧包证明未来提交。

Windows 产品仍来自 2d814479，后续 QA／归档／文档差异明确单列。Mac d7 ARM 的限定原生动画、游戏和工具通过；Intel WebGL 与真实游戏 GPU 超时仍失败。Linux 已有两架构原生包及实际服务生命周期，尚无全套硬件桌面证据。鸿蒙原生游戏链、SDK 编译、签名及设备仍未完成。没有正式 v1.1.11 标签或 Release；最终交接外层文档列出真实通过范围、全部摘要和未覆盖项。

2026-10-04 14:33（Asia/Hong_Kong）：本次验收收尾公开测试 968 项（967 通过、0 失败、1 项跳过），类型及许可证通过。Windows 四主题实际操作与原始帧已独立核对，成品来源仍为产品提交 `2d814479b22b8778c80bbbdf8b04421a81cf9c47`；后续仅 QA、测试和文档变化单列，不改写成品身份。Linux 主程序入口和 Mac 点击观察器的确定验收缺陷已修正，工具失败增加真实状态与受控清理记录，新的原生运行尚待提交后核对。

最终交接包根目录 START_HERE/CURRENT_STATUS 和外部交付回执负责列出最终原生结果、附件摘要及源码与产品提交差别；source/保留这些源码文档和历史记录。完整平台资格、人工听感和三项独立 ≥9 分尚未完成，仍无正式 v1.1.11 标签或 Release。鸿蒙工程未完成编译签名、实机或原生游戏链。下方旧记录只能用于追溯。

本轮继续 1.1.11 的多平台实现与验收，正式公开基线仍为 1.1.10。新增 Linux 陶瓦独立服务隔离、账号页真实凭据状态、鸿蒙原生关闭与持久目录授权，以及 Mac 四主题全页面、队列、重启、公开资源下载的真实成品验证入口。新产品代码要求 Windows、Mac ARM64／Intel、Linux x64／ARM64 全部重新构建；旧成品与历史证据保留，但不能为新提交背书。

最终成品身份、原始测试结果和 SHA256 以本轮外部交付回执为准。完整验收范围为 15 页面、280 个固定接口、63 个固定功能及其全部子断言。证据协议只能检查可信采集输入的完整性和一致性，不能自证任意清单确实来自真实操作；必须结合实际原生运行、截图、原始录屏与独立评审。任何必测未覆盖、评分不足或关键缺陷都阻止正式发布。

源码构建及验证命令仍为 `npm ci`、`node scripts/build-bridge.cjs`、`npx tsc --noEmit`、`npm test`、`npm run license:check`、`npm run build`。Windows 打包 `npm run dist:win`；Mac 和 Linux 在相应原生架构分别使用 `node scripts/pack-mac.mjs <arm64|x64> --package-only` 与 `node scripts/pack-linux.cjs <arm64|x64>`。交接包记录的轻量验证命令为 ["node", "scripts/check-licenses.cjs"]，它不替代完整平台验收。

鸿蒙使用 `npm run prepare:harmonyos`、`npm run verify:harmonyos`、`npm run dist:harmonyos`。缺少合法官方 SDK、签名和设备时构建失败保持失败；固定模板故障测试与共享资源字节核对不能替代 ArkTS 编译、HAP 签名安装或 JVM／LWJGL 游戏链。用户已确认没有真实 Intel Mac，当前也没有 Ubuntu 两架构完整桌面及鸿蒙电脑的验收证据。当前差距见 `docs/validation-1.1.11/CONTINUATION.md`。

下方各轮时间戳是历史记录，不能替代本轮最终回执。

2026-10-04 09:02（Asia/Hong_Kong）当前快照：第六轮精确绑定已推送 master `69c1e89ba23a007535c77bae65052f19b6d87f58`；main `bb41072b907d8ade365e9beadc4a71ee7e69a6ca` 经 cherry-pick 保留独立历史。Mac run [37163779043](https://github.com/kamubaba-i/KAMUCL/actions/runs/37163779043) 和 Linux run [37163779037](https://github.com/kamubaba-i/KAMUCL/actions/runs/37163779037) 已结束，均不构成完整平台合格或正式发行。版本仍为 1.1.11，本批不再修改 03:59 的内置更新日志或递增版本。

Mac 两架构各 864 项为 860 通过、0 失败、4 项既有平台 skip；15 个任务中 12 成功、3 个正式 UI 失败。成功任务为 2 个打包、2 个实际 Demo 游戏、6 个限定集成、ARM DMG 的限定四主题 GUI 和 1 个独立 Intel GPU 诊断。ARM DMG 的实际可见皮肤步行、焦点和受管壁纸均通过；ARM APP 的原始 CDP 90 帧在第 55→56 帧倒退 1.618 ms，原算术 FPS 60.53927763443197、formalTimingUsable=false，保持失败。Intel APP／DMG 实际 WebGL 初始化仍失败；独立诊断收集成功也仍无画布，不能把它当产品修复。第六四个 Mac 成品实际内嵌文件为 Contents/Resources/kamucl-mac.json，sourceCommit、Electron 44.3.0、架构、最低 macOS 13 和签名均有实际核对；旧摘要的 kamucl-build.json 计划名在 summary.json 的 historicalSnapshotErrors 保留。

Linux 两架构各 864 项为 861 通过、0 失败、3 skip；六个 AppImage／DEB／tar.gz 原生包的干净解压、ELF、执行位、摘要及内嵌 source69／实际 Electron 44.3.0 身份通过。三次 Xvfb 已启动实际应用，均在 ANGLE／Mesa llvmpipe 的 WebGL 创建失败后触发 skin canvas missing，原失败和时间保留，finalizer 没有再遮蔽首错。软件驱动 blocklist 是强线索，最终根因仍待同一进程 GPU 状态诊断；不能宣称 Vulkan 驱动缺失。没有 Ubuntu 24／26 两架构的真实 GNOME／KDE／XWayland 硬件桌面主机或完整验收。

本批第七完整本机公开测试为 874 项：873 通过、0 失败、1 项 Linux 原生 shell skip，duration=117970.8989 ms，TypeScript 通过。这覆盖原冻结的 QA／合同测试；全套之后追加的 Mac 原生 BGRA 证据收集修订另经针对性校验，不能笼统称全部最终 QA 都在 874 项之前冻结。它们不能替代 Mac／Linux 原生执行。本批黑橙 general refinement 实际 GUI 已通过，包含最小窗口、1.25／1.5 缩放、最大化与原报告中的有限交互；当前黑橙 ux110 可见步行／外观另在执行，尚不预填通过。四主题 final3 可见模型／步行／壁纸仍为上一 source69 已验证结果。新 Mac SCK 与 Linux 同进程 GPU 观察器尚未原生运行，源7提交 SHA=null、nativeRunsStarted=false；Windows 产品输入未改，不因 QA／文档重包装。所有平台完整资格 false、三项评分 null、无正式 Release。

全部平台 completeQualification=false，视觉／交互／动效分别为 null；没有 v1.1.11 正式标签或公开 Release。第六源码／旧交接的build/icon.png 与 build/icon-512.png 两个构建图标漏收已由独立审计记录，下一交接按根文档与完整 source prefix 闭包核对，不能以旧包运输验证冒充源码闭包完成。第五轮旧世界 SHA 缺失继续保留；第六真实正常退出后的新 Demo 世界 SHA 单独记录，不补造旧世界。用户图片、设置、收藏、历史计数、私有账号／世界／整合包和未提交文件保留，不纳入公开包。以下旧快照只描述各自记录时点。

2026-10-04 08:02（Asia/Hong_Kong）第五轮历史快照：第五轮绑定已推送提交 `5167500`，Mac run `37160588297` 和 Linux run `37160588146` 均已结束。Mac 两架构各 857 项为 853 通过、0 失败、4 项既有平台 skip；2 个打包、2 个实际 Demo 游戏和 6 个限定集成任务成功，4 个 APP／DMG UI 任务失败。Linux 两架构各 857 项为 854 通过、0 失败、3 skip，六个三格式包的原生干净解压与包检查通过；三次 Xvfb 预检因缺少 `xdpyinfo` 失败，随后 finalizer 的 `scandir out ENOENT` 遮蔽首错，应用未启动。这是 QA 前置失败，不能算应用失败或桌面通过。

本批已冻结 QA 和包装身份修订，Windows 产品构建输入无变化。Windows 最新全套为 864 项：863 通过、0 失败、1 项 Linux 原生 shell skip，TypeScript 通过；四主题最新 final3 实际 GUI 的可见模型、原帧时间、真实像素变化、焦点及受管壁纸解码断言通过，仍属限定本机回归。Mac／Linux 新内嵌 sourceCommit／runtimeVersion 身份必须在本次提交后的新原生构建中验证，目前尚未重建。本批源码 SHA 和 Delivery 外部回执须在提交后更新，不能给第五轮旧包绑定未来提交。全部平台完整资格仍为 false，视觉／交互／动效评分均为 null；没有 v1.1.11 正式标签或 Release。

2026-10-04 07:04（Asia/Hong_Kong）更新：第四轮候选 `e0b1210` 原生 CI 均结束。Mac `37158774374` 两架构各 855 项为 851 通过、0 失败、4 项既有系统 skip；两个打包及六个限定集成任务成功，4 个 UI 和 2 个游戏任务失败。双架构 ZIP／DMG 已实际生成，Electron 44.3.0、最低 macOS 13、ad-hoc 签名明确记录；不代表完整一致性通过。ARM UI 缺独立输出目录、游戏任务依赖跨任务窗口探针属于 QA 前置问题，已修正。Intel 原截图、30 个无画布样本及三轮 EGL／GPU 错误确认 3D 初始化真实失败，根因仍需原进程诊断。Linux `37158774429` x64 为 852 通过、0 失败、3 skip，三格式实际生成后因 builder 文件名映射失败；ARM64 为 851 通过、1 失败、3 skip，打包跳过。已改为精确命名和私有目录独占发布，取消夹具使用真实未完成传输门控，保留原时间线。最新 Windows 全套 857 项为 856 通过、0 失败、1 项原有 Linux skip；最终 tar 发布调用另经语法及 4 项专项复验。此次产品输入及 Windows 成品未变，QA／Linux 包装变化必须原生重跑。独立修正复核通过，但所有平台完整门控及三项评分仍未完成；没有正式标签或 Release。

2026-10-04 06:30（Asia/Hong_Kong）更新：第三轮 `2b1e6f1` 的 Mac run `37157548139` 与 Linux run `37157548122` 均完整结束为 failure，没有取消或死锁。Mac 两架构各 853 项为 846 通过、3 失败、4 项既有系统限定 skip；失败为测试临时目录别名 `/var` 与产品正确返回的 `/private/var` 规范路径不同。Linux x64 为 850 通过、0 失败、3 skip 后进入生产构建，但旧打包检查要求官方 Electron 44.3.0 已不含的 `libEGL.so`；ARM64 为 849 通过、1 失败、3 skip，合成 Windows classifier 被正确的 ARM 安全规则拒绝。已修正测试根目录和原生库夹具，按两份官方 ZIP／SHA 建立准确的运行时清单，保留 ELF、执行位、ICD、资源和许可检查。Windows 最新全套 855 项为 854 通过、0 失败、1 项原有 Linux skip。此次新增 Linux 打包校验变化，Windows 产品输入和成品未改变；完整原生重跑仍必需，尚无 Mac／Linux 成品、桌面／游戏或独立三项合格结论。原始失败日志及时间保留，不能改称通过。

本包是多平台移植的接续候选，不是 Mac／Linux／鸿蒙完整验收或正式发行。source69 的 Mac 四个与 Linux 六个新候选已实际生成、内嵌身份核对并保留 SHA；ARM DMG 限定 GUI 成功不覆盖 ARM APP 原时间逆序、Intel 两种包的 WebGL 失败或 Linux 三次 Xvfb 失败。当前第七 QA 尚无原生结果。鸿蒙缺官方 SDK、合法签名条件和实机，尚无 HAP 或游戏证明。以 CURRENT_STATUS.md、docs/RELEASE-1.1.11.md 和独立评审为当前状态，下方旧版本说明只用于追溯。

Windows x64 使用 _handoff/artifacts/KAMUCL-1.1.11.exe，或解压紧凑 ZIP；无法使用自解压包装器时解压 windows-x64-unpacked ZIP。各附件 SHA256 在 CURRENT_STATUS.md；交接包逐文件清单在 _handoff/manifest.json 与 _handoff/SHA256SUMS.txt。账户、游戏、用户图片、私钥和未提交的 pelican-bicycle.html 不包含在内，接收者自行选择自己的游戏目录和登录。

当前 Mac／Linux 接续构建使用 Node.js 24、npm、Git、完整 JDK 17+；Windows 源码最低需要 Node.js 22+。依次运行 npm ci、node scripts/build-bridge.cjs、npx tsc --noEmit、npm test、npm run license:check、npm run build。Mac／Linux 共同 Electron 基线为 44.3.0，必须在对应原生架构上构建；Mac 最低 macOS 13，Linux 目标 Ubuntu 24.04／26.04 LTS。Mac 打包入口 node scripts/pack-mac.mjs <arm64|x64> --package-only；Linux 入口 node scripts/pack-linux.cjs <arm64|x64>。只构建不构成原生功能或一致性通过，详细验收清单见 docs/validation-1.1.11/parity-matrix.json。Mac 当前为 ad-hoc 签名，不是 Developer ID 签名或公证。

鸿蒙入口 npm run prepare:harmonyos、npm run verify:harmonyos、npm run dist:harmonyos；固定运行时、工程来源及缺失条件见 docs/HARMONYOS.md。不可用 SDK／签名／设备条件会报错，界面资源一致性不等于 JVM／LWJGL／游戏启动链通过。

本交接包记录的验证命令是 ["node", "scripts/check-licenses.cjs"]，无需先安装 npm 依赖。先核对外层 SHA256，再核对逐文件摘要并执行该命令；命令成功只验证许可证和对应源码要求。当前新平台正式发布门控要求完整原生运行、游戏、更新回滚、原始画面及独立评分，不能以此轻量命令替代。

2026-10-04 06:05（Asia/Hong_Kong）：GitHub workflow 标准授权已完成，`0d71da0` 已推送 master，main 通过 cherry-pick 同步为 `ef8ec49`，保留独立历史。两次原生 CI 因 FRP／进程就绪测试夹具等待而取消；旧 Windows 更新包夹具也已准确绑定平台，全部原始日志保留。修复后的 853 项 Windows 回归为 852 通过、0 失败、1 项原生 Linux skip；须重新执行完整原生测试和构建。正式标签和 Release 尚未创建，不能从本地包推断存在公开下载。不得强制推送、覆盖 main 独立历史或操作 wuhui。

---

# START HERE — KAMUCL 1.1.10 历史

当前交付为 Windows x64；下载并运行 KAMUCL-1.1.10.exe，或解压紧凑 ZIP。需要绕过自解压包装器时用 windows-x64-unpacked ZIP。两种 Mac 架构继续暂停。

源码需要 Node.js 22+、npm、Git、完整 JDK 17+；Windows 使用 PowerShell。运行 npm ci、node scripts/build-bridge.cjs、npx tsc --noEmit、npm test、npm run license:check、npm run build；打包 npm run dist:win。已验证的 Windows 使用 Electron 44.3.0。

交接包验证入口为 scripts/check-licenses.cjs，记录命令 ["node", "scripts/check-licenses.cjs"]。比较外层 SHA256 后核对 _handoff/manifest.json 与 _handoff/SHA256SUMS.txt 并运行记录命令。成品位于 _handoff/artifacts。源码包另含 SOURCE-MANIFEST.json。

当前验证与边界见 docs/RELEASE-1.1.10.md 和 docs/validation-1.1.10/summary.json；图片、账号及游戏实例为个人运行数据，未打入源码或 EXE。升级无需删除实例或存档。下方是以前版本的历史说明，仅供追溯。

---

# KAMUCL 1.1.9 交付入口（Windows）

**用户已将本轮交付限定为 Windows，暂缓 Intel 和 Apple Silicon Mac 的开发、构建与验收。验收范围以 CURRENT_STATUS.md 为准；公开状态、版本标签和下载附件以 GitHub 的 v1.1.9 Release 及外部 SHA256SUMS.txt 为准。**

本轮交付包含皮肤编辑器布局、主题勾选控件与安装弹窗、LOGO 原位卡慕互动、统一整合包分类导入的源码、资源、锁文件、测试、许可证及 Windows 成品。功能见 docs/FEATURES_1.1.9.md；实际覆盖、历史失败和未覆盖项见 CURRENT_STATUS.md 与 docs/RELEASE_1.1.9.md。详细证据位于交接包 release/validation-1.1.9/Delivery/；用户整合包、存档、浏览器配置和账号不包含在内。暂停前的 Mac 失败证据保留，不能视为 Mac 合格或已交付。

- Project: KAMUCL 1.1.9。
- Deliverable: 本轮实现、Windows 成品、完整源码及验证证据。
- Packaged artifact: Windows EXE / compact ZIP / unpacked ZIP；不包含 Mac 安装包。
- Intended receiver: 启动器使用者及接续开发者。

## Use the deliverable

Windows x64 使用 KAMUCL-1.1.9.exe，或解压 KAMUCL-1.1.9-windows-x64.zip。保留 EXE 旁的 KAMUCL-runtime 缓存；无法运行自解压包装器时使用 unpacked ZIP。本轮不提供 Mac 下载或安装说明。

交接包成品在 _handoff/artifacts/；GitHub Release 提供 Windows 独立下载。升级保留用户图片、收藏、设置和所有历史人物计数；另六人的界面退出，不删除计数。此前误导入的实例不会自动删除。

## Prerequisites

- Operating system: Windows x64。
- Runtime/tool versions: 源码需要 Node.js 22+、npm、完整 JDK 17+、Git。Windows 成品 Electron 44.3.0。

将 JAVA_HOME 指向包含 javac 与 jar 的完整 JDK，Windows 使用 PowerShell。工具和游戏依赖需要网络访问，缓存不随包交付。账号、发布令牌及 VoxLink 工单凭据另行提供。

## Setup

    npm ci
    node scripts/build-bridge.cjs
    npx tsc --noEmit
    npm test
    npm run license:check
    npm run build

Windows 成品命令为 npm run dist:win。本轮不执行 Mac 构建或验收；源码中既有 Mac 流程保留，Windows 构建不代表 Mac 通过。

## Verify

无需安装 npm 依赖即可运行许可证与对应源码检查，失败时退出码非零：

    node scripts/check-licenses.cjs

交接记录命令为 ["node", "scripts/check-licenses.cjs"]。_handoff/manifest.json 与 _handoff/SHA256SUMS.txt 记录逐文件 SHA256。独立源码包用 SOURCE-MANIFEST.json；发行附件用外部 SHA256SUMS.txt。先核对外层摘要，再解压核对逐文件摘要并执行命令。干净构建通过不表示包含签名和打包元数据的成品逐字节相同。

## 验证入口与边界

- npm test：公开合成包分类、安全路径、事务回滚、互动队列和持久化、皮肤编辑状态机等。
- scripts/verify-windows-package.cjs：最终 Windows 成品、中文路径、冷/热启动、ZIP 解压与完整文件摘要。
- scripts/verify-ui-refinement.cjs：隔离成品 GUI；KAMUCL_EXTENSION_GUI=1 覆盖全部本轮模块。KAMUCL_UI_MODULE 可指定 header、skin118、palette、gallery、gallery118、import119、selection119。
- scripts/verify-kamu-logo-119-ui.cjs：实际模型就绪、键盘焦点、连续点击、声音源、关闭重试、隐藏与释放；旧 verify-mascot-header-ui.cjs 按版本选择。
- scripts/verify-pcl-import-119.cjs <private-export.zip>：真实服务导入和所属游戏窗口验证，结果仅写 ignored out。私有 ZIP 由使用者提供，不包含在源码。
- scripts/verify-mac.cjs、scripts/verify-mac-game.cjs：原生 APP / DMG 及真实 Minecraft 窗口与世界检查。
- scripts/verify-kamu-native-trace-119.cjs：独立进程的原生合成器诊断，记录实际 BeginFrame、绘制、提交及窗口状态；带追踪的数据不能替代正常动效验收。
- scripts/verify-kamu-native-video-119.cjs 与 scripts/mac-logo-capture-119.swift：独立 ScreenCaptureKit 原生 LOGO 观察，保存实际窗口身份、原始 BGRA、PTS、采样状态及无损 PNG；不替代正式 CDP 录屏或其低于目标的结果。scripts/native-video-evidence-119.cjs 核对阶段与归档完整性。未获屏幕录制权限时保留失败，不请求权限弹窗。
- scripts/verify-kamu-feedback-scale-119-ui.cjs：125%／150% 缩放下实际点击、接触帧与声音计数核对；附加截图采集明确作为诊断。
- scripts/verify-source-archive.cjs：最终源码包干净解压、成员与摘要、安装依赖、类型和生产构建。

夹具社区响应不等同真实在线收藏安装；数值音频检查不等同耳机或扬声器听感。真实微软上传、硬件主观听测、用户存档实际游玩等未覆盖项以发行报告为准。

历史原始截图帧与失败记录已打包为 KAMUCL-1.1.9-validation-history-part001.zip 至 part005.zip，均可独立解压；完整清单为 KAMUCL-1.1.9-validation-history-index.json。五卷共 155056 个文件、5827360601 字节原始内容，已逐文件核对源目录与干净解压后的大小、SHA256 和完整清单；每卷小于 GitHub 单附件 2GB 限制。包含暂停前的 Mac 失败及 Windows 长路径漏收证据，失败指标保持原值。交接包完整携带所有最终原始证据、历史帧时间及日志；历史图片、视频、原像素和音频由上述五卷完整保存，避免重复使交接 ZIP 超过单附件限制。Delivery/HISTORY-REFERENCE.json 列出各附件大小、SHA256 与每个未重复媒体成员所在分卷及摘要。临时浏览器缓存单独列为隐私排除，原始历史帧和失败结果没有删改；交接包运输校验与外部历史附件闭合分别验证，接收者需核对索引和全部五卷。
