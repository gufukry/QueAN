# CURRENT STATUS — KAMUCL 1.1.15 Windows

## Identity

- Project: KAMUCL。
- Version or revision: 1.1.15；最终master/main独立提交与v1.1.15标签由归档外部DELIVERY-1.1.15.json及Release回执记录。
- Status timestamp: 2026-10-06 18:18（Asia/Hong_Kong）。
- Prepared by: 本批实施与独立评审agent；Windows x64四项修改，未继承全平台合格结论。

## Last verified state

- Build command: npm run build；npx electron-builder --win portable --x64 --config.electronDist=node_modules/electron/dist --publish never；node scripts/pack-windows-zip.cjs。
- Build result: 17:34香港时间冻结497项工作输入后构建，Electron44.3.0，最终三包如下。原有图标另作构建后Git字节核对；构建后的QA、文档和证据不冒称产品重新构建。源码ZIP保留Git blob字节，CRLF/LF及原生编译时间差异单列。
- Test/validation commands: npm test、npx tsc --noEmit、npm run license:check；本批真实坐标、原生合成、编译工具与干净包脚本见 docs/validation-1.1.15/README.md。
- Validation results: 最终全量1208项：1207通过、0失败、1 Linux专属跳过；类型、许可、生产构建、四主题隐私／分类／披风、重启持久化、30个三主题初次窗口状态、7项编译工具保护、3包干净验证均通过。独立评审结论与分项≥8.5门槛见INDEPENDENT_REVIEW.md；真实服务／夹具／原始失败分别保留。
- Finished artifact / Artifact SHA256:

| Windows成品 | 字节 | SHA256 |
| --- | ---: | --- |
| KAMUCL-1.1.15.exe | 97289152 | `4822890c6cc94353a16687d812fdfb0be9379dad1c0258ef4b1a35c14d686df9` |
| KAMUCL-1.1.15-windows-x64.zip | 97320724 | `9f7f6de1a92b4e8a2d9f09bb24140082040a80ae8e9d9def58765212b4df6d0f` |
| KAMUCL-1.1.15-windows-x64-unpacked.zip | 142949477 | `bafb95956a15dd428fd93938f348de77e5ff6b331adb4acd95d5ee867c3c164f` |

源码与交接ZIP的摘要在归档外部清单中，避免自引用。交接包显式包含源码和上述三包，记录命令["node","source/scripts/check-licenses.cjs"]仅验证许可。运输复制、干净解压和命令再执行由交付回执记录，功能证据不由许可命令替代。

## Completed

- 服务器地址默认隐藏；点击显示，选择／导航／失焦／隐藏后清除；服务器名称、公告、玩家、版本和提示脱敏。
- 自建版本分类增删改、归类及独立收藏过滤；跨游戏文件夹稳定实例键，分类删除不改实例／收藏。88个合成实例文件字节保持不变。
- 披风失败补载、真实错误和刷新入口、同URL旧像素保留、切换账户的迟到响应隔离、标准／紧凑／高清纹理；20活动缩略图和20清晰背面复评，四轮自有进程身份在自然退出后全结束。
- Windows淡入完成后的原生样式缓存刷新，七项保护；撤销侧栏两项强制合成声明，四主题全历史无注入新成品16张原生侧栏图均清晰，保留GPU及动效。

## Remaining

- 发布附件、源码／交接归档与master/main/tag远端核对按已授权流程完成，精确回执位于归档外部；不把此文档自引用为发布证明。
- 原用户灰屏和原披风账号服务未复现／未取得；Mac ARM64 1.1.14历史失败未解决，不纳入本版Windows放行。

## Changed or important files

src/shared/serverPrivacy.ts、connection/ServerAddress.vue与服务器页面；shared/core/versionCategories.ts、VersionCategoriesPanel.vue及GameView；skinTexture.ts、skinProfileCache.ts、skins.ts与SkinViewer3D；native/WindowMaterial.cs、App.vue；本批QA和docs/validation-1.1.15证据。

## Decisions and constraints

资源优化停止；保留用户数据、历史人物计数、旧实例及pelican-bicycle.html。不操作wuhui、不强推，main只cherry-pick保留独立历史和既有文档差异。Collector的visualPending字段保持原样，独立评审另行结论。公开截图仅去隐私合成页面与原生侧栏，不包含外部置顶窗口裁图。

## Known issues and risks

原首次灰色全窗口未复现，不把原生刷新机制当成用户设备因果证明；Aurora/Hero/Twisted原账号真实服务未覆盖；本批合成服务器专项未验证真实剪贴板／公开连接／实际游戏。最小配置960×620，本机125%实测外框962×623、内容961×622。Windows发行者未签名（NotSigned），真人听感无本批结论；Mac14世界／恢复／帧率门槛仍失败，未正式发布；Intel/Linux/鸿蒙不在本批成品范围。全部历史无效截图、前台变化、QA桥绑定、进程复用与纹理失败均保留，不回填为通过。

## Recommended next action

用户下载本版Windows并核对SHA256，使用自己的账号与游戏目录；接续开发者从最终标签源码及本批证据继续真实服务／其他平台验收。先读本版README与独立评审，不继承历史失败成品资格。

---

# Historical CURRENT STATUS — KAMUCL 1.1.14 Mac ARM64 接续候选

2026-10-06 14:28（Asia/Hong_Kong）补充：第三轮原生运行 37421862931，源 `4b43130cfb5ac9a44c10c21570b9c79bdc4cca54`，13 项任务为 8 成功、4 失败、1 撤回跳过；纯测试 1147 项中 1142 通过、0 失败、5 平台跳过。DMG 四主题实际 1280×900 与 960×620、125% 缩放通过。APP 皮肤行走原始 22 完整帧跨度 1.75 秒，12 FPS，原门槛 30 FPS，保持失败；收藏 APP/DMG 的 1.20.1 世界 240 秒失败和显示恢复失败保持。parity 的主题重载后旧文档身份错误只修 QA，后续精确重构建的专项不算完整功能通过。独立最终评分仍未满足，不发布正式 v1.1.14。下方成品表保持为原 298 构建的历史原件，最新成品以外部交付清单为准。

2026-10-06 13:52（Asia/Hong_Kong）：Mac ARM64 恢复，优化目标仍撤回，其他架构不制作。本批修复 Mac 陶瓦 daemon/客户端抢锁风险并保留真实会话身份与原日志，Windows 协议不变；版本递增 1.1.14。产品源为 `c2e4d178ba2bb8f5195e58453eff88b52ad568f2`，此后的改动仅限测试、QA、工作流和文档，必须明确区分源输入一致与字节相同重构建。

Windows 三包已构建，干净解压、冷/暖启动、中文路径及当前设置/界面原生回归通过。Mac 原生 ARM64 构建（源 `298b48ad857257636e2f3d289b2d024eefc4cd2d`）全量 1119 项，1114 通过、0 失败、5 项平台跳过；ZIP/DMG、本地签名、启动、更新、默认下载位置、陶瓦与普通 26.2 世界检查通过，DMG 四主题界面通过。该原生运行仍整体失败：APP 图库观测漏窗、资源弹窗 Fabric 操作及收藏 APP 导航须严格复验；收藏 DMG 的 1.20.1 世界等待仍失败。虚拟 Apple Paravirtual 图形设备下的 26.2 Vulkan 成功不能证明 1.20.1 OpenGL 通过。未取得真实 Apple Silicon 设备验证，不宣称完整 Mac 功能一致性合格；原始录屏与帧时间不修改，人工听感未覆盖。

以下是已经生成的候选原件，Mac 项准确属于上述 298 原生构建，不代表未来 QA 提交构建，也不代表正式放行。后续重构建以新的外部清单为准，历史原件单独保留。源码和交接 ZIP 的摘要放在外部清单，避免自引用。

| 候选成品 | 字节 | SHA256 |
| --- | ---: | --- |
| KAMUCL-1.1.14.exe | 97184697 | 3a25ed17f3478e974575c12f5ecfd1461291d61326d386b5263ac290a20c41c7 |
| KAMUCL-1.1.14-windows-x64.zip | 97216269 | 66b1155aad5ed0f77b8700876f55a25486845f5ff8e4da2a2a53f261c0136f6b |
| KAMUCL-1.1.14-windows-x64-unpacked.zip | 142853692 | 7b0d354388a22a95c793529a9a8b0f4596e8687b70d38a09a947a5687402fe60 |
| KAMUCL-1.1.14-mac-arm64.zip | 125874340 | 54235e74eae4ea76087c6fbfead1ccddb296421ee31d5d27d3f9eaf56293d970 |
| KAMUCL-1.1.14-mac-arm64.dmg | 135695698 | f0e3616c75ae169f470a470f7dc0e3f6d237fdc71cc4d81af9136f0a5dbe73bd |

公开 1.1.13 Release 与六份 Windows/源码/交接附件保持不变；1.1.14 没有正式标签或 Release。`master` 与 `main` 以 cherry-pick 维持独立历史，不操作 `wuhui`；保留用户图片、收藏、设置、全部计数、旧实例及未提交文件。

---

# Historical CURRENT STATUS — KAMUCL 1.1.13

2026-10-06 11:54（Asia/Hong_Kong）：用户暂停 Mac ARM64 制作，本次只交付 Windows x64 收藏模组漏装修复。Mac 自动打包改为默认停用，仅在明确恢复后手动启用；当前没有活动的 Mac 作业。完整资源优化和十组/十对采集已撤回，已有改动保留、原失败保留，不将局部实验当作完整应用资源收益。Windows 的真实双来源收藏、两份所选文件及一份必要前置哈希、实际生成世界、保存正常退出和重启均通过；独立专项合理性 9.1、功能性 9.0、外观 8.9。界面/动画/便携包原始证明另行保留；最终仅 Windows 的放行报告、源码/交接包及公开附件核对正在收尾，没有把 Mac 或资源未测项目填成通过。

Windows 产品输入为 `25eada75831eb4567ca629149d13a7c39f91e67b`；后续仅 QA、文档及工作流差异单列，本轮沿用已经真实验证的成品，不冒称最终文档提交重新构建。全套产品测试 1067 项中 1066 通过、0 失败、1 项平台专属跳过，类型/许可/生产构建通过。原生便携 EXE、紧凑 ZIP 干净解压及冷/暖启动通过，隐私与许可成员已核对。成品如下，最终源码、交接包及其 SHA256 以发布旁的外部清单为准，避免源码档案自引用自身哈希。

| Windows 成品 | 字节 | SHA256 |
| --- | ---: | --- |
| KAMUCL-1.1.13.exe | 97282552 | b808bc978df54a0dea45ad4daa9c16df23606bd011f3e6725395906e0da6438b |
| KAMUCL-1.1.13-windows-x64.zip | 97314124 | 69024184a4d40439534b9807bcf1d57c1a3522464a05b4ddda425aa8dbad8c81 |
| KAMUCL-1.1.13-windows-x64-unpacked.zip | 142942804 | 4569af4264146ab2e4312bc5215e0e4082594021e34203ba3cfc6e8f43059270 |

Mac `37365296044` 两次功能作业均未取得 ARM64 hosted runner，原步骤/附件为空，不能推断功能或产品失败；此前 Mac 实际游戏/显示及资源失败仍分别保留。第二次容量失败回执 `out/resource113/mac-ci-37365296044/attempt2-capacity-failure-receipt.json`，SHA256 `8e77568076090c38f62b888e40bc064d96e84a07aa1df1cda7541a4b4eebd451`。Mac 原生验收、Developer ID/公证、人工听感及完整资源收益仍未覆盖，不是本次 Windows 放行范围。

---

## 原计划和原始失败（历史）

2026-10-06 03:41（Asia/Hong_Kong）：用户明确暂时放弃资源优化需求。资源资格、十组基线、十组交替对照及新优化/采集器修复全部停止；已有测试进程和采集器自然退出，用户程序没有被关闭。已完成改动暂保留，收藏漏装修复及 Windows x64 / Mac ARM64 功能验收和交付继续；不把未完成的资源对照作为收益证明。Mac 工作流的资源对照改为单独显式启用，普通构建不再执行该任务。

Windows 新冷/暖资格已通过，但正式基线仅第零组完整，第一组冷场首页截图等待失败：当场外部前台切换后原文档失焦/隐藏，有限动画计数在原三十秒内未归零。另五个当场原计数失败保留，涉及旧外部进程的父 PID 数字被新的测试进程复用；旧进程没有被绑定或读入内存/CPU 数值，未修订原结果或修改采集器。归档 `out/resource113/baseline-1791228792441/failure-independent-archive.json`，SHA256 `f0e16259c59f8e3b1fc3d5b69186a36f2323bcc9a880c4ab848eea37738183ba`。没有有效的十组基线、噪声冻结或交替对照。

Mac `37362411153` attempt 1 没有取得 hosted runner，原步骤与附件为空，官方注记为 ARM64 容量不足；不是 Swift、产品或游戏执行失败。同源码 attempt 2 在 03:42 仍未分配 runner、没有实际步骤，随后因用户撤回资源验收范围主动取消；与 attempt 1 容量失败分别记录。取消回执 SHA256 `6e726f9af2b8468480290474bdcebbe5624997d2bc9e5d646206b0bca05c65d1`。下一次验收只保留功能、界面和成品检查；收藏游戏链、实际显示覆盖、最终 Mac 成品及公开 Release 仍未填通过。

2026-10-06 03:14（Asia/Hong_Kong）：Mac 采集器新增当次 `PROC_PIDTBSDINFO arg=1` 的原始结构字节、创建时间和 SZOMB 检查；只有同进程当场确证退出才停止采样，未知身份仍失败，指标不补零或未观测的 CPU 尾部。真实自有 fork/pipe 子进程资格与原 NOTE_EXIT/空树资格一起等待新的原生编译执行；81 项纯检查通过。资格的未执行 close、失败 waitpid 均明确为空并保留原缓冲，不冒称成功系统调用。导航新增受控的原生屏幕模式清单/临时准备/正常还原工具，只在可验证刷新率和像素质量保持、实际工作区足够的 GitHub 原生临时桌面使用；不足或未知能力直接失败，12 项纯检查通过，不改变产品/GPU/视口或资源测量桌面。

Windows 新传输的首轮恢复在第九次出现当场外部前台 PID/HWND 变化，原八轮和整体失败保留，不归因于未取证的外部程序名称，也没有抢外部焦点或放宽可见性要求。完全重新执行二十次已通过，报告 SHA256 `2ac513d4657cb167295c0509a0acc549be0deea7641f38716be41bc41253f3df`；候选完整冷/暖资格正在执行，正式十组/十对尚未填通过。master 1c3a06e 经 cherry-pick 为 main 322e958，五份既有文档差异和用户未提交文件保留。

2026-10-06 03:01（Asia/Hong_Kong）：Windows 新测量资格的冷场通过，暖场完整回调快照超过 Node 24 内置 WebSocket 解压的四 MiB 上限，原连接异常关闭并在原三十秒期限失败。实际 4,876,011 字节消息的受控复现实验确认该采集传输问题；公共 QA 改用现有 ws 的无压缩传输及默认有界一百 MiB 接收上限，保留完整原帧、上下文和三十秒期限，新增关闭、错误与超时记录。八项 socket 反例及合并八十九项纯检查通过；此前资格和私有诊断不计正式基线，新正式验证从头执行。没有修改产品、画质、功能或性能标准。

Mac `37355409749` / 60c9447 的原生构建及普通 26.2 游戏通过，但收藏 APP/DMG 在原二百四十秒内均未进入世界，诊断 Swift 编译又因 SDK 宏导入限制失败；现采用等值 4096 字节常量表达，仍需真实编译和线程栈，不推断游戏停点原因。图库 APP 的后续新预加载图片被全局四秒期限误判，现按实际挂载周期保留各自保守四秒界限，完整十五秒/八图/三层和显示前解码检查不变，十项纯检查通过，新的原生界面未验证。导航原二百四十场中一百二十场实际高度被桌面限制到 678；请求 900/960 高度不能计为通过，补实际几何稳定观察并继续严格核对最终覆盖。

该 Mac run 十组原版冷/暖采集完成，但首次冻结因退出中进程身份无法读取的原生必要计数错误而失败，没有噪声文件或候选对照。后续退出通知不能擦除当时未知身份；新增采集方案只接受当时同创建时间的明确原生退出证据，原错误及原始帧全部保留。两平台完整十组/十对、Mac 收藏游戏链、最终资源收益及整体独立评分仍待验证；未创建 1.1.13 标签或公开 Release。

2026-10-06 02:21（Asia/Hong_Kong）：Mac `37353415041` 已取得 macOS 14 ARM64 测试机；原生构建、公共测试、类型、许可证和 Swift 内存采集器资格通过。收藏 APP/DMG 在只读诊断夹具文件数量断言处各 21/22 失败，未进入收藏游戏链；本次仅规范夹具生成根的实际路径，Windows 纯测试 22/22，不冒称原 Mac 别名原因已直接取证或原失败已复测。普通 26.2 游戏在 MoltenVK 调用处因 `AppleParavirtDevice newArgumentEncoderWithLayout:` 缺失而异常退出，保留原崩溃；这是该虚拟图形路径的实际失败，不证明真实 Apple Silicon 硬件运行通过。默认目录 APP、启动与更新专项通过，DMG 十二场行为结束后因挂载卷仍忙而卸载失败；导航失败另行定位。

该 run 剩余资源、界面与工具作业由维护者主动取消，原因是补齐原不可变基线/对照 JSON 和分析工具字节的验收附件；取消前已发生的失败按各自原因保留，不全部归因于取消。Windows 分析工具的两处固定快照目录约束补齐，74 项纯反例全部通过；没有修改产品或性能阈值。后续整个 Mac 矩阵使用官方 GA 标准 `macos-26` ARM64 测试机，继续同机重采十组/十对，不混用 macOS 14 原采样，不购买较大型测试机、不改游戏/GPU依赖。Electron 44.3.0、最低 macOS 13 和原门槛保持；新虚拟显卡兼容、macOS 13/14 实机游戏及全资源验收仍待验证。

2026-10-06 01:59（Asia/Hong_Kong）：最终 Windows 收藏专项独立核对 28 份原文件及 11 张截图、三份模组哈希、生成世界/四个区域文件、正常关窗保存与重启证据；合理性 9.1、功能性 9.0、外观 8.9，均达到 8.5，未发现本专项关键缺陷。报告 `out/resource113/windows-favorites-25-independent-review.json`，SHA256 `1fdb94bdf63a11d43ec24b845094655db1105c38d5180a26e983f4107d08e268`。QA 执行提交/文件与产品输入 25eada7 分别绑定；游戏事件会话 ID 和落盘日志目录 ID 属于两个命名空间，原一次启动/正常关闭、同实例/目录/世界事件已核对，未把主动正常关窗说成自行退出。此评分不批准 Mac、完整资源对照、人工听感或 Release。

帧采集 QA 新合同在真正回调入口读取原时钟，使用私有序号/请求链、单次快照与实际 CDP 上下文；允许合规量化时钟同值但不抹去零间隙，缺链、重复帧、时钟倒退或上下文失效仍拒绝。旧十组保留分析失败、不补字段；后续须重新采集完整基线。原始工具文件增加字节快照与 SHA，平台内继续严格核对当前源；跨电脑复核使用单独获批的原快照，准确列明 Windows/Mac 换行差异，不把 Git LF 归档冒称原运行文件相同字节。新真实资格、十组/十对与 Mac 原生执行仍未预填通过。

2026-10-06 01:23（Asia/Hong_Kong）：最终 Windows 25eada7 成品通过真实 Modrinth/CurseForge 收藏安装、两份所选文件及一份必要前置的 SHA1/SHA256、实际进入生成世界、保存正常退出和重启保留。该场在实际设置页明确把四线程官方源切为 BMCLAPI，设置原调用与前后状态均记录；属于单独网络条件，不证明前四次官方源失败已修复，也不计资源优化收益。最终证明为 `out/favorites113/final-25eada7-windows-retry4-bmclapi/summary.json`，SHA256 `380da93583b2cf4ce10c240bd262f1deb87bceacfe33a19f95713ac32d00aa79`。

Windows 十组原 1.1.12 冷/暖采集完整自然结束，但首次噪声冻结因分析器要求交付时钟严格递增而失败，尚未生成噪声文件或采候选。原两处相邻 rAF 时间各自递增、`performance.now()` 交付值相等；全部原帧、来源和失败日志保留。独立复核确认该时钟只保证不倒退，但原记录缺少回调请求链，不能单凭时钟语义把旧批次补为通过。新增入口时钟、序号、请求链及真实执行上下文取证后将重新资格验证并采完整基线；不补写旧字段、插帧、删除帧或放宽性能门槛。

Mac run `37343433504` 的两次尝试均未获得 hosted runner，原 runner/steps 为空、后续全跳过、附件为零；精确失败为 “The job was not acquired by Runner of type hosted even after multiple attempts”，并附 ARM64 容量通知。两次原 API/annotation 分别归档于 `out/resource113/mac-ci-37343433504`。现将整个验收矩阵统一改为官方仍支持的 `macos-14` 原生 M1 ARM64 测试机，Electron 44.3.0、产品代码、最低 macOS 13 和各项原门槛均不变；该机需要重新完整原生构建、资格和同机十组/十对验收，不能继承历史失败或零执行作业为通过。

2026-10-06 00:44（Asia/Hong_Kong）：最终 Windows 图库与单卡慕专项完整通过，图库八项启用顺序真实轮换且最多三层解码资源，保留原十五秒全轮换与四秒解码门槛；卡慕队列、计数、声音时间线、隐藏与释放及原录屏已记录。图库此前三次 QA 失败保留：生产 VNode 使用路径键、直接 IPC 写时长未通知已有渲染状态，以及选择器转义错误；只修观察/夹具入口，不改产品。最终测量、图库与 Mac 只读游戏诊断工具的 81 项反例通过。新增 Mac 内核退出通知、严格空树判断和绑定原游戏 PID 的线程/窗口诊断等待新的原生编译与执行。

收藏官方源第四次真实安装仍在基础 client.jar 网络停滞，已确认实际四线程设置与正常退出，未开始收藏落盘，不回填通过。隔离 Electron 44.3.0 的原小段 Range 传输及生产 downloadFetch 路径取得 206 与正确 64 KiB；同次完整 GET 前缀只有 32 KiB 后超时、直连分支失败，保留原样，尚不能区分网络与流背压问题。首次诊断的 QA loader/Headers 错误和一次后台启动自动审批拒绝均独立保留。该诊断不替代最终收藏/游戏或资源采集。

2026-10-06 00:25（Asia/Hong_Kong）：Windows 最终 25eada7 成品连续二十次原生隐藏/恢复已完整通过，原窗口可见、前景、步态和回调逐轮核对，节流策略最终恢复原值；测试进程和采集器均自然退出 0。新工具的候选及公开 1.1.12 各一组完整冷/暖资格也通过，本次旧版偶然正常恢复仍统一排除恢复性能比较，不挑结果计算收益。资格数据不替代正式十组原版噪声冻结与十组交替对照；正式组尚未开始。收藏第四次对照已从实际设置界面将并发 16 改为 4，保留原官方源、代理、哈希与断言，结果另行记录。

2026-10-06 00:05（Asia/Hong_Kong）：当前产品输入固定为 master `25eada75831eb4567ca629149d13a7c39f91e67b`；后续只改测量/验收工具与文档，尚未发布 1.1.13。Windows EXE 97,282,552 字节、SHA256 `b808bc978df54a0dea45ad4daa9c16df23606bd011f3e6725395906e0da6438b`；紧凑 ZIP 97,314,124 字节，直接解压 ZIP 142,942,804 字节。便携 EXE 与干净解压 ZIP 的冷/暖启动、全部文件、许可和共享模块边已核对通过；首次 ASAR 验收路径分隔符错误保留，仅修 QA 后重跑通过。最终皮肤编辑、调色板/PNG、原生勾选/半选/键盘专项通过；四类主题的首页、皮肤、千级模组和 LOGO 截图已采集，custom 该场使用默认自定义值，不冒称任意自定义色板验收。

最终 Windows 收藏真实服务前三次均在基础实例下载阶段失败，分别停在官方 slf4j、LWJGL 原生库及 client.jar；第三次已使用显式本机代理，仍保留失败，未报告收藏全部完成。两次独立原地址 client.jar 传输均 200、23,028,853 字节且 SHA1 正确，实际耗时 45.72/32.39 秒，只作为传输诊断，不等于应用验收。下一轮通过真实设置入口把并发 16 改为 4 做单变量对照，不作同条件性能收益。此前 215b7d7 的完整收藏/游戏成功与评分只能用于该历史产品。

Mac ARM64 第四个 run `37327458667`（产品及源码 25eada7）的构建、普通实际游戏、启动/工具/更新、导航及 APP/DMG 默认目录专项通过，但收藏、图库与资源仍失败。收藏三份所选/必要文件的哈希正确；原游戏事件停在 LWJGL 初始化，240 秒仍未进入世界，原因尚未确立。图库读取生产版没有的组件内部属性，正改为原生挂载 VNode 与持久化播放清单观测，不改变产品或降低完整轮换断言。资源原十组含 35 条未确认退出的身份读取错误；kill(0) 成功不能证明自然结束，原错误不删。暖场 group3 的最后两条原采样有一条属于不完整生命周期片段，旧退出标记不能继承为通过。Swift 原生 CPU 比例与短子进程资格通过，不等于完整应用采集通过；尚无噪声冻结或候选对照。

Windows 前两轮连续二十次操作均恢复动画，但采集器重新打开已复用的 PID 导致测量资格失败，原件保留。新工具持有已核实所属进程的只读查询句柄，以原对象实际退出信号结束观察，不控制外部程序；26 项反例和实际自有短子进程资格通过，正在重做二十次恢复及完整冷/暖资格。两平台的正式十组原版噪声和十组交替对照、最终完整三项独立评分均未填为通过。master 已推送，四笔本批提交经 cherry-pick 同步 main 独立历史，已有五份文档差异保留，wuhui 未操作。

2026-10-05 20:09（Asia/Hong_Kong）：基于公开 1.1.12 开发，只制作 Windows x64 / Mac ARM64。收藏安装意图、逐项查询状态、明确跳过/仅安装基础实例确认、四路去重查询和实际落盘摘要已实现；保留旧接口、事务回滚、补装及用户数据。后台快照、扫描/图标、轮播层、纯文件哈希和大 ZIP 读取已优化。共享生产 worker 消除重复代码；Java 限堆只作用于只读探测，不改变游戏内存参数。安全归属无法证明的旧缓存不清理，七份无损资源重新压缩无收益而弃用。

第二次完整回归 1039 项：1038 通过、0 失败、1 项平台限定跳过，217521.961 ms。后续增加文件描述符绑定、读者结束后真实释放、大小写别名保护及 SHA1 大小写兼容专项，最终完整回归尚需重跑。流式读者第一次收尾验证 4 项 EBADF 失败保留；修复借用文件描述符的 close 钩子后 7/7 通过。微测试收益见 `docs/validation-1.1.13/OPTIMIZATIONS.md`。

2026-10-05 20:29（Asia/Hong_Kong）：最终产品代码的第三次全量回归 1046 项：1045 通过、0 失败、1 项平台限定跳过，195034.142 ms；含取消/回滚、FD drain、路径替换、别名顺序、收藏失败与补装、SHA1 大小写和实际落盘。类型及许可证通过，Windows/Mac 资源分析工具故障检查分别 5/5、8/8 通过。两种大 JSON 候选真实峰值均稳定升高而弃用，产品保留原生解码；原试验代码及结果已归档。本段是源码与工具验证，最终成品原生、真实服务、资源对照和评分仍未标为通过。

2026-10-05 20:46（Asia/Hong_Kong）：首个 Mac ARM64 run 37310356267 / 源码 65cce4b 的 package 回归为 1040 通过、1 失败、5 跳过；缓存重试测试在第二次下载的请求数断言失败，下游未执行，没有 Mac 1.1.13 成品。测试原用固定 50 ms 触发另一文件失败，现改为实际完成、哈希正确且已发布的缓存状态门控；新增未完成传输必须重新下载的反向检查，5/5 专项通过。保留原失败，不仅延长延时。独立审计另发现 APFS NFC/NFD 别名需按归一化身份串行；已补写入批次和 Mac 文件锁，只归一化队列键而不改文件名。修前真实异步写者测试失败，修后通过；Mac inode/readback 原生分支待再次运行。最终相关 28 项通过；公共资源采集工具 v2 保持冻结，尚未采正式组。Windows 首次打包网络请求 600000 ms 超时，原日志保留；代理重试待完成。

2026-10-05 21:16（Asia/Hong_Kong）：215b7d7 产品输入的第四次 Windows 全量回归 1049 项，1048 通过、0 失败、1 项平台跳过，176876.8169 ms。最终生产构建与 Windows EXE/两类 ZIP 生成完成；EXE 97,283,852 字节，SHA256 693d55238cc681cf3027ba4eaa6ad12bcff939c1ce8490bf23c1522db1cd9576。官方 44.3.0 下载保留首次超时及坏缓存链接失败，随后使用任务专属缓存、原代理与官方校验完成，没有换运行时或关闭完整性检查。正式 Windows 10 组旧版资源基线刚开始，尚未冻结或采候选；便携完整核对与收藏真实服务仍待执行，不将包生成等同验收。

第二个 Mac ARM64 run 37312032385 / 215b7d7 原生测试 1044 通过、0 失败、5 跳过，生产构建、ZIP/DMG、本地签名、启动、更新、目录专项、联机工具、导航一致性和实际游戏 job 通过。UI APP/DMG、收藏 APP/DMG 与资源 job 失败，整体 run 保持失败。原始资料确认：旧扩展夹具无视新查询 ticket 返回旧数组，收藏输入的 Cmd+A 未真正执行全选；均仅修 QA 契约与真实选择观察，原断言保留。资源首次公开 112 warm 失败时，真实系统 reduceMotion=true、所有步态秒数为 0、20 图只出现一张，产品正在遵守系统偏好；未进入候选或噪声冻结。专用 CI 将记录系统设置前后原值并切为正常动效，且真实 matchMedia 必须为 false 才接受采集。Swift 原生 owned-PID 物理占用观测已通过；Mac 实际被屏幕钳制的 1280×684 按原值记录，不重标请求的 1280×900。

后续本批仅 QA/工作流与交付文档变化，Windows 成品输入仍准确标为 215b7d7；最终来源差异另做审计。完整十组重复基线、十组交替对照、最终两平台收藏真实服务及独立评分均未预填通过。没有 1.1.13 标签或公开 Release。所有历史失败原件保留，工作集整理不计内存优化收益；人工听感和 macOS 13 真实系统未测时继续单列。

2026-10-05 22:20（Asia/Hong_Kong）：Windows 215b7d7 候选经原生界面从 Modrinth 和 CurseForge 收藏、安装，实际两份所选模组及一份必要前置的落盘大小、SHA1/SHA256 核对通过；实际进入生成世界、正常保存关闭、退出 0，重启后收藏及文件保留。九张最终截图与原日志经独立核对，本次收藏范围评分为合理性 9.2、功能性 9.0、外观 8.9；不是完整资源或最终新成品放行。该候选 EXE、ASAR 及原三包摘要已另存不可变历史目录，不能绑定后续产品代码。

Windows 首个正式资源基线只完成首冷场，暖场在恢复窗口处失败，完整组数为 0。六组因果取证确认：原 1.1.12 真实可见、未最小化、已聚焦且没有上方可见窗口时，Chromium 仍晚报告 hidden，实际皮肤步态停止；去掉辅助激活及主题重载仍失败。仅对自有窗口瞬时关闭再还原原节流策略，原始连续三秒步态与完整 RAF 回调恢复；另两种定位手段也有效，但不视为原版功能或性能通过。现在产品新增 Windows 恢复意图重同步，11 项事件/失败反例和类型检查通过，完整回归、重建及最终成品连续二十次原生恢复待完成。Mac 不启用这一修复。

噪声冻结前明确区分可比性能与已坏的旧版功能：所有正常前台场景和二十轮页面操作先运行，再进行隐藏/恢复；旧版恢复原失败保留并单列，不修改旧版、不拿停止动画的 CPU/FPS 冒充正常工作负载。十组基线和十组交替对照仍须从头采集；候选所有功能必须通过，无法与旧版正常恢复对照的指标不得造收益或全等价结论。

第三个 Mac ARM64 run 37315575373 / c709c1c 整体仍失败。原生构建及普通实际游戏 job 通过；两种成品的收藏均已实际安装三份正确文件，但进入世界观察在 240 秒超时，原判断不改为通过，已新增原始启动返回、事件时间线与私有游戏日志观察以定位。原图库检查误将优化后的三层 DOM 当成八图丢失，现核完整八项逻辑顺序及实际全轮换。资源十组旧版场景完成，但尚未冻结噪声、没有候选对照；原生计数的四条身份退出竞争保留，旧 CPU 单位未正确换算也不计收益。新工具只将确证退出/复用与活进程计数错误分开，保留原片段和缺失，使用真实 Mach 比例与 CPU 时钟交叉验证；仍需原生编译及重新采集，不能回填旧十组通过。

2026-10-05 22:29（Asia/Hong_Kong）：包含 Windows 恢复修复与真实收藏/图库观察契约的第五次完整回归为 1067 项：1066 通过、0 失败、1 项平台跳过，219018.7139 ms。Mac 最新纯工具反例 23/23 通过；本机没有 Swift/macOS，不将这些反例测试当作原生计数资格通过。下一次 Mac 成品构建与 Windows 重建必须使用本次产品代码；原 215b7d7/c709c1c 成品不得替换身份或继承新修复通过。

---

# Historical CURRENT STATUS — KAMUCL 1.1.12

2026-10-05 16:54（Asia/Hong_Kong）：本轮仅 Windows x64 与 Mac ARM64。整合包名称含 ASCII 冒号导致 Windows 重命名失败，以及默认目录、活动目录和异步安装目标不一致，均确认属于启动器缺陷。修复保持原下载地址、校验值、旧实例和设置兼容，旧任务完成后不再切回旧默认目录。

原报告整合包从原 Modrinth 地址下载 22843 字节，大小、SHA1、SHA512 均匹配；清单探测通过，不将此证据表述为全部资源安装或游戏启动。首轮完整回归 1012 项：1008 通过、3 失败、1 跳过，原耗时 194764.9647 ms，失败记录保留。两项旧静态目录契约按新行为强化；另修复旧“安装目录”搜索关键词遗漏。相关 18 项定向验证通过，最终完整回归 1012 项：1011 通过、0 失败、1 项平台专属跳过，原耗时 195342.8788 ms。类型、生产构建、许可证和最终原生观察器 3 项契约检查通过；原生成品验证进行中，尚未预填评分或发布结果。

本次独立评审按合理性、功能性、外观分别至少 8.5/10 执行，只评价本轮修改。成品 SHA256、最终源码提交、原生 APP/DMG/Windows 证据及真实未覆盖项由 1.1.12 外部交付回执和交接包根文档准确列明。下方 1.1.11 历史成品与失败不重标为本轮通过；Intel、Linux、鸿蒙不发布新包。

2026-10-05 17:09（Asia/Hong_Kong）：Windows 便携 EXE、紧凑 ZIP、直接解压 ZIP 已完成本机生产构建和真实中文路径干净解压、冷/热启动、全部文件及运行时/ASAR 核对。成品输入来自 fc0e85d；后续只修改 QA 与文档，不改产品或重标旧成品。首次构建网络超时记录保留，代理重试成功。

Mac ARM64 首次原生 run 37286684316 的公共测试为 1012 项：1007 通过、1 失败、4 跳过；失败是测试临时根 /var 与产品规范路径 /private/var 的别名比较，测试现在规范夹具根并继续严格断言实际下载目标。Windows focused 首次在第九场主题切换前遇到 QA 即时页面断言，八场截图和真实 C→D 目录操作保留；改为等待实际唯一可点控件、路由动画结束和调试连接释放，原时限与退出断言不变。上述两项 QA 修订分别经 16 项和 3 项专项检查通过，新的原生运行及独立评分待实测。

2026-10-05 17:12（Asia/Hong_Kong）：Windows focused 第三次完整通过 12/12 场景、真实 C→D 落盘、下载期间切换不回写设置、哈希失败、旧实例不变、重新打开后持久化及两次原应用正常退出。第二次旧文档尚未重载的误判和十场原证据保留；最终观察器要求新 performance.timeOrigin、真实完整文档及窗口大小/缩放提交后才输入，不延长原截止。执行时 Git HEAD 为 5d7afb9、工作树最终 QA 文件 SHA256 为 ae7f85df8b6dd73428b366573a987193022e534661b9e9b755dc7ac1a73ee8f3，随后提交这些实际执行字节；Windows产品仍来自fc0e85d。Mac run 37288079451 因同一 QA 修正主动中止并保留取消日志，不计为产品失败或通过。仅源码验证脚本/文档差异，不需要重构建未改动的 Windows 产品输入；Mac 将使用新的完整提交原生构建。系统目录选择框操作、原报告整合包的全资源安装及人工听感不以这次 focused 夹具代替。

2026-10-05 17:18（Asia/Hong_Kong）：Windows 最终第四次 focused 完整通过；统一等待有限动画结束后补采的 12 场景和 5 张辅助截图经独立逐图复核，合理性 9.2、功能性 9.0、外观 8.8，分别达到本轮 8.5 要求，无观察到关键缺陷。19 份证据摘要、物理跨盘、旧实例字节、资源摘要、磁盘设置和本任务临时目录清理经独立核查。执行时 Git HEAD 为 2a68698、最终 QA 文件 SHA256 为 a7682e405c914aebf69c6d93709e15c8cf8eaaf7244f3ac44d14179971c76998；本次提交固定这些实际执行字节，仅为 QA/文档变化。第三次辅助截图过渡时点仍原样保留，不拿过渡图替代最终外观。Mac ARM64 原生成品最终验证及评分、发布链接和成品摘要以外部 1.1.12 交付回执为准，未覆盖范围继续单列。

---

# 历史：KAMUCL 1.1.11 接续候选

2026-10-04 16:22（Asia/Hong_Kong）：验收观察器最终冻结检查为 994 项，993 通过、0 失败、1 项平台专属跳过，原耗时 170461.1027 ms；类型、许可证、语法及差异格式检查通过。本批仅改 QA 与文档，实际公共产品和 Windows 成品来源仍为 85d811f27e2bd119e2d93ed6583a847b9b93b425，不重标已验证成品。Windows 三包冷／热启动、中文路径干净解压及四主题实际交互已独立通过；599 个步行和 916 个队列原帧已逐项核对，人工听感及完整矩阵仍未覆盖。

Mac 85 两架构实际游戏正常退出、ARM APP／DMG 限定原生动画已通过，752 完整帧／792 回调保留；Intel 图形失败和两架构安装观察器 Proxy 序列化首错原样保留。只读 DTO 投影保留唯一实例、文件哈希、完整目标及计划条件。Linux 85 的实际 ASAR、工具及 X11 验证通过；更新测试停于正常被消费的通知文件及早期页面上下文读取，原失败不改写。新观察器要求本次持久拒绝日志、精确失败事务和实际改动哈希，并等待所属页面真实就绪；退出、销毁和同编号复用永久拒绝，不重复 IPC、不放宽原截止。新版 Mac 安装与 Linux 完整更新仍待下一原生实际执行。

完整平台资格、三项至少 9 分评分及鸿蒙原生游戏链仍未完成，没有正式 v1.1.11 标签或 Release；最终候选交付与交接文件按各自实际源码、成品和证据身份绑定。


2026-10-04 15:46（Asia/Hong_Kong）：上述物理归档修复和 Mac 真实目标选择已完成最终冻结检查：984 项，983 通过、0 失败、1 项平台专属跳过，原耗时 174065.1826 ms；类型、许可证、语法及差异格式检查通过。实际 Electron 归档合同记录保留，Windows 本机符号链接 EPERM 的未覆盖范围单列；此合同只证明限定归档边界，不替代 Linux 完整更新交易或原生桌面。新产品成品和全部原生验证仍须按下一提交重构建。


2026-10-04 15:42（Asia/Hong_Kong）：本轮确认并修复 Linux 更新的物理 ASAR 校验与回退备份复制缺陷，实际 Electron 44.3.0 私有合同已验证原缺陷和修复后的归档字节一致；不改全局归档模式。Mac 安装验收改为真实坐标选择联机验证实例，等待对应 1.20.1／Fabric 请求、文件哈希与目标目录一致，兼容性规则和原时限保持不变。完整检查与所有桌面平台重构建尚待本批最终冻结执行，不以旧成品替代新产品提交。

原 b596 原生记录保持原样：Mac 两架构真实 Demo 正常退出和存档通过，Intel WebGL 未通过；Linux 实际窗口控制已通过，更新验收遇到物理归档读取首错。本轮不回填旧失败，未具备完整原生桌面、人工听感及鸿蒙游戏链的证据，不创建正式版本标签或 Release。


2026-10-04 15:10（Asia/Hong_Kong）：最终冻结的公开检查为 979 项，978 通过、0 失败、1 项平台专属跳过，原耗时 164451.6388 ms；类型、许可证与差异格式检查通过。本批仅修正验收和源码归档，不改变产品输入或既有 Windows 成品来源。归档改从 HEAD 的原始 Git blob 生成，明确记录提交及表示格式并拒绝未提交源码和非普通文件；旧 d7 源码包的 247 份 CRLF 工作树差异及严格原字节检查失败单独保留。

Linux d7 三个实际工具服务生命周期已通过；业务验收的逻辑 ASAR 目录错误已精确复现并修正，原 ENOENT 失败不回填。Mac d7 的两架构真实连续点击已通过 32 接受／8 拒绝、接触、音源与计数校验；安装观察器注册包装、诊断写入和恢复边界已修正，独立 17 项合同及 5 项实际表达式故障复核通过，仍需新原生执行。ARM 本轮实际原生采集 719 完整帧／790 回调，全部 SCK 原 FPS 达原门槛；CDP 时间逆序原件仍为不可用于正式时间验收。Intel 实际 WebGL 初始化失败及 GPU Timeout／退出码 255 保持失败。新成品身份与最终交接以此提交后的外部回执为准，所有平台完整资格及三项评分仍未通过。

2026-10-04 14:33（Asia/Hong_Kong）验收收尾：最终公开测试 968 项，967 通过、0 失败、1 项平台专属跳过，原耗时 161446.2582 ms；类型与许可证检查通过。本次仅改验收入口、观察器、测试及状态文档，Windows 产品仍来自 `2d814479b22b8778c80bbbdf8b04421a81cf9c47`。实际四主题和 598 个原始步行帧已独立核对；CDP 采集频率不等同原生呈现帧率，人工听感及完整功能矩阵仍未覆盖。

已实际定位 Electron 44 动态导入 CJS 使旧 Linux 验收入口未执行，以及 Mac 点击后值的微任务过早读取；修正不放宽原条件。工具诊断磁盘故障、超时、迟到的启动与所属进程清理经独立故障注入复核。新的 Mac/Linux 原生结果、最终源码和交接身份由提交后的外部回执绑定；旧结果保持原样，所有平台完整资格仍为 false。下方 13:36 及更早内容为各自时点记录。

2026-10-04 13:36（Asia/Hong_Kong）接续验证：新增公共产品与原生适配已冻结，版本保持尚未正式发布的 1.1.11。当前工作树完整公开检查 `npm test` 为 949 项：948 通过、0 失败、1 项 Linux 专属 shell 在 Windows 跳过，原耗时 162260.3472 ms；`npx tsc --noEmit`、`npm run license:check` 与差异格式检查均通过。固定 280 接口、63 功能、481 子断言的协议专项 83 项通过；独立复核确认布尔宣告包装与矛盾 SCK 帧状态被拒绝，真实布尔返回类型保留。协议夹具不能证明原生功能或平滑动作。

本轮修复 Linux 陶瓦服务的独立临时目录与所属进程退出、账号页真实凭据安全提示、卡慕接受／拒绝点击的同步观察值，以及固定鸿蒙十个模板的关闭交接与持久目录授权。鸿蒙旧授权恢复失败可见重试，损坏记录保留原值后重新授权；普通文件不扩大授权。Mac 新验证覆盖四主题、240 个真实坐标页面场景、队列、离线账号与设置重启、真实公开 Modrinth 下载。Linux 新验证覆盖当前官方联机二进制、架构、私有服务、后端状态、三类更新包交易与所属 X11 窗口。相关实现与实际覆盖边界见 `docs/validation-1.1.11/CONTINUATION.md`。

Windows 生产构建、ASAR／运行时检查及三份包已生成；本机全局构建缓存问题的失败和中止日志保留，使用本批独立 Electron 下载缓存后完成，未改运行时版本或关闭哈希检查。EXE 为 97184460 字节、SHA256 `4bcb9a3d8a218963d091f8bd116419a229442e4a7de4009419a6618ce70eb604`；紧凑 ZIP 为 97216031 字节、SHA256 `a2a4c432cdd2b9c0f22a430f5def62258531272e623604493bd9195c907ee849`；直接解压 ZIP 为 142973034 字节、SHA256 `d538a4708d60e9d256fa310017997c470df4c0b438c6d1ec6d4c92e6abb8157c`。真实 GUI、便携包干净启动及新的 Mac／Linux 原生结果以提交后的外部回执为准，未预填通过。原 99e0ef4 的 14 份成品已逐摘要备份至 `release/history-99e0ef4/`；其原始证据、失败及交接包保持不变。

本轮首次全量为 932 项：930 通过、1 失败、1 跳过；非法步长已正确拒绝，失败原因是测试对隐式错误文字的匹配，条件不变并补明确错误提示后重跑上述 949 项。两张静态图交替或任意自造 manifest 仍不能凭协议摘要证明真实采集或动作顺滑；可信实际采集器和独立观看是必要条件，不能把合成正例称为原生通过。

所有平台完整资格仍为 false，视觉／交互／动效评分均未完成，没有 v1.1.11 正式标签或 Release。用户已明确没有真实 Intel Mac；Ubuntu 两架构 GNOME／KDE／XWayland 全部桌面证据尚缺。鸿蒙缺官方 SDK、合法签名、电脑及尚未完成的原生 JVM／JNI／GLFW／音频游戏链。新公共产品修改要求重新构建各桌面平台，旧包不能绑定新提交。当前源码提交、远端同步、最终成品与交接包身份将在本轮外部回执准确绑定，以下各轮记录为历史快照。

2026-10-04 09:02（Asia/Hong_Kong）当前快照：第六轮精确绑定已推送 master `69c1e89ba23a007535c77bae65052f19b6d87f58`；main `bb41072b907d8ade365e9beadc4a71ee7e69a6ca` 经 cherry-pick 保留独立历史。Mac run [37163779043](https://github.com/kamubaba-i/KAMUCL/actions/runs/37163779043) 和 Linux run [37163779037](https://github.com/kamubaba-i/KAMUCL/actions/runs/37163779037) 已结束，均不构成完整平台合格或正式发行。版本仍为 1.1.11，本批不再修改 03:59 的内置更新日志或递增版本。

Mac 两架构各 864 项为 860 通过、0 失败、4 项既有平台 skip；15 个任务中 12 成功、3 个正式 UI 失败。成功任务为 2 个打包、2 个实际 Demo 游戏、6 个限定集成、ARM DMG 的限定四主题 GUI 和 1 个独立 Intel GPU 诊断。ARM DMG 的实际可见皮肤步行、焦点和受管壁纸均通过；ARM APP 的原始 CDP 90 帧在第 55→56 帧倒退 1.618 ms，原算术 FPS 60.53927763443197、formalTimingUsable=false，保持失败。Intel APP／DMG 实际 WebGL 初始化仍失败；独立诊断收集成功也仍无画布，不能把它当产品修复。第六四个 Mac 成品实际内嵌文件为 Contents/Resources/kamucl-mac.json，sourceCommit、Electron 44.3.0、架构、最低 macOS 13 和签名均有实际核对；旧摘要的 kamucl-build.json 计划名在 summary.json 的 historicalSnapshotErrors 保留。

Linux 两架构各 864 项为 861 通过、0 失败、3 skip；六个 AppImage／DEB／tar.gz 原生包的干净解压、ELF、执行位、摘要及内嵌 source69／实际 Electron 44.3.0 身份通过。三次 Xvfb 已启动实际应用，均在 ANGLE／Mesa llvmpipe 的 WebGL 创建失败后触发 skin canvas missing，原失败和时间保留，finalizer 没有再遮蔽首错。软件驱动 blocklist 是强线索，最终根因仍待同一进程 GPU 状态诊断；不能宣称 Vulkan 驱动缺失。没有 Ubuntu 24／26 两架构的真实 GNOME／KDE／XWayland 硬件桌面主机或完整验收。

本批第七完整本机公开测试为 874 项：873 通过、0 失败、1 项 Linux 原生 shell skip，duration=117970.8989 ms，TypeScript 通过。这覆盖原冻结的 QA／合同测试；全套之后追加的 Mac 原生 BGRA 证据收集修订另经针对性校验，不能笼统称全部最终 QA 都在 874 项之前冻结。它们不能替代 Mac／Linux 原生执行。本批黑橙 general refinement 实际 GUI 已通过，包含最小窗口、1.25／1.5 缩放、最大化与原报告中的有限交互；当前黑橙 ux110 可见步行／外观另在执行，尚不预填通过。四主题 final3 可见模型／步行／壁纸仍为上一 source69 已验证结果。新 Mac SCK 与 Linux 同进程 GPU 观察器尚未原生运行，源7提交 SHA=null、nativeRunsStarted=false；Windows 产品输入未改，不因 QA／文档重包装。所有平台完整资格 false、三项评分 null、无正式 Release。

全部平台 completeQualification=false，视觉／交互／动效分别为 null；没有 v1.1.11 正式标签或公开 Release。第六源码／旧交接的build/icon.png 与 build/icon-512.png 两个构建图标漏收已由独立审计记录，下一交接按根文档与完整 source prefix 闭包核对，不能以旧包运输验证冒充源码闭包完成。第五轮旧世界 SHA 缺失继续保留；第六真实正常退出后的新 Demo 世界 SHA 单独记录，不补造旧世界。用户图片、设置、收藏、历史计数、私有账号／世界／整合包和未提交文件保留，不纳入公开包。以下旧快照只描述各自记录时点。

2026-10-04 08:02（Asia/Hong_Kong）第五轮历史快照：第五轮绑定已推送提交 `5167500`，Mac run `37160588297` 和 Linux run `37160588146` 均已结束。Mac 两架构各 857 项为 853 通过、0 失败、4 项既有平台 skip；2 个打包、2 个实际 Demo 游戏和 6 个限定集成任务成功，4 个 APP／DMG UI 任务失败。Linux 两架构各 857 项为 854 通过、0 失败、3 skip，六个三格式包的原生干净解压与包检查通过；三次 Xvfb 预检因缺少 `xdpyinfo` 失败，随后 finalizer 的 `scandir out ENOENT` 遮蔽首错，应用未启动。这是 QA 前置失败，不能算应用失败或桌面通过。

本批已冻结 QA 和包装身份修订，Windows 产品构建输入无变化。Windows 最新全套为 864 项：863 通过、0 失败、1 项 Linux 原生 shell skip，TypeScript 通过；四主题最新 final3 实际 GUI 的可见模型、原帧时间、真实像素变化、焦点及受管壁纸解码断言通过，仍属限定本机回归。Mac／Linux 新内嵌 sourceCommit／runtimeVersion 身份必须在本次提交后的新原生构建中验证，目前尚未重建。本批源码 SHA 和 Delivery 外部回执须在提交后更新，不能给第五轮旧包绑定未来提交。全部平台完整资格仍为 false，视觉／交互／动效评分均为 null；没有 v1.1.11 正式标签或 Release。

2026-10-04 07:04（Asia/Hong_Kong）更新：第四轮候选 `e0b1210` 原生 CI 均结束。Mac `37158774374` 两架构各 855 项为 851 通过、0 失败、4 项既有系统 skip；两个打包及六个限定集成任务成功，4 个 UI 和 2 个游戏任务失败。双架构 ZIP／DMG 已实际生成，Electron 44.3.0、最低 macOS 13、ad-hoc 签名明确记录；不代表完整一致性通过。ARM UI 缺独立输出目录、游戏任务依赖跨任务窗口探针属于 QA 前置问题，已修正。Intel 原截图、30 个无画布样本及三轮 EGL／GPU 错误确认 3D 初始化真实失败，根因仍需原进程诊断。Linux `37158774429` x64 为 852 通过、0 失败、3 skip，三格式实际生成后因 builder 文件名映射失败；ARM64 为 851 通过、1 失败、3 skip，打包跳过。已改为精确命名和私有目录独占发布，取消夹具使用真实未完成传输门控，保留原时间线。最新 Windows 全套 857 项为 856 通过、0 失败、1 项原有 Linux skip；最终 tar 发布调用另经语法及 4 项专项复验。此次产品输入及 Windows 成品未变，QA／Linux 包装变化必须原生重跑。独立修正复核通过，但所有平台完整门控及三项评分仍未完成；没有正式标签或 Release。

2026-10-04 06:30（Asia/Hong_Kong）更新：第三轮 `2b1e6f1` 的 Mac run `37157548139` 与 Linux run `37157548122` 均完整结束为 failure，没有取消或死锁。Mac 两架构各 853 项为 846 通过、3 失败、4 项既有系统限定 skip；失败为测试临时目录别名 `/var` 与产品正确返回的 `/private/var` 规范路径不同。Linux x64 为 850 通过、0 失败、3 skip 后进入生产构建，但旧打包检查要求官方 Electron 44.3.0 已不含的 `libEGL.so`；ARM64 为 849 通过、1 失败、3 skip，合成 Windows classifier 被正确的 ARM 安全规则拒绝。已修正测试根目录和原生库夹具，按两份官方 ZIP／SHA 建立准确的运行时清单，保留 ELF、执行位、ICD、资源和许可检查。Windows 最新全套 855 项为 854 通过、0 失败、1 项原有 Linux skip。此次新增 Linux 打包校验变化，Windows 产品输入和成品未改变；完整原生重跑仍必需，尚无 Mac／Linux 成品、桌面／游戏或独立三项合格结论。原始失败日志及时间保留，不能改称通过。

## Previous verified snapshot

2026-10-04 06:05（Asia/Hong_Kong）。当前版本 1.1.11；公开基线为 1.1.10。修复 FRP、POSIX 进程就绪等待与 Windows 更新夹具后的全量公开测试 853 项：852 通过、0 失败、1 项 Linux 原生 shell 测试在 Windows 跳过；TypeScript 通过。初始下载与取消阶段分别观测，五个平台／架构使用隔离夹具执行真实控制器；独立真实子进程观测保留 stdout、正常退出码和仅自建进程终止断言，四个平台／架构更新路由保留目标隔离与记录清理断言。这些合成／局部平台分支验证不能替代原生运行。此前生产构建、许可证、Windows EXE／两 ZIP 干净解压、冷／热启动及完整 payload／ASAR 核对通过。本次后续变更仅测试、工作流和状态文档，全部 Windows 产品构建输入及成品摘要未改变。四主题 ux110 与黑橙的彩蛋、皮肤、色板、图片／收藏、导入和选择必测模块通过；服务夹具与故障注入不作为真实在线服务证据。

## Completed

已实现共享平台信息和架构规则、Linux 安全凭据与会话回退、ARM64 原生库与 ELF 验证、联机资产、X11 游戏窗口助手、按安装形式选择附件和 Linux 更新回滚、Mac 44.3.0／13+ 两架构构建与完整验收入口。修复旧版本测试分流，建立 15 页面／280 IPC／63 场景矩阵和读取原始证据的发布门控。鸿蒙固定原生运行时／模板，生成接续工程并验证共享前端字节一致；不宣称 HAP 或游戏运行通过。

## Remaining

第六 source69 的 Mac 四包和 Linux 六包已完成原生生成、真实内嵌提交／运行时身份和限定包检查。ARM DMG 四主题可见步行与壁纸通过，ARM APP 原 CDP 时间逆序和 Intel APP／DMG WebGL 缺失仍失败；Linux 三次 Xvfb 的实际应用也在 llvmpipe 上失败。当前第七 QA 的原生采集／GPU 状态观察尚未原生验证。所有页面、真实账号／联机／更新和完整矩阵尚未完成，三项独立评分均未给出。鸿蒙缺 SDK、合法签名、设备和完整 JVM／LWJGL／JNI 游戏链。

GitHub workflow 标准授权已完成，第六 master `69c1e89ba23a007535c77bae65052f19b6d87f58` 与 main `bb41072b907d8ade365e9beadc4a71ee7e69a6ca` 已实际推送，main 为 cherry-pick 独立历史。当前第七 QA／文档批次尚未提交，其 sourceCommit=null，提交后再写外部源码／Delivery 回执。首次和第二次 FRP／POSIX 夹具等待取消、旧 Windows 更新夹具误路由，以及第三至第六轮原日志和失败逐轮保留，不改称通过。此前 OAuth 权限拒绝、超时和过期授权记录仍是历史；没有 v1.1.11 标签或公开 Release，不操作 wuhui。

## Known issues and risks

历史 Intel 活动停顿与 CDP／SCK 未达标继续失败；第六 Intel APP／DMG 当前 WebGL 初始化仍未解决。ARM APP 90 个原帧的 55→56 时间倒退 1.618 ms，算术 FPS 60.53927763443197 不能使其原时序可用；不排序或重写帧时间。ARM DMG 的独立四主题可见步行通过不填补 APP 失败。Linux AppImage 缺少 FUSE／提取运行不自动替换；DEB 交系统安装器确认不等于自动更新完成；系统模糊与外部窗口控制仍受合成器及 X11／XWayland 能力限制。安全凭据、会话回退和 ARM 原生库拒绝规则保持。

物理听感、全部真实账号与社区服务、跨架构真实联机及完整矩阵未覆盖。Mac 仍为 ad-hoc 签名，不是 Developer ID 或公证；第五轮旧世界逐字节 SHA 缺失不可补造，第六 Demo 正常退出后的新世界 SHA 有独立收据。第六源码／旧交接漏收build/icon.png 与 build/icon-512.png 两个构建图标的历史审计保留，下一源码／交接包必须核对完整构建输入闭包。详见本轮 summary、Mac／Linux 说明和独立评审。

## Recommended next action

本机第七完整套件与 TypeScript 已通过；确认全套之后追加的原生证据收集针对性校验与当前黑橙 ux110 结果，再提交并重新执行 Mac／Linux 原生验收；以实际 PID 校验后的 Linux GPU 状态和 Mac SCK 原始 BGRA／PTS／完整采集状态分析失败，不更改正式帧率／时序门槛。补充 Ubuntu 24／26 两架构真实桌面／GPU主机，以及 Mac 支持 GPU／最低版本实机。提供合法鸿蒙 SDK、签名与设备后继续游戏链。各平台全部必测通过且视觉、交互、动效分别至少 9 分、无关键缺陷后才能发布，不用 Xvfb／交叉构建／局部 Windows 回归代替。

## Packaged artifact SHA256

以下为本地 Windows 接续候选，未公开发布。

| 成品 | 字节数 | SHA256 |
| --- | ---: | --- |
| KAMUCL-1.1.11.exe | 97290244 | dd584c92935c71ebdcd5ca379d0f1f851c9a07b604a9dcdfd6f2a1986e8142c7 |
| KAMUCL-1.1.11-windows-x64.zip | 97321815 | 78d31f0045102c983fa67d586511993312f2901c686b3f75be1d0f95c163bd2f |
| KAMUCL-1.1.11-windows-x64-unpacked.zip | 143058234 | 676162091dc723ee97ee6309232eca01de24d0fe602ddaa3d2e71d702b2dd881 |

内嵌 ASAR SHA256：b03fe16bb64a0bb341069db58b736e3aaeffb8866495f45f1bc4c82dcc64b5f1。最终源码提交由 SOURCE-MANIFEST.json、源码审计回执和交接包清单绑定。完成最新应用构建后的改动仅涉及 QA、发布门控和文档；干净源码审计再次核对提交后的文件。全部用户图片、账号、收藏、计数和未提交的 pelican-bicycle.html 保留在原位置，不包含在成品或公开源码。

## Changed files and evidence

入口为 START_HERE.md。源码文件列表由源码清单给出；本批验证汇总 docs/validation-1.1.11/summary.json，逐项库存 parity-matrix.json，独立评审 independent-review.md。原始测试／构建／包验证／GUI 记录保存在交接包 release/validation-1.1.11/；原始帧不插帧、不重写时间。记录验证命令 ["node", "scripts/check-licenses.cjs"]，成功仅表示许可证／对应源码核对，不是全平台验收。

---

# CURRENT STATUS — KAMUCL 1.1.10 历史

## Last verified state

2026-10-03 14:43，788/788 测试、TypeScript、生产构建、许可证及最终 EXE 四主题 GUI 已通过。产品成品 SHA256 如下。成品之后的文档与 QA 脚本变更不改变应用构建输入。

## Completed

收藏真实图标与旧记录补图、黑紫玻璃透明度、闲置 LOGO 永久暂停皮肤修复、自然站姿和回调保护、启动减少动态效果诊断、启动卡随机播放。原生启动当前显示器 59.96 FPS，备用启动两种动态模式已验证。详见 docs/RELEASE-1.1.10.md。

## Remaining

本文件写入时，Windows 最终 EXE/ZIP 干净解压与完整哈希检查已通过；源码和交接包运输核对以及远端提交、标签、公开 Release 与附件核对仍在执行；由交付回执确认结果，不由文件描述推断完成。

## Known issues and risks

受影响用户电脑不可访问；真实 CDN/平台图标网络未覆盖（服务夹具与真实图片解码分别记录），物理声音未复听，Mac 两架构暂停，录屏帧时间未改写或当作所有机器的 FPS 合格证明。历史失败保留，详见 summary.json。

## Recommended next action

从 GitHub v1.1.10 核对 SHA256 和 Windows 下载，源码运行 START_HERE.md 的验证；系统关闭动画时在 Windows 辅助功能 → 视觉效果开启动画。继续 Mac 工作须由用户恢复范围。

## Packaged artifact SHA256

- KAMUCL-1.1.10.exe: 92e93260179b75f36d3e145ad70f7c4e818042d6de60fadf3949a372249b50da
- KAMUCL-1.1.10-windows-x64.zip: 76cfb0eebf917a639b6551b0d360c47ddc71850239799a303430dce1fca95442
- KAMUCL-1.1.10-windows-x64-unpacked.zip: d1b42165dedeed4d32f53559da120af90fd083001959deb63f23013bb2a5edd7

## Changed files and evidence

应用更改覆盖收藏模型/项目元数据/收藏 UI，App 玻璃与彩蛋优先级，SkinViewer3D 帧生命周期，原生启动诊断，轮播类型/策略/状态/UI，设置说明及版本锁文件。验证入口 tests/appearance-motion-110.test.ts 和 scripts/verify-appearance-motion-110-ui.cjs；完整文件列表由源码和交接清单给出。

---

以下保留 1.1.9 历史文档，仅用于追溯，不是当前验收或安装说明。

# CURRENT STATUS — KAMUCL 1.1.9

## Last verified state

Windows 成品、三主题与真实 PCL 导入已验证；783 项测试、许可证检查及源码干净解压构建通过。实际成品仍来自 07a1d87c，最终文档与发布工具没有改变应用构建输入。

## Completed

皮肤编辑、主题勾选和安装弹窗、LOGO 单人卡慕及整合包优先分类已完成。独立 Windows 评分为视觉 9.0、交互 9.1、动效 9.0。完整证据、成品摘要及服务／夹具边界见下文。

## Remaining

最终源码包和交接包需与本次文档修订提交重新绑定，并完成运输复验、GitHub 公开 Release 和远端附件核对；这些步骤以发行回执为准，不由本地验收推断完成。Intel 和 Apple Silicon Mac 按用户要求暂停。

## Known issues and risks

物理音效听感、微软真实上传和社区真实在线安装未覆盖。暂停前 Intel 动效停顿及低于目标的原始结果未解决；Mac 不属于本轮合格或交付范围。完整历史失败与未覆盖项见下文。

## Recommended next action

接收者先核对 GitHub v1.1.9 的标签、SHA256SUMS.txt 和附件，再按 START_HERE.md 解压、核对逐文件摘要并运行记录的许可证检查。接续 Mac 工作前须获得用户恢复该范围的指令，保留原始失败与帧时间。

状态时间：2026-10-03 11:01（Asia/Hong_Kong）。

本轮仅交付 Windows。产品提交 07a1d87c0710ab070b763172d27dcf7cebd6547b；EXE SHA256 11a8f16142a0f40519adb2203a91ee7bf74b2ab35ef456729e5034a8fc0aafef；renderer index-rFSz7oqx.js；Windows 实际 GUI QA 07a1d87c0710ab070b763172d27dcf7cebd6547b。暂停前 Mac 历史 QA／源码桥接 a8496bf81efc1f815dc89b7a3e078f8b6ed89b74、CI 37087543778 不作为 Windows 实测 QA 或全球合格结论。Windows production07a1d87c and its real GUI/PCL/portable validation remain unchanged. Subsequent QA/source a8496bf product inputs are identical. User explicitly deferred both Mac architectures; Windows-only release asset selection and delivery documentation do not change app build inputs. Original Windows GUI QA07 and PCL QA0a exact source receipts remain separately bound to the current07 executable; Mac results are historical and never Windows or global qualification.

皮肤编辑器采用模板布局并固定关闭／保存操作；勾选、半选和安装弹窗统一主题。LOGO 原位卡慕单人逐次排队拍打，保留其余六人的历史计数。统一 import:probe 优先识别整合包清单，修复 PCL 包夹带存档时的误分类；用户图片、设置、收藏、原始包和现有实例继续保留。

实际日志 out/test-119-windows-only-release-final.log：783/783 通过，失败／跳过／取消／未实现测试项均为零，耗时 75722.0961 ms。Windows 范围独立评审 complete／passed：视觉 9、交互 9.1、动效 9 各自达到 9，当前 Windows 关键缺陷为零；不使用平均分或推断填分。

当前附加许可日志 out/licenses-119-windows-only-release-final.log 已通过，SHA256 d2fc75c204b8379dc900667285519f9edf35d111740b03a309f6c42645ebd43e。

Windows 黑橙／浅色／自定义三主题完整记录逐文件哈希相符。用户明确暂停 Intel／Apple Silicon Mac 的开发、构建、验收与安装包输出。本轮不运行 Mac 验收，不输出 Mac 成品，Mac 未合格；Windows 评分不替代其动效验收。

暂停前 Mac 历史：release/validation-1.1.9/Delivery/History/native-a849-deferred-original-intel-formal-stall/evidence.json（SHA256 29d23e9549eb2b19b37058d78aa9837478e917541b164d1fee6d78f349be0fcc）；macQualificationAccepted=false。原 Intel 活动 PTS 间隔 130.62733300000673 ms、声源间隔 168.20000000006985／165／198.09999999997672／163.29999999993015／328.70000000006985／246.59999999997672／164.59999999997672／195.59999999997672／170.30000000004657 ms 保留，正式 CDP／SCK below-target false 与独立动效拒收未改为通过。原成功 job 为功能结果，不能代替动效合格；根因未确定。Mac work and installers deferred by the user. Previously collected a849 native jobs succeeded functionally; Intel formal APP original active PTS gap130.627333ms and below-target formal/SCK false remain unresolved and are not accepted. These results do not gate or claim Windows-only UI qualification.

皮肤保存状态补验分别读取四次可信单点事件、真实 UV／RGBA 差异和两个原始 PNG；基础层保持 255、外层保存 128 透明度，重复绘制与吸色实际差异为零。每个保存同时要求原 IPC 返回 true、渲染器已保存且忙碌／冻结解除；未将 IPC 返回单独当作完成。实际保存处理器均恢复。Windows 补验清单 SHA256 b373fedfab401ae2dc5062b50f483e1103ad61b1fdd7d5baa05a57c076ae6050，16 个原文件已逐项核对；独立记录不替代原三主题完整验证，也不推断历史 Intel 失败原因。

Windows 黑橙独立补验：release/validation-1.1.9/Delivery/Palette-State-Observation-Windows-download304/skin-palette-state-119-black-orange.json（SHA256 722a7575ce17441975864b5935210f0aadc9d94f70404dd7935ec9caf24c3439）；两次实际就绪 321 ms／PNG SHA256 8a8033e08fc638439b050c37e301f7e01dc110d277eeb0ef381315c5d5a26db1、317 ms／PNG SHA256 9931814687c2179da9c3372d5fd33b77b91768d478cc8677ae8423a94e902354。

已存在的额外截图／缩放记录按原清单复验：release/validation-1.1.9/Delivery/Skin-State-Observation-Windows-download304（35 个原文件；清单 SHA256 1fda8d42be47f0810ae35fa360113241a2d8b84d3f7130d66eb8a4488c33bd3d）；release/validation-1.1.9/Delivery/Feedback-Scale-download304/black-orange（443 个原文件；清单 SHA256 6a211f1feeff4072d30bdb01b4f84e9a4e16b9d48c10fd1d866874bfd5d6d04e）；release/validation-1.1.9/Delivery/Feedback-Scale-download304/blue-white（443 个原文件；清单 SHA256 2b2f7d1e93b1954f1e33f3b87cdf8a59d9e2924dfdd01afa8fd855bebe50cdc4）；release/validation-1.1.9/Delivery/Feedback-Scale-download304/custom（445 个原文件；清单 SHA256 64afd46b3d863cd3bf6ef6498d39ae0d254d5179c8ead2af40042d0e284d2863）。这些记录的功能、评分和原始 benchmark 保持分开。

独立生产窗口生命周期补验 release/validation-1.1.9/Delivery/Active-Production-Lifecycle-Windows-download304/kamu-active-production-lifecycle-119-black-orange.json（SHA256 f4e4a41b3b9e0dfc254bd18f898b351090b0234299b1a8bd5211a208e43289b9），原始 receipt SHA256 5d1985847d76173827ae9d9f3c21f4889004af709a438f26e25879dc325ad28d、log SHA256 92ed81beb0c4860e994b6e373dedd993175621a22f4f2ec57f8317cd232e83f2。其 product／EXE／QA、四个原始 QA 源快照和清单逐字节核对，未复用历史 df8 结果。实际 backgroundThrottling=true，关闭焦点模拟并移除三项禁用后台节流参数；原生最小化时 document.hidden=true，实测暂停 258.09389999999985 ms，动画时间、音源开始数、计数、队列和画布上传不动。恢复后六个可信拍打对应加六和六个独立真实掌击声源；第二次真实活动掌击期间关闭彩蛋，18 项效果取消、两个 AudioContext 原调用返回 closed、两个实际 WebGL2 上下文报告 lost，原 renderer／main hooks 均恢复。这里只验证彩蛋 stage 关闭，没有推断整个窗口销毁、GC、正常帧率或耳机／扬声器听感。

当前精确 EXE 的安全 PCL 证明为真实服务验证后的隔离缓存复用（fixtureTransport=false、realServices=true），不是新冷 CDN 下载。46 模组／4 材质包／2 光影包／1 存档；50 清单 SHA1／SHA512 和 325 overrides 字节一致，原用户包未变。实际 Windows Minecraft 1.20.1／Forge 47.4.23 主菜单窗口 PNG 已按 SHA256 核对；没有打开私有世界。社区收藏、逐个安装与失败恢复使用受控 metadataFixture，committed:false 的记录仅是安装计划，不能描述成真实在线下载写入。

## 三个 Windows 成品 SHA256

| 成品 | 字节数 | SHA256 |
| --- | ---: | --- |
| KAMUCL-1.1.9.exe | 97282718 | 11a8f16142a0f40519adb2203a91ee7bf74b2ab35ef456729e5034a8fc0aafef |
| KAMUCL-1.1.9-windows-x64.zip | 97314287 | d71d0173ed7d3cabb92b70b77f08f773385c25d4f613d984a07c6a3a1d91149c |
| KAMUCL-1.1.9-windows-x64-unpacked.zip | 143047398 | c33f492fc66054991cf8d8f904df2ffc477904e8e4e828d44e3c5901dea11c30 |

## 历史失败与未覆盖

历史 416 Intel APP 的 480000 ms UI 超时和原始 298.0389595031738 ms 活动延迟完整保留，该次未完成后续 DMG／游戏。c101 后续实际 APP／DMG／Minecraft 核心均通过，额外更新被 25 分钟总作业期限取消，不能改写成游戏失败。其 APP 掌 170.0739860534668 ms、DMG 掌 202.5442123413086 ms、转身 293.9188480377197 ms 当时为动效 8.5 的拒收缺陷。原采集 false／below-target、冷头像 478.9009094238281 ms 等待、帧／时间／声源均保持原值；不插帧、不平均或放宽 nominal30／实际显示频率和 90 ms 音源门槛。

be684（CI 37040518710）ARM APP 完成，DMG 的 skin makeDirty 失败，后续游戏／工具／更新被跳过；原因未证实。Intel 当次 APP／DMG／游戏／工具／更新完成，但正常 CDP APP／DMG 原始 28.823869368236103／29.16257666775738 fps 均为 status=below-target、passed=false。后来新观测通过不能反推该旧失败已得到因果修复。原 Candidate：release/validation-1.1.9/Delivery/History/native-be684-recorder-partial/CANDIDATE.json（SHA256 64ab7897e9c17599cfb131e27827030069ef688edaf76e548a73e990dabdc9ea）。

ad217（CI 37046206273）两架构 APP／DMG／游戏／工具／更新功能完成。4 条日志仅报告 ARM SCK APP／DMG passed、Intel below-target；因阶段环境变量遗漏，四项原始 BGRA、PTS／status JSON、PNG 和 receipt 未归档，独立原生视频属于未覆盖，不能从日志重建或验收。正常 CDP 原值为 arm64 app 59.94766591464087 fps / true、arm64 dmg 60.02177665884168 fps / true、x64 app 28.786189379478277 fps / false、x64 dmg 28.080961413285365 fps / false，其中 Intel false 完整保留。原 Candidate：release/validation-1.1.9/Delivery/History/native-ad217-video-collection-gap/CANDIDATE.json（SHA256 82aba82ad49cd49c6e63b76a26f3e562fa45cc3e1b8a951563386e811261460a）。

38296（CI 37049091272）两架构 APP／DMG／游戏／工具／更新功能及四项 SCK 原始采集完成；2026-10-03 03:10 独立全局评分仍为 9／9／8.5，未合格。Intel APP 活动掌击原帧 30→33 的 PTS 间隔为 131.81587899998704 ms（约 131.815879 ms），callback 间隔 137.903734 ms；中间 31／32 为原始非完整 status=1，没有补成图像帧。这是实际活动呈现延迟，不能只解释为静止同像素采集抑制。与此独立，Intel SCK 全程 APP／DMG 原始 24.814276859672987／29.105916860429957 fps 的 whole benchmark 均为 false；正常 CDP APP／DMG 25.279447778570155／29.176427377942996 fps 也仍为 false。ARM 局部动效 9 和功能完成不抵消 Intel 缺陷；新产品的备用帧调度也不反推历史失败已治愈。原 Candidate：release/validation-1.1.9/Delivery/History/native-38296-foreground-cadence-failure/CANDIDATE.json（SHA256 90a2ed8058450286d80e14abda95fffcf0d502bf1965e93c2c162939eea2ed0b）。

f7b9070（CI 37054137475）为加入备用帧调度后的共享新产品历史。ARM APP／DMG／游戏／工具／更新完成，原两个 ARM 成品仅保留外部 SHA 引用。Intel APP 在第四个 1440×684／1.5 紧凑布局的头部显隐 aria-pressed=true、预期 false 断言失败，后续 DMG／游戏／工具／更新跳过；Intel 两个成品未产出，原因尚未证实。本次三项独立 SCK 采集及逐字节 BGRA→PNG 核对完成，不能把 Intel 全 APP 改为通过。Intel SCK whole 26.150901109769062 fps 原始 passed=false，active 29.938328603795487 fps 与 whole 分别记录，正常 CDP 27.993230370645446 fps 原始 false 保留。实际 watchdog 样本保留 null RAF 时间；40 ms 备用定时器不能跨越阻塞 JavaScript 的 143 ms 长任务。原注入 no-backdrop 145.39999999996508 ms 和恢复 backdrop 95.39999999996508 ms 保留为诊断，不用它们或后续 1b42 观测反推本次修复或合格。只读分析未提供评分；重复／idle／PTS／未知丢帧保持原值。原 Candidate：release/validation-1.1.9/Delivery/History/native-f7b9070-watchdog-part-toggle-failure/CANDIDATE.json（SHA256 d00f1955bdc24aa04f45eef23d5e7ab2f30b6c711fc9390008996394d77a6d9d）。

1b42（CI 37056101196）为同 f7 产品的后续 QA 历史。ARM APP／DMG／游戏／工具／更新完成；Intel 五种皮肤布局各单次可信点击后头部隐藏，未复现旧 f7 显隐断言，但不倒推旧失败原因。随后重复半透明像素 clean 断言 actual=false／expected=true 失败，并出现退出 ETIMEDOUT，DMG／游戏／工具／更新及两 Intel 成品缺失。之前 outer alpha=128 断言已执行，原 palette-opacity PNG、绘制 UV／RGBA、保存结算状态和失败截图未归档，不能补造或给出原因结论。Intel header 23.757291518740544 fps 和 SCK whole 26.824209424779546 fps 的原始 false 保留；active 29.996424561658944 fps 低于原 30，分别记录。正式 SCK 活动最大原 PTS 间隔 36.83803900003113 ms，无 watchdog；cold first-native 的 16 个 watchdog 仍伴随 late timer 和 95.40000000002328 ms 活动延迟，after-header 99.5 ms 保留为诊断，回调来源不等于因果治愈或合格。后续保存结算与 UV／RGBA 观察复测未纳入，未提供评分。原 Candidate：release/validation-1.1.9/Delivery/History/native-1b42-palette-clean-assertion-failure/CANDIDATE.json（SHA256 ae6bf1879120801810317a050db487fd4ac23d9b18c3af05bf2a64659b344548）。

5be338d（CI 37059909531）继续使用 f7 产品，仅增加 QA 观察。两架构 APP／DMG 及新调色板原始可信 UV／RGBA、两次保存结算和原 PNG 均完成，不能倒推旧 f7／1b42 失败原因。ARM 游戏／工具／更新完整；Intel 实际日志已进入测试世界，随后 screencapture -x -D 1 返回 exit 1、could not create image from display 25165824，属于实际截图失败而非超时。其最终 verification、原存档／区域和正常关闭检查未完成；保存日志不能替代磁盘验证。Intel 工具／更新独立完成，四个原 Mac 成品按 SUMS 核对后仅留外部 SHA 引用。Intel header 原始 28.995708575449907／28.692595207239854 fps 均为 false；SCK whole 23.74567382434487／27.61504013395236 fps 原始 false 保留，active 26.25578514636929／30.00698242476522 fps 分别记录，只有 DMG active 达标不能替代 whole。APP 原活动帧 51→53 间隔 99.68214199989234 ms，中间 52 为 idle；DMG 40→41 间隔 37.33113999987836 ms。真实 RAF／watchdog、原始 PTS／status、逐像素 BGRA／PNG 和未知丢帧边界保持原值，未提供分数或推断因果治愈。原 Candidate：release/validation-1.1.9/Delivery/History/native-5be338d-game-capture-failure/CANDIDATE.json（SHA256 048b4937a34ca433fe58eda27477c9d14ae4620af1b20ff951e0ba4bf1d9ea17）。

df8c7ee CI37067073503 attempt1：ARM 功能、游戏、工具与更新完成；Intel APP／DMG、实际 Minecraft 与工具完成，25分钟作业期限取消时更新未完成，capture-state.complete=false、inhibitor.released=false 原样保留。它没有满足最终采集收尾门控，不能当作一次全绿。Intel 正常 CDP 27.256736241485495／29.066234101110993 fps、SCK whole 28.36494070620574／27.391204323099952 fps 原始 false 保留。原 Candidate：release/validation-1.1.9/Delivery/History/native-df8c7ee-palm-compositor-deadline-cancelled/CANDIDATE.json（SHA256 4b7a4407a41f676ef30228d2e3c4b5745c3ee49479b11a587dd29f565b88db08）。

同头 df8 attempt2，精确 Intel artifact11255172163／job111052908196：APP／DMG 功能、调色板与原始 SCK 全字节核对完成，但真实 Minecraft 资源安装收到 HTTP304，游戏尚未启动；capture-state.complete=false、captureCount=0，抑制器最终 released=true。工具与更新独立完成，不能拼接 attempt1 游戏通过而声称此次成功。原 artifact ZIP SHA256 ef3b752685da2cf1b7e89e4e09fd870100b32165576ade8f6ada1c5f36551e1f 与官方 digest 一致；正常 CDP 29.676458271655054／28.384082438956497 fps、SCK whole 26.663502528570735／27.619654621285928 fps 均保留 false，active 分别为 29.98475345549518／27.789268051985044 fps。原 Candidate：release/validation-1.1.9/Delivery/History/native-df8c7ee-palm-compositor-http304-failure/CANDIDATE.json（SHA256 01c991bb2148970da7df5deaf0d64094e534401c2db9c584cf25a9d02c1070ca）。

Windows 独立 lifecycle 首次原 run exit1：原生最小化成功，document.hidden=false／visibilityState=visible，等待实际隐藏暂停的断言失败；两个原 hook cleanup=true。原失败清单 release/validation-1.1.9/Delivery/History/Active-Lifecycle-Windows-palm-compositor-failure-20261002T223918465Z/evidence.json（SHA256 6dcecc310971c4041a2409005b8aa1e96d591eafa033828e5ce142127a4c5119）保留。随后 df8 production-window 独立原 run exit0 在关闭焦点模拟并移除三项禁用后台节流参数后，观察到 backgroundThrottling=true、真实文档隐藏，暂停 258.4445000000014 ms、六计数及六独立掌击源、两个 AudioContext closed 和两个 WebGL2 lost。原 proof release/validation-1.1.9/Delivery/Active-Production-Lifecycle-Windows-palm-compositor/kamu-active-production-lifecycle-119-black-orange.json（SHA256 f36de1a75f7d86d3b0898237044daf553498fe0c05ac8582463b76ee79fbce92）。两次实际运行条件不同，后一次不能抹除首次失败或作为新产品免测依据；stage close 不代表整个窗口销毁、GC、主观听感或帧率验收。首次 PCL 参数遗漏日志另外按测试启动基础设施失败保存，导入器当时未运行。

0297b12 CI37076690205：ARM APP／DMG 功能与采集通过，正常 CDP 原值 57.966754564804／60.23482452170352 fps；ARM 游戏、工具及更新的原成功记录单独保留。Intel job111068069746／artifact11257351229 在“successful upload refreshes profile”断言失败，尚未完成 APP 后续、DMG、游戏及工具验收；当次没有充分上传状态观测，原因仍未确立，不能用后来 QA 发现回填为已证实原因。该 run 总体 failure；ARM 成功不能补齐 Intel。两架构官方 ZIP、源 Git blob、原日志及所有安全成员的 SHA／大小／文件闭包已核对。原清单 release/validation-1.1.9/Delivery/History/native-0297b12-download304-arm-success-intel-upload-failure/evidence.json（SHA256 0b676b12832a2d34c409b1177962c205363c6758532512adaad2937cceffa399）。

0a7ff72 CI37079281681：ARM／Intel 均在“upload QA observers/handlers must be restored”断言失败。原夹具失败与成功上传状态均 ready=true，监听器已移除且原 busy listeners 保留，但两个原 IPC handler identity=false；原项目 handle 注册包装与夹具重新注册原 handler 再次包裹相吻合，QA 还原缺陷已确立。此为隔离夹具验证，未验证真实 Microsoft 上传服务；两平台后续完整 APP／DMG、游戏及工具未覆盖，不能借 0297 ARM 成功拼接。Intel 独立 observer ABA 在减少动态效果模式执行正常手掌可见断言，原 65 个样本均 reduced，手掌透明度0／动画为空、手印正常淡出，10次计数与10个真实声源守恒；该诊断仍 complete=false。SCK whole 原值 24.18409687240473 fps／原门槛 29.43021583557129，原 passed=false 保留，active 30.025562958352772 fps 不替代 whole。两架构官方 artifact/source/log 身份与全部安全成员已核对。原清单 release/validation-1.1.9/Delivery/History/native-0a7ff72-upload-qa-restoration-aba-failure/evidence.json（SHA256 d16fefb806d44f3f5cdc9d75bddcc41ec8408784965bfdf8d4840861ca9c6be8）。

e81ddc7 CI37080988726：ARM job111081241919 与 Intel job111081241732 原结论均 success，APP／DMG 功能、实际游戏、工具与更新的作业成功不改写为失败。独立评审拒收该候选：Intel visual9／interaction9.1／motion8.5，Windows 与 ARM 原分数分别保留，不平均、不用正式采集或诊断B抵消原始A。正常 ARM CDP 60.22155321151912／59.28990924304912 fps 与 SCK whole 57.4999999999986／56.42857142857235 fps 原 passed=true；Intel CDP 29.680599324439076／29.249667689740264 fps、SCK whole 26.01303154825863／27.37552248580128 fps 原 passed=false。六个独立 observer ABA case 功能 complete=true、每项10真实声源／计数／保存与正面结束；六项原 wholeFPS=false仍保留，不替代正式 baseline。clocks-A-original 完整原帧44→45 131.00935700003902ms、46→49 136.11220999996476ms，47／48原 idle 无伪造像素；源间隔324.9／262.6ms附近真实停顿未覆盖为通过。四帧 BGRA／PNG 全RGBA、六sidecar原PTS／status、官方ZIP／源Git blob／原日志与8787个安全成员SHA／大小／闭包已核对。额外时钟探针、67ms长任务、宿主或产品合成路径的因果仍未确立；B较快不是修复证明，物理听感和真实Microsoft上传等未覆盖项保留。原冻结清单 release/validation-1.1.9/Delivery/History/native-e81ddc7-job-success-independent-intel-motion-rejection/evidence.json（SHA256 32fd38f74973190f166caa4d09f81fc33ed2abe9597ab43d0d58618790f3d284）；宽scope完整评审仅引用原size／SHA，精简公共评审与14个原失败BGRA／PNG／JSON完整保留。

历史完整原始证据：KAMUCL-1.1.9-validation-history-part001.zip、KAMUCL-1.1.9-validation-history-part002.zip、KAMUCL-1.1.9-validation-history-part003.zip、KAMUCL-1.1.9-validation-history-part004.zip、KAMUCL-1.1.9-validation-history-part005.zip，共 5 卷，每卷小于 2 GB；索引 KAMUCL-1.1.9-validation-history-index.json（SHA256 9284b34bda62cdce1b14b9fda11953b3326c201df4e174ca2a4b34fd2141fca4）。已核对当前 155056 个成员、原字节与分卷摘要，保留干净解压回执。临时浏览器 profile／缓存仅在公开排除记录中列明；原 out 与真实用户数据未删改。

仍未覆盖：物理扬声器／耳机的实际掌击听感及爆音感知，真实 Microsoft 登录／皮肤上传，社区两平台真实在线收藏安装，Mac 实际用户 PCL 包或私有存档游玩，公网 NAT、长期稳定性、磁盘耗尽、全部缩放与无中键设备实物验证，Developer ID 签名／公证。数字峰值与声源检查不替代听感。Mac 本轮工作和输出暂停，未声明任何 Mac 合格；实例专属图片优先主要依据源码／测试。

本文只记录经门控的本地验收；源码／handoff 干净构建与运输复验、外层 SHA256、远端 master／main、精确标签及公开附件核对由发行步骤另行确认，不提前宣称发布成功。main 保留独立历史，通过 cherry-pick 同步，不操作 wuhui。
