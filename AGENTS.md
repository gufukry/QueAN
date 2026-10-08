# 项目规则（QuaAN）

本仓库是 QuaAN —— 基于 KAMUCL v1.1.15 二开的 Minecraft Java 启动器（MIT）。
原始上游：https://github.com/kamubaba-i/KAMUCL 。本仓库（发布目标）：https://github.com/gufukry/QueAN

## 交付流程

- 每批功能或修复完成后，运行与本批改动相关的验证与构建：`npm test`、`npx tsc --noEmit`、`npm run license:check`。
- Windows 包由 GitHub Actions 工作流 `.github/workflows/windows-build.yml` 在 GitHub 的 Windows 机器上自动构建（便携 EXE、ZIP、SHA256SUMS）。本地容器为 aarch64 Linux，无法直接出 Windows EXE。
- 推送源码到本仓库 `origin/master`（`https://github.com/gufukry/QueAN`）。除非用户明确要求，不推送到任何其它仓库。
- 版本号写在 `package.json`，需与 `package-lock.json` 根版本、`src/shared/updateNotes.ts` 内置更新日志保持同步；更新日志日期用本地时间 `YYYY-MM-DD HH:mm`。

## 约定

- 品牌与渠道常量集中在 `src/shared/branding.ts`（自更新仓库、内测群号），改这里，不要散落代码深处。
- 保留 `LICENSE`（MIT）与 `THIRD_PARTY_NOTICES.md`，不改写原始版权声明。
- 保留用户既有改动，不结束他人的游戏进程。
- 用户当次明确要求优先：如当次要求只分析、暂不执行或暂不发布，以当次指令为准。
