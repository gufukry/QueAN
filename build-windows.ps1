# QueAN Windows build helper (ASCII only, safe for Windows PowerShell 5.1)
# Usage: open PowerShell in this folder, then run:
#   powershell -ExecutionPolicy Bypass -File .\build-windows.ps1
# It sets China mirrors (fast Electron download), installs deps and builds the portable EXE.
# The window stays open until you press Enter.

$ErrorActionPreference = "Continue"

$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR = "https://npmmirror.com/mirrors/electron-builder-binaries/"

try {
  Write-Host "==> npm install" -ForegroundColor Cyan
  npm install

  Write-Host "==> npm run build (electron-vite)" -ForegroundColor Cyan
  npm run build

  Write-Host "==> build portable EXE (electron-builder --win portable)" -ForegroundColor Cyan
  npx electron-builder --win portable --x64 --publish never

  Write-Host "==> done. Output is in the .\release folder:" -ForegroundColor Green
  Get-ChildItem release -Filter *.exe -ErrorAction SilentlyContinue | ForEach-Object { Write-Host ("  " + $_.Name) }
}
catch {
  Write-Host ("==> ERROR: " + $_.Exception.Message) -ForegroundColor Red
}

Write-Host ""
Read-Host "Press Enter to close"
