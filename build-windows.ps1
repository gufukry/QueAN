# QueAN Windows 打包脚本
# 用法：在项目目录右键“使用 PowerShell 运行”，或在本窗口执行：
#   powershell -ExecutionPolicy Bypass -File .\build-windows.ps1
# 作用：设置国内镜像（避免从 GitHub 慢速下载 Electron），然后构建便携 EXE。

$ErrorActionPreference = "Stop"

$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR = "https://npmmirror.com/mirrors/electron-builder-binaries/"

Write-Host "==> 安装依赖 (npm install)" -ForegroundColor Cyan
npm install
if ($LASTEXITCODE -ne 0) { Write-Host "npm install 失败" -ForegroundColor Red; exit 1 }

Write-Host "==> 构建便携 EXE (electron-builder --win portable)" -ForegroundColor Cyan
npx electron-builder --win portable --x64 --publish never
if ($LASTEXITCODE -ne 0) { Write-Host "打包失败" -ForegroundColor Red; exit 1 }

Write-Host "==> 完成，产物在 release\ 目录" -ForegroundColor Green
Get-ChildItem release -Filter *.exe | ForEach-Object { Write-Host ("  " + $_.Name) }
