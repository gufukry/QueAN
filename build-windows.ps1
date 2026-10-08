# QueAN Windows 打包脚本
# 用法：在项目目录右键“使用 PowerShell 运行”，或在已打开的 PowerShell 窗口执行：
#   powershell -ExecutionPolicy Bypass -File .\build-windows.ps1
# 作用：设置国内镜像（避免从 GitHub 慢速下载 Electron），然后构建便携 EXE。
# 结尾会暂停，窗口不会自动关闭。

$ErrorActionPreference = "Continue"

$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR = "https://npmmirror.com/mirrors/electron-builder-binaries/"

try {
  Write-Host "==> 安装依赖 (npm install)" -ForegroundColor Cyan
  npm install

  Write-Host "==> 构建便携 EXE (electron-builder --win portable)" -ForegroundColor Cyan
  npx electron-builder --win portable --x64 --publish never

  Write-Host "==> 完成，产物在 release\ 目录" -ForegroundColor Green
  Get-ChildItem release -Filter *.exe -ErrorAction SilentlyContinue | ForEach-Object { Write-Host ("  " + $_.Name) }
}
catch {
  Write-Host ("==> 出错：" + $_.Exception.Message) -ForegroundColor Red
}

Write-Host ""
Read-Host "按回车键关闭窗口"
