import fs from 'node:fs'
import path from 'node:path'
import { randomUUID, createHash } from 'node:crypto'
import type { ReleaseInfo } from '../../shared/types'

export interface UpdateTransaction {
  schema: 1
  id: string
  target: string
  file: string
  sha256: string
  size: number
  from: string
  release: ReleaseInfo
  mode: 'upgrade' | 'rollback' | 'local'
  helperPid?: number
}

export const PORTABLE_RUNTIME_DIR = 'QueAN-runtime'

/**
 * 便携运行时目录：exe 的祖先里名为 QueAN-runtime 的那一层（electron-builder 便携包固定结构）。
 * 该目录在 Windows 上被设为隐藏，更新暂存放进去就不会在桌面/目录里多出文件夹。
 */
export function portableRuntimeDirOf(execPath: string): string | null {
  let dir = path.dirname(execPath)
  for (let depth = 0; depth < 4; depth++) {
    if (path.basename(dir) === PORTABLE_RUNTIME_DIR) return dir
    const parent = path.dirname(dir)
    if (parent === dir) break
    dir = parent
  }
  return null
}

/** 更新暂存目录：优先放进（已隐藏的）便携运行时目录，非便携运行退回 exe 同级。 */
export function updateStageDir(exe: string): string {
  return path.join(portableRuntimeDirOf(exe) || path.dirname(exe), 'QueAN-update')
}

export const updateMarker = (exe: string): string => path.join(updateStageDir(exe), '.queanupdate')
export function atomicUpdateJson(file: string, data: unknown): void {
  fs.mkdirSync(path.dirname(file), { recursive: true })
  const tmp = file + '.' + randomUUID() + '.tmp'
  const fd = fs.openSync(tmp, 'wx')
  try { fs.writeFileSync(fd, JSON.stringify(data, null, 2)); fs.fsyncSync(fd) } finally { fs.closeSync(fd) }
  try { fs.renameSync(tmp, file) } finally { fs.rmSync(tmp, { force: true }) }
}

export function readUpdateTransaction(file: string, target: string): UpdateTransaction | null {
  try {
    const t = JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '')) as UpdateTransaction
    const rel = path.relative(updateStageDir(target), t.file)
    const key = (p: string) => process.platform === 'win32' ? path.resolve(p).toLowerCase() : path.resolve(p)
    if (t.schema !== 1 || !/^[a-f\d-]{36}$/i.test(t.id) || key(t.target) !== key(target)
      || !rel || rel.startsWith('..') || path.isAbsolute(rel) || !/^[a-f\d]{64}$/i.test(t.sha256)
      || !Number.isSafeInteger(t.size) || t.size <= 0 || !/^\d+\.\d+\.\d+$/.test(t.release?.version)
      || !['upgrade', 'rollback', 'local'].includes(t.mode)) return null
    return t
  } catch { return null }
}

export async function validateUpdatePayload(t: UpdateTransaction): Promise<void> {
  const stat = await fs.promises.lstat(t.file)
  if (!stat.isFile() || stat.isSymbolicLink() || stat.size !== t.size) throw new Error('更新包大小或文件类型已变化，请重新下载')
  const hash = createHash('sha256')
  for await (const chunk of fs.createReadStream(t.file)) hash.update(chunk)
  if (hash.digest('hex') !== t.sha256) throw new Error('更新包 SHA256 已变化，请重新下载')
}

export interface UpdaterScriptSpec {
  oldExe: string
  newExe: string
  mainPid: number
  wrapperPid?: number
  stateDir: string
  transaction: UpdateTransaction
  oldSha256: string
  launchArguments?: string
}

/** One claimed job, one replacement, one launch. A missing receipt never triggers a rollback/restart loop. */
export function buildUpdaterScript(spec: UpdaterScriptSpec): string {
  const q = (s: string) => `'${s.replace(/'/g, "''")}'`
  const t = spec.transaction
  const claim = updateMarker(spec.oldExe) + '.applying'
  return `$ErrorActionPreference = 'Stop'
$env:PSModulePath = (Join-Path $PSHOME 'Modules') + [IO.Path]::PathSeparator + $env:PSModulePath
$oldExe = ${q(spec.oldExe)}
$newExe = ${q(spec.newExe)}
$backup = Join-Path ([IO.Path]::GetTempPath()) ${q(t.id + '.exe.bak')}
$stateDir = ${q(spec.stateDir)}
$claim = ${q(claim)}
$receipt = $claim + '.receipt.json'
$swap = $oldExe + ${q('.' + t.id + '.swap')}
$expected = ${q(t.sha256)}
$oldHash = ${q(spec.oldSha256)}
$mainPid = ${spec.mainPid}
$wrapperPid = ${spec.wrapperPid ?? 0}
$logFile = Join-Path $stateDir 'updater-last.log'
$replaced = $false
function Log($m) { try { Add-Content -LiteralPath $logFile -Value ((Get-Date -Format 'HH:mm:ss') + ' ' + $m) -Encoding UTF8 } catch {} }
function Hash-File($file) {
  $stream = [IO.File]::OpenRead($file); $sha = [Security.Cryptography.SHA256]::Create()
  try { return ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '').ToLowerInvariant() }
  finally { $stream.Dispose(); $sha.Dispose() }
}
function Write-Json($file, $value) {
  $temp = $file + '.tmp'
  [IO.File]::WriteAllText($temp, ($value | ConvertTo-Json -Depth 8), (New-Object Text.UTF8Encoding($false)))
  Move-Item -LiteralPath $temp -Destination $file -Force
}
try {
  Log 'Applying claimed update on user startup'
  for ($n = 0; $n -lt 90; $n++) {
    if (-not (Get-Process -Id $mainPid -ErrorAction SilentlyContinue) -and (-not $wrapperPid -or -not (Get-Process -Id $wrapperPid -ErrorAction SilentlyContinue))) { break }
    Start-Sleep -Milliseconds 500
  }
  if ((Hash-File $newExe) -ne $expected) { throw 'Update payload checksum changed' }
  if ((Hash-File $oldExe) -ne $oldHash) { throw 'Target changed; refusing replacement' }
  Copy-Item -LiteralPath $newExe -Destination $swap
  if ((Hash-File $swap) -ne $expected) { throw 'Staged copy checksum mismatch' }
  # Replace is atomic on the same volume. The scratch target keeps it atomic without
  # leaving a backup behind: no missing-exe interval, no leftover folder.
  for ($n = 0; $n -lt 60; $n++) {
    if ((Hash-File $oldExe) -ne $oldHash) { throw 'Target changed while waiting; refusing replacement' }
    try { [IO.File]::Replace($swap, $oldExe, $backup); $replaced = $true; break }
    catch { if ($n -eq 59) { throw }; Start-Sleep -Milliseconds 500 }
  }
  # The user's startup is continued once, using the original filename/shortcut.
  $start = New-Object Diagnostics.ProcessStartInfo
  $start.FileName = $oldExe
  $start.WorkingDirectory = [IO.Path]::GetDirectoryName($oldExe)
  $start.Arguments = ${q(spec.launchArguments ?? '')}
  $start.UseShellExecute = $false
  [Diagnostics.Process]::Start($start) | Out-Null
  for ($n = 0; $n -lt 240; $n++) {
    if (Test-Path -LiteralPath $receipt) {
      $ack = Get-Content -LiteralPath $receipt -Raw | ConvertFrom-Json
      if ($ack.id -eq ${q(t.id)} -and $ack.version -eq ${q(t.release.version)}) {
        Log 'Renderer startup receipt verified'
        Move-Item -LiteralPath $claim -Destination ($claim + '.completed') -Force
        if (-not (Test-Path -LiteralPath ${q(updateMarker(spec.oldExe))})) {
          try { if ((Hash-File $newExe) -eq $expected) { Remove-Item -LiteralPath $newExe -Force } } catch { Log 'Staged package cleanup deferred' }
        }
        exit 0
      }
    }
    Start-Sleep -Milliseconds 500
  }
  throw 'Replacement completed; startup receipt not received. No automatic rollback or relaunch.'
} catch {
  Log $_.Exception.Message
  Log $_.ScriptStackTrace
  try { Set-Content -LiteralPath (Join-Path $stateDir 'update-failed.flag') -Value $_.Exception.Message -Encoding UTF8 } catch {}
  try { Move-Item -LiteralPath $claim -Destination ($claim + '.failed') -Force } catch {}
  # A pre-replacement error leaves the original executable intact. After a successful
  # replacement the new executable stays in place; there is no backup to roll back to.
} finally {
  Remove-Item -LiteralPath $backup -Force -ErrorAction SilentlyContinue
  Remove-Item -LiteralPath $swap -Force -ErrorAction SilentlyContinue
  Remove-Item -LiteralPath $MyInvocation.MyCommand.Path -Force -ErrorAction SilentlyContinue
}
`
}
