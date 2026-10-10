/** QueAN HTTP transfer service, implemented from the application contracts.
 * SPDX-License-Identifier: MIT
 * HTTP semantics: RFC 9110 (status codes, Range and Content-Range).
 * No PCL source is used by this replacement. Historical provenance remains in Git.
 */
import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import { downloadLimiter } from './downloadLimits'
import { withFileJob } from './fileJobs'
import { downloadFetch } from './downloadFetch'
import { abortableDelay, inheritTaskControl, isTaskPaused, waitIfTaskPaused } from './tasks'
import { DownloadProgressTracker, SmoothedSpeedEstimator, type DownloadProgressSnapshot } from './downloadProgress'
import { DownloadSourcePool } from './downloadSources'
import { resolveNativeIntegrity } from './platformNatives'

export type MirrorPref = 'official' | 'bmclapi'
export type ProgressFn = (done: number, total: number, networkBytes?: number) => void
export interface DownloadBatchProgress extends DownloadProgressSnapshot { activeFiles?: string[]; speedBps: number; etaSeconds: number | null; paused: boolean }
export type AllProgressFn = (done: number, total: number, speedBps: number, detail: DownloadBatchProgress) => void
export interface DownloadTask { label?: string; url: string; urls?: string[]; dest: string; sha1?: string; sha512?: string; sha256?: string; size?: number; reuseDirs?: string[]; reuseFiles?: string[]; nativeChecksumUrl?: string }
interface Integrity { sha1?: string; sha512?: string; sha256?: string; size?: number; systemProxy?: boolean }
export const BMCL_MAVEN_ROOT = 'https://bmclapi2.bangbang93.com/maven/'
export function mirrorUrl(input: string, mirror: MirrorPref): string {
  if (mirror === 'official') return input
  try {
    const u = new URL(input)
    if (!['https:', 'http:'].includes(u.protocol)) return input
    const h = u.hostname, p = u.pathname, q = u.search, base = 'https://bmclapi2.bangbang93.com'
    if (['piston-meta.mojang.com','piston-data.mojang.com','launchermeta.mojang.com','launcher.mojang.com'].includes(h)) return base + p + q
    if (h === 'resources.download.minecraft.net') return base + '/assets' + p + q
    if (['libraries.minecraft.net','maven.fabricmc.net','maven.minecraftforge.net'].includes(h)) return BMCL_MAVEN_ROOT + p.slice(1) + q
    if (h === 'maven.neoforged.net' && p.startsWith('/releases/')) return BMCL_MAVEN_ROOT + p.slice(10) + q
    if (h === 'files.minecraftforge.net' && p.startsWith('/maven/')) return base + p + q
    if ((h === 'cdn.modrinth.com' && /^\/data\/[^/]+\/versions\//.test(p)) || (['edge.forgecdn.net','mediafilez.forgecdn.net'].includes(h) && /^\/files\/\d+\/\d+\//.test(p))) return 'https://mod.mcimirror.top' + p + q
  } catch { /* Non-URL inputs retain their original validation error. */ }
  return input
}
export function downloadCandidates(urls: string[], mirror: MirrorPref): string[] {
  return [...new Set(urls.flatMap(url => {
    const variants = [url]
    try {
      const u = new URL(url)
      if (u.protocol === 'https:' && ['edge.forgecdn.net', 'mediafilez.forgecdn.net'].includes(u.hostname) && /^\/files\/\d+\/\d+\//.test(u.pathname)) {
        // Same immutable CDN object, independent of the edge redirect cache.
        u.hostname = 'mediafilez.forgecdn.net'; variants.unshift(u.href)
        u.hostname = 'edge.forgecdn.net'; variants.push(u.href)
      }
    } catch { /* Keep URL validation in the transfer. */ }
    // 内容 CDN（Modrinth / CurseForge）直连原站就能跑满并发（美西螈正是直连 18MB/s），
    // 镜像作为兜底排在后面；其余主机仍按「镜像优先」。
    const hosted = (() => {
      try {
        const host = new URL(url).hostname
        return host === 'cdn.modrinth.com' || host === 'edge.forgecdn.net' || host === 'mediafilez.forgecdn.net'
      } catch { return false }
    })()
    const mirrored = mirrorUrl(url, mirror)
    const candidates = hosted
      ? [url, ...(mirrored !== url ? [mirrored] : [])]
      : [...(mirror === 'official' ? [] : [mirrored]), ...variants]
    // 即使是官方源也保留 BMCLAPI 作为最后一档兜底：maven.neoforged.net 这类主机在部分网络下
    // 完全不通，而镜像只是同一份不可变对象的代理，仍按 sha1/sha512 校验，不会降低安全性。
    const fallback = mirror === 'official' ? mirrorUrl(url, 'bmclapi') : (hosted ? mirrorUrl(url, 'bmclapi') : '')
    if (fallback && fallback !== url && !candidates.includes(fallback)) candidates.push(fallback)
    return candidates
  }))]
}
export type HttpFailureKind = 'unavailable' | 'transient' | 'fatal'
export function classifyHttpStatus(status: number): HttpFailureKind {
  if ([404,410].includes(status)) return 'unavailable'
  // Downloads never send conditional validators. A bodyless 304 cannot satisfy
  // the file contract; retry it within the existing budget, then fail normally.
  return [304,408,425,429].includes(status) || (status >= 500 && status < 600 && ![501,505].includes(status)) ? 'transient' : 'fatal'
}
export class DownloadHttpError extends Error {
  constructor(readonly status: number, readonly url: string) { super(`HTTP ${status}: ${url}`); this.name = 'DownloadHttpError' }
}
class InvalidContent extends Error {}
class NetworkIdle extends Error {}
export const transferTimeouts = {
  /** 基准「无数据」超时：对齐 PCL 的 max(连接均值, 15s) 下限 */
  inactivityMs: 15_000,
  /** 无数据超时上限（PCL: min(基准 ×(1+该源失败次数), 30s)） */
  maxInactivityMs: 30_000,
  /** PCL 的「速度过慢」：数据包间隔超过该值（毫秒）才进入判定 */
  slowGapMs: 5_000,
  alternativeHeadersMs: 2500
}
const failures = new Map<string, { count: number; until: number }>()
const origin = (url: string) => { try { return new URL(url).origin } catch { return url } }
const serverCooldowns = new Map<string, number>()
export function retryAfterTime(value: string | null, now = Date.now()): number {
  if (!value) return now + 30_000
  const seconds = Number(value)
  const until = Number.isFinite(seconds) ? now + Math.max(0, seconds) * 1000 : Date.parse(value)
  return Number.isFinite(until) ? Math.max(now, until) : now + 30_000
}
export function noteHostFailure(url: string): void {
  const key = origin(url), count = (failures.get(key)?.count ?? 0) + 1
  // 国内链路抖动多：要连续失败 3 次才冷却，且最长只冷 60s（原为 2 次 / 最长 10 分钟，
  // 会把一个只是抖了一下的镜像长时间踢出候选）。
  failures.set(key, { count, until: count < 3 ? 0 : Date.now() + Math.min((count - 2) * 30_000, 60_000) })
}
export function noteHostSuccess(url: string): void { failures.delete(origin(url)) }
export function noteHostsRecovered(): void { failures.clear() }
export function resetHostHealthForTest(): void { failures.clear() }
let diskProbe: ((dir: string) => { bavail: number; bsize: number }) | null = null
export function setStatfsProbeForTest(probe: typeof diskProbe): void { diskProbe = probe }
export function fetchSignal(signal?: AbortSignal): AbortSignal {
  const timeout = AbortSignal.timeout(120_000)
  return signal ? AbortSignal.any([signal, timeout]) : timeout
}
export async function verifyFile(file: string, expected: Integrity, signal?: AbortSignal): Promise<string | null> {
  signal?.throwIfAborted()
  let stat: fs.Stats
  try { stat = await fs.promises.stat(file) } catch (e) { if ((e as NodeJS.ErrnoException).code === 'ENOENT') return '文件缺失'; throw e }
  if (!stat.isFile()) return '路径不是文件'
  if (expected.size !== undefined && stat.size !== expected.size) return '文件大小不符'
  if (!stat.size && expected.size !== 0) return '空文件'
  const checks = (['sha1','sha512','sha256'] as const).filter(key => !!expected[key]).map(key => ({ key, hash: crypto.createHash(key) }))
  if (checks.length) {
    const stream = fs.createReadStream(file, { signal })
    for await (const bytes of stream) { signal?.throwIfAborted(); for (const check of checks) check.hash.update(bytes) }
    for (const check of checks) if (check.hash.digest('hex').toLowerCase() !== expected[check.key]!.toLowerCase()) return `${check.key} 校验失败`
  }
  signal?.throwIfAborted()
  return null
}
async function reuse(dest: string, expected: Integrity, roots: string[], signal?: AbortSignal, files: string[] = []): Promise<boolean> {
  if (!expected.sha1 && !expected.sha512 && !expected.sha256) return false
  const copy = async (file: string) => {
    const normalize = (value: string) => process.platform === 'win32' ? path.resolve(value).toLowerCase() : path.resolve(value)
    if (normalize(file) === normalize(dest)) return false
    // A bound folder may be offline/read-protected. It is only an optional cache;
    // target write failures still propagate instead of hiding a disk problem.
    try { if (await verifyFile(file, expected, signal)) return false } catch { signal?.throwIfAborted(); return false }
    try { await fs.promises.copyFile(file, dest + '.part') } catch (error) {
      signal?.throwIfAborted()
      if ((error as NodeJS.ErrnoException).code === 'ENOENT') return false
      throw error
    }
    if (await verifyFile(dest + '.part', expected, signal)) { await fs.promises.rm(dest + '.part', { force: true }); return false }
    signal?.throwIfAborted()
    await fs.promises.rename(dest + '.part', dest)
    return true
  }
  // Assets and Maven libraries have exact content/relative-path identities. Do
  // not recursively walk every other instance for each of thousands of assets.
  for (const file of new Set(files)) if (await copy(file)) return true
  const own = path.resolve(path.dirname(dest)), extension = path.extname(dest).toLowerCase()
  const queue = roots.map(dir => ({ dir: path.resolve(dir), depth: 0 }))
  let visited = 0
  while (queue.length && visited++ < 10000) {
    signal?.throwIfAborted()
    const item = queue.shift()!
    if (item.dir === own) continue
    let entries: fs.Dirent[]
    try { entries = await fs.promises.readdir(item.dir, { withFileTypes: true }) } catch { continue }
    for (const entry of entries) {
      const file = path.join(item.dir, entry.name)
      if (entry.isDirectory() && item.depth < 8) queue.push({ dir: file, depth: item.depth + 1 })
      if (!entry.isFile() || path.extname(entry.name).toLowerCase() !== extension) continue
      if (await copy(file)) return true
    }
  }
  return false
}
/** One request owns one limiter slot, private file handle and cancellation controller. */
async function receive(url: string, temporary: string, expected: Integrity, signal: AbortSignal | undefined, progress: ProgressFn | undefined, hasAlternative: boolean, range?: { start: number; end: number }, connected?: (url: string) => void, sources?: DownloadSourcePool, observationUrl = url): Promise<number> {
  await waitIfTaskPaused(signal)
  // Respect source rate limits across all files, including parallel range workers.
  while ((serverCooldowns.get(origin(url)) ?? 0) > Date.now()) {
    await abortableDelay(Math.min(1000, serverCooldowns.get(origin(url))! - Date.now()), signal)
  }
  // 每个域名都限并发（镜像 BMCLAPI 更紧并额外错峰，避免被 403/429 限频）。
  const release = await downloadLimiter.acquire(signal, origin(url))
  const controller = new AbortController()
  const abort = () => controller.abort(signal?.reason)
  signal?.addEventListener('abort', abort, { once: true })
  let response: Response | undefined, reader: ReadableStreamDefaultReader<Uint8Array> | undefined, file: fs.promises.FileHandle | undefined
  let timer: ReturnType<typeof setInterval> | undefined, waiting = false, sinceData = 0, elapsed = 0, transferred = 0, lastDataAt = 0
  let last = performance.now()
  let headersMs = 0, bodyMs = 0, remainingBytes = expected.size ?? Infinity
  try {
    signal?.throwIfAborted()
    let offset = 0
    try { offset = (await fs.promises.stat(temporary)).size } catch { /* First request. */ }
    // Cross-source continuation without a hash cannot establish representation identity.
    if (!expected.sha1 && !expected.sha512 && !expected.sha256) offset = 0
    const required = range ? range.end - range.start + 1 : expected.size
    // A cancelled/late EOF may leave every byte already on disk. Never send
    // Range: bytes=size- (HTTP 416); the caller still verifies the full hash.
    if (offset && required === offset && (range || !await verifyFile(temporary, expected, signal))) {
      progress?.(offset, required, 0)
      return offset
    }
    if (required !== undefined && offset >= required) offset = 0
    const headers: Record<string,string> = { 'accept-encoding': 'identity' }
    if (range) headers.Range = `bytes=${range.start + offset}-${range.end}`
    else if (offset) headers.Range = `bytes=${offset}-`
    timer = setInterval(() => {
      const now = performance.now(), dt = now - last; last = now
      if (!waiting || isTaskPaused(signal) || downloadLimiter.isThrottling) return
      sinceData += dt; elapsed += dt
      // PCL「无数据超时」：min(max(连接均值,15s)×(1+该源失败次数), 30s)
      const idleLimit = Math.min(transferTimeouts.inactivityMs * (1 + (failures.get(origin(url))?.count ?? 0)), transferTimeouts.maxInactivityMs)
      if (lastDataAt > 0 && sinceData >= idleLimit) controller.abort(new NetworkIdle('下载网络停滞'))
      // 有备用来源时，响应头迟迟不来就切源（PCL 的“慢速重试/快速重试”思路）
      if (!response && hasAlternative && elapsed >= transferTimeouts.alternativeHeadersMs) controller.abort(new NetworkIdle('下载源响应缓慢，切换备用来源'))
    }, Math.max(10, Math.min(200, transferTimeouts.inactivityMs / 4)))
    waiting = true
    const headerStart = performance.now()
    response = await downloadFetch(url, { signal: controller.signal, headers, bodyTimeoutMs: 120_000, separateConnection: !!range, systemProxy: expected.systemProxy })
    headersMs = performance.now() - headerStart
    waiting = false; sinceData = 0
    if (!response.ok) {
      if (response.status === 429 || (response.status === 503 && response.headers.has('retry-after'))) {
        if (serverCooldowns.size > 128) for (const [host, until] of serverCooldowns) if (until <= Date.now()) serverCooldowns.delete(host)
        serverCooldowns.set(origin(url), Math.max(serverCooldowns.get(origin(url)) ?? 0, retryAfterTime(response.headers.get('retry-after'))))
      }
      throw new DownloadHttpError(response.status, url)
    }
    if (!response.body) throw new InvalidContent('下载响应没有内容')
    connected?.(response.url || url)
    // 连接已建立即开始计「无数据超时」（PCL 用读超时，语义等价）：
    // 服务端一直不发正文时，不能无上限地等。
    lastDataAt = performance.now()
    let total = range ? range.end - range.start + 1 : expected.size ?? 0
    if (response.status === 206) {
      const parsed = /^bytes (\d+)-(\d+)\/(\d+)$/.exec(response.headers.get('content-range') ?? '')
      if (!parsed || Number(parsed[1]) !== (range?.start ?? 0) + offset || Number(parsed[2]) < Number(parsed[1]) || Number(parsed[2]) >= Number(parsed[3]) || (range && Number(parsed[2]) !== range.end)) throw new InvalidContent('Content-Range 不正确')
      if (expected.size !== undefined && Number(parsed[3]) !== expected.size) throw new InvalidContent('响应总大小不符')
      if (!range) total = Number(parsed[3])
    } else {
      if (range) throw new InvalidContent('服务器不支持范围请求')
      offset = 0
      const length = Number(response.headers.get('content-length'))
      if (!total && length > 0) total = length
    }
    file = await fs.promises.open(temporary, offset ? 'a' : 'w')
    reader = response.body.getReader()
    let done = offset
    remainingBytes = total ? total - done : Infinity
    progress?.(done, total, 0)
    while (true) {
      await waitIfTaskPaused(signal)
      signal?.throwIfAborted()
      waiting = true
      const readStart = performance.now()
      const chunk = await reader.read()
      bodyMs += performance.now() - readStart
      waiting = false; sinceData = 0
      if (chunk.done) break
      // PCL「速度过慢」：数据包间隔 >5s 且该间隔到位的字节数 < 间隔毫秒数（≈<1KB/s），
      // 且不是单源下载时才断开——单源慢就让它慢慢下完。
      const dataAt = performance.now()
      if (lastDataAt > 0 && hasAlternative && dataAt - lastDataAt > transferTimeouts.slowGapMs && dataAt - lastDataAt > chunk.value.length) {
        controller.abort(new NetworkIdle('下载速度过慢'))
      }
      lastDataAt = dataAt
      await downloadLimiter.consume(chunk.value.length, signal)
      await waitIfTaskPaused(signal)
      let written = 0
      while (written < chunk.value.length) {
        const result = await file.write(chunk.value, written, chunk.value.length - written)
        if (!result.bytesWritten) throw new Error('写入文件失败')
        written += result.bytesWritten
      }
      done += written; transferred += written; remainingBytes = total ? Math.max(0, total - done) : Infinity
      if (total && done > total) throw new InvalidContent('响应数据超出声明大小')
      progress?.(done, total, written)
      // HTTP length/Content-Range plus the final hash define completion, not a
      // delayed transport EOF. Release this slot immediately once bytes arrive.
      if (total && done === total) break
    }
    if (total && done !== total) throw new InvalidContent('下载大小不符')
    await file.sync()
    if (!downloadLimiter.isThrottling && !isTaskPaused(signal)) sources?.observe(observationUrl, headersMs, bodyMs, transferred)
    return done
  } catch (error) {
    signal?.throwIfAborted()
    if (controller.signal.aborted || error instanceof TypeError || error instanceof DownloadHttpError || error instanceof InvalidContent) sources?.fail(observationUrl)
    if (error instanceof Error) Object.assign(error, { downloadUrl: url })
    throw controller.signal.aborted ? controller.signal.reason : error
  } finally {
    clearInterval(timer)
    controller.abort()
    if (reader) { await reader.cancel().catch(() => {}); reader.releaseLock() }
    else await response?.body?.cancel().catch(() => {})
    await file?.close()
    signal?.removeEventListener('abort', abort)
    release()
  }
}

/** Small resumable ranges share a live connection budget and retry independently. */
async function segmented(url: string, dest: string, expected: Integrity, signal: AbortSignal | undefined, progress: ProgressFn | undefined, fallback: boolean, maxSegments: number | (() => number) = () => downloadLimiter.maxConcurrent, alternatives: string[] = [], sourcePool?: DownloadSourcePool): Promise<number> {
  const cache = path.resolve(dest + '.segments-cache'), size = expected.size!
  const clear = async () => { if (path.dirname(cache) !== path.dirname(path.resolve(dest))) throw new Error('缓存路径越界'); await fs.promises.rm(cache, { recursive: true, force: true }) }
  try { const stat = await fs.promises.lstat(cache); if (stat.isSymbolicLink() || !stat.isDirectory()) throw new Error('下载缓存不是普通目录') } catch (error) { if ((error as NodeJS.ErrnoException).code !== 'ENOENT') throw error }
  let previous: { version?: number; size?: number; sha1?: string; sha512?: string; sha256?: string; step?: number } = {}
  try { previous = JSON.parse(await fs.promises.readFile(path.join(cache, 'identity.json'), 'utf8')) } catch {}
  const compatible = previous.version === 1 && previous.size === size && previous.sha1 === expected.sha1 && previous.sha512 === expected.sha512 && previous.sha256 === expected.sha256 && Number.isSafeInteger(previous.step) && previous.step! > 0 && previous.step! <= size
  // Keep old valid fragments, including those created with a different thread setting.
  // 与 PCL 一致：ModNet.FilePieceLimit = 256KB —— 按 256KB 粒度切片，
  // 几 MB 的模组/资源包/光影也会拆成十几条并发（直连 CDN 时才快）。
  const step = compatible ? previous.step! : Math.min(256 * 1024, size)
  const count = Math.ceil(size / step)
  if (!compatible) {
    await clear(); await fs.promises.mkdir(cache, { recursive: true })
    await fs.promises.writeFile(path.join(cache, 'identity.json'), JSON.stringify({ version: 1, size, sha1: expected.sha1, sha512: expected.sha512, sha256: expected.sha256, step }))
  }
  const controller = new AbortController(), cancel = () => controller.abort(signal?.reason)
  inheritTaskControl(signal, controller.signal)
  signal?.addEventListener('abort', cancel, { once: true }); if (signal?.aborted) cancel()
  const received = Array<number>(count).fill(0)
  const sources = [...new Set([url, ...alternatives])].map(url => ({ url, identity: url, active: 0, used: false, disabled: false, failures: 0, rate: 0 }))
  // edge and mediafilez address the same immutable object. Try the direct range endpoint;
  // retain edge in downloadFile's ordinary-GET fallback for CDN Range rejection.
  for (const source of sources) {
    try {
      const u = new URL(source.url)
      if (u.hostname === 'edge.forgecdn.net' && u.protocol === 'https:') {
        u.hostname = 'mediafilez.forgecdn.net'
        if (sources.some(other => other.url === u.href)) source.disabled = true
      }
    } catch {}
  }
  const emit = (wire = 0) => progress?.(received.reduce((a, b) => a + b, 0), size, wire)
  const runRange = async (index: number) => {
    const start = index * step, end = Math.min(size, start + step) - 1, file = path.join(cache, index + '.part')
    if (received[index] === end - start + 1) return
    const tried = new Map<typeof sources[number], number>()
    let lastError: unknown = new Error('没有可用的分片下载地址')
    for (let attempt = 0; attempt < sources.length * 2 + 1; attempt++) {
      controller.signal.throwIfAborted()
      const available = sources.filter(source => !source.disabled && (tried.get(source) ?? 0) < 2)
      if (!available.length) throw lastError
      // Explore each supplied source once, then feed work to the measured faster source.
      // A failed shard changes source without cancelling other healthy requests.
      const ranked = sourcePool?.order(available.map(source => source.identity), end - start + 1)
      available.sort((a, b) => (tried.get(a) ?? 0) - (tried.get(b) ?? 0) || a.failures - b.failures || (ranked ? ranked.indexOf(a.identity) - ranked.indexOf(b.identity) : Number(a.used) - Number(b.used) || b.rate - a.rate) || a.active - b.active)
      const source = available[0], retry = tried.get(source) ?? 0
      source.used = true; source.active++; tried.set(source, retry + 1)
      const before = received[index]; let connectedAt = performance.now()
      try {
        await receive(source.url, file, expected, controller.signal, (done, _total, wire) => { received[index] = done; emit(wire) }, retry === 0, { start, end }, _finalUrl => {
          // Keep the logical origin for shared measurements. Redirect targets
          // may be signed/short-lived and must not become a batch-wide endpoint.
          // Always resolve the original address on the next range/retry. Pinning
          // a mirror's redirect traps all later ranges on one slow/expired node.
          connectedAt = performance.now()
        }, sourcePool, source.identity)
        source.rate = (received[index] - before) * 1000 / Math.max(1, performance.now() - connectedAt)
        source.failures = 0
        return
      } catch (error) {
        controller.signal.throwIfAborted(); lastError = error
        // 因过慢/停滞断开的源就地降权：后续分片优先选更快的源（PCL 断开后换源继续）；
        // 同时标记到共享源池，使同一任务后续文件也优先用更快的源
        if (error instanceof NetworkIdle) {
          source.rate = 0
          source.failures++
          try { sourcePool?.fail(source.identity) } catch { /* 源池标记失败不影响重试 */ }
        }
        if (error instanceof InvalidContent || (error instanceof DownloadHttpError && classifyHttpStatus(error.status) !== 'transient')) source.disabled = true
        else if (error instanceof NetworkIdle || error instanceof TypeError || (error instanceof DownloadHttpError && classifyHttpStatus(error.status) === 'transient')) source.failures++
        else throw error // Disk/permission failures are not network retries.
      } finally { source.active-- }
    }
    throw lastError
  }
  let poll: ReturnType<typeof setInterval> | undefined
  try {
    // Load all saved bytes before reporting; source changes must not reset completed ranges.
    for (let index = 0; index < count; index++) {
      const file = path.join(cache, index + '.part'), length = Math.min(step, size - index * step)
      try {
        const st = await fs.promises.lstat(file)
        if (!st.isFile() || st.isSymbolicLink()) throw new Error('下载分片不是普通文件')
        if (st.size <= length) received[index] = st.size
        else await fs.promises.rm(file)
      } catch (error) { if ((error as NodeJS.ErrnoException).code !== 'ENOENT') throw error }
    }
    emit()
    const seg = Math.floor(typeof maxSegments === 'function' ? maxSegments() : maxSegments) || 1
    const startBudget = Math.max(1, Math.min(16, downloadLimiter.maxConcurrent, seg))
    // 快就继续加：每 0.5s 评估一次，只要加速后总速度没回落就继续翻倍加路，直
    // 到用满配置线程数或链路饱和（加路反而变慢就停手）；慢速链路持续加路到上限。
    const hardCap = Math.max(startBudget, Math.min(downloadLimiter.maxConcurrent, seg))
    let budget = startBudget
    let meterBytes = received.reduce((a, b) => a + b, 0), meterAt = performance.now(), lastSpeed = -1
    await new Promise<void>((resolve, reject) => {
      let cursor = 0, active = 0, failure: unknown
      const pump = () => {
        if (controller.signal.aborted) failure ??= controller.signal.reason
        if (failure) { if (!active) reject(failure); return }
        const now = performance.now()
        if (budget < hardCap && cursor < count && now - meterAt >= 500) {
          const bytes = received.reduce((a, b) => a + b, 0)
          const speed = (bytes - meterBytes) * 1000 / (now - meterAt)
          meterBytes = bytes; meterAt = now
          if (lastSpeed < 0 || speed >= lastSpeed * 0.95 || speed < 256 * 1024) budget = Math.min(hardCap, budget + Math.max(1, Math.round(budget / 4)))
          lastSpeed = speed
        }
        while (active < budget && cursor < count && !isTaskPaused(signal)) {
          const index = cursor++
          if (received[index] === Math.min(step, size - index * step)) continue
          active++
          void runRange(index).catch(error => { failure ??= error; controller.abort(error) }).finally(() => { active--; pump() })
        }
        if (cursor >= count && !active) resolve()
      }
      poll = setInterval(pump, 100)
      pump()
    })
    const output = await fs.promises.open(dest + '.part', 'w')
    try {
      for (let index = 0; index < count; index++) {
        for await (const chunk of fs.createReadStream(path.join(cache, index + '.part'), { signal })) {
          const data = Buffer.from(chunk); let offset = 0
          while (offset < data.length) { const result = await output.write(data, offset, data.length - offset); if (!result.bytesWritten) throw new Error('分片合并写入失败'); offset += result.bytesWritten }
        }
      }
      await output.sync()
    } finally { await output.close() }
    const invalid = await verifyFile(dest + '.part', expected, signal)
    if (invalid) { await clear(); throw new InvalidContent(invalid) }
    await clear(); return size
  } catch (error) {
    signal?.throwIfAborted()
    if (error instanceof InvalidContent || (error instanceof DownloadHttpError && [403,404,416].includes(error.status))) {
      // Never throw away good fragments just because one CDN rejects Range. A plain
      // response is a final compatibility path, and is still verified before commit.
      await fs.promises.rm(dest + '.part', { force: true })
      const bytes = await receive(url, dest + '.part', expected, signal, progress, fallback, undefined, undefined, sourcePool)
      const invalid = await verifyFile(dest + '.part', expected, signal)
      if (invalid) throw new InvalidContent(invalid)
      await clear()
      return bytes
    }
    throw error
  } finally { clearInterval(poll); signal?.removeEventListener('abort', cancel); controller.abort() }
}

export async function downloadFile(url: string, dest: string, progress?: ProgressFn, sha1?: string, mirror: MirrorPref = 'official', signal?: AbortSignal, alternatives: string[] = [], integrity: { sha512?: string; sha256?: string; size?: number; reuseDirs?: string[]; reuseFiles?: string[]; systemProxy?: boolean; maxAttempts?: number; maxSegments?: number | (() => number); sourcePool?: DownloadSourcePool } = {}): Promise<void> {
  return withFileJob(dest, signal, async () => {
    const expected = { sha1, sha512: integrity.sha512, sha256: integrity.sha256, size: integrity.size, systemProxy: integrity.systemProxy }, temporary = dest + '.part'
    const attempts = Math.max(1, Math.min(4, integrity.maxAttempts ?? 3))
    await fs.promises.mkdir(path.dirname(dest), { recursive: true })
    try {
      if (!await verifyFile(dest, expected, signal)) { const size = (await fs.promises.stat(dest)).size; progress?.(size, size); return }
      if (!await verifyFile(temporary, expected, signal) && (sha1 || integrity.sha512 || integrity.sha256)) { signal?.throwIfAborted(); await fs.promises.rename(temporary, dest); return }
      if (await reuse(dest, expected, integrity.reuseDirs ?? [], signal, integrity.reuseFiles)) { const size = (await fs.promises.stat(dest)).size; progress?.(size, size); return }
      if ((expected.size ?? 0) >= 50 * 1024 * 1024) {
        const space = diskProbe ? diskProbe(path.dirname(dest)) : await fs.promises.statfs(path.dirname(dest))
        if (Number(space.bavail) * Number(space.bsize) < expected.size!) throw new Error('磁盘空间不足')
      }
      const supplied = downloadCandidates([url, ...alternatives], mirror)
      const candidates = (integrity.sourcePool?.order(supplied, expected.size) ?? supplied).sort((a,b) => Number(Math.max(failures.get(origin(a))?.until ?? 0, serverCooldowns.get(origin(a)) ?? 0) > Date.now()) - Number(Math.max(failures.get(origin(b))?.until ?? 0, serverCooldowns.get(origin(b)) ?? 0) > Date.now()))
      let lastError: unknown = new Error('没有下载地址')
      for (let index = 0; index < candidates.length; index++) {
        const source = candidates[index], fallback = index + 1 < candidates.length
        for (let attempt = 0; attempt < attempts; attempt++) {
          try {
            const bytes = (expected.size ?? 0) >= 256 * 1024 && downloadLimiter.maxConcurrent >= 2 && (sha1 || integrity.sha512 || integrity.sha256)
              ? await segmented(source, dest, expected, signal, progress, fallback, integrity.maxSegments, candidates.slice(index + 1), integrity.sourcePool)
              : await receive(source, temporary, expected, signal, progress, fallback, undefined, undefined, integrity.sourcePool)
            const invalid = await verifyFile(temporary, expected, signal)
            if (invalid) { integrity.sourcePool?.fail(source); throw new InvalidContent(invalid) }
            signal?.throwIfAborted()
            await fs.promises.rename(temporary, dest)
            noteHostSuccess(source); progress?.(bytes, bytes)
            return
          } catch (error) {
            signal?.throwIfAborted(); lastError = error
            if (error instanceof InvalidContent) { await fs.promises.rm(temporary, { force: true }); break }
            if (error instanceof DownloadHttpError && classifyHttpStatus(error.status) !== 'transient') break
            noteHostFailure(source)
            // Try a healthy alternative before repeating a failing connection.
            if (fallback) break
            if (attempt + 1 < attempts) await abortableDelay(100 * 2 ** attempt, signal)
          }
        }
      }
      const failure = lastError as Error & { downloadUrl?: string; cause?: { code?: string; message?: string } }
      let address = failure.downloadUrl || url
      try { const u = new URL(address); address = u.origin + u.pathname } catch { /* retain invalid input */ }
      throw new Error(`下载失败：${failure.message || String(lastError)}${failure.cause?.code ? ` (${failure.cause.code})` : ''}\n文件：${path.basename(dest)}\n地址：${address}`, { cause: lastError })
    } finally {
      // No detached stream may keep writing after this promise settles.
      await fs.promises.rm(temporary, { force: true })
    }
  })
}
export async function downloadAll(tasks: DownloadTask[], progress?: AllProgressFn, concurrency = downloadLimiter.maxConcurrent, mirror: MirrorPref = 'official', signal?: AbortSignal): Promise<void> {
  const tracker = new DownloadProgressTracker(), speed = new SmoothedSpeedEstimator(), active = new Map<number,string>()
  let lastFilesAt = 0
  const sourcePool = new DownloadSourcePool()
  // Most workers start large files early; reserve one in four for small files.
  // Overlap per-file latency with bulk transfer instead of deferring thousands
  // of small assets until 99% of bytes are already downloaded. No extra slots.
  const order = tasks.map((task, index) => ({ index, size: task.size ?? 0 })).sort((a, b) => b.size - a.size)
  const workers = Math.min(tasks.length, Math.max(1, Math.floor(concurrency) || 1))
  const mixed = workers >= 4 && order.some(task => task.size >= 512 * 1024)
  const large = mixed ? order.filter(task => task.size >= 512 * 1024) : order
  const small = mixed ? order.filter(task => task.size < 512 * 1024).reverse() : []
  let largeCursor = 0, smallCursor = 0
  const take = (worker: number) => {
    if (mixed && worker % 4 === 3 && smallCursor < small.length) return small[smallCursor++]
    return largeCursor < large.length ? large[largeCursor++] : small[smallCursor++]
  }
  tasks.forEach(task => tracker.add(task.size, task.label ?? path.basename(task.dest))); tracker.seal()
  const controller = new AbortController(), abort = () => controller.abort(signal?.reason)
  signal?.addEventListener('abort', abort, { once: true }); inheritTaskControl(signal, controller.signal)
  let firstError: unknown, networkBytes = 0
  const report = () => {
    const snapshot = tracker.snapshot(), paused = isTaskPaused(signal)
    const sampled = speed.sample(networkBytes, snapshot.bytesTotal == null ? null : Math.max(0, snapshot.bytesTotal - snapshot.bytesDone), performance.now(), paused)
    // 文件清单每 500ms 上报一次：够实时，也不会在大任务里刷爆 IPC
    const withFiles = tasks.length >= 1 && Date.now() - lastFilesAt >= 500
    if (withFiles) lastFilesAt = Date.now()
    progress?.(snapshot.completedFiles, tasks.length, sampled.speedBps, {
      ...snapshot, ...sampled, paused, activeFiles: [...active.values()],
      ...(withFiles ? { files: tracker.files().slice(0, 120), filesTotal: tasks.length } : {})
    })
  }
  report()
  const timer = setInterval(report, 250)
  try {
    signal?.throwIfAborted()
    if (!tasks.length) { progress?.(0,0,0,{completedFiles:0,totalFiles:0,bytesDone:0,bytesTotal:0,fraction:1,indeterminate:false,speedBps:0,etaSeconds:null,paused:false}); return }
    await Promise.all(Array.from({ length: workers }, async (_, worker) => {
      while (!controller.signal.aborted) {
        const next = take(worker)
        if (!next) return
        const index = next.index, task = tasks[index]; active.set(index, task.label ?? path.basename(task.dest))
        try {
          await resolveNativeIntegrity(task, controller.signal)
          await downloadFile(task.url, task.dest, (done,total,wire = 0) => { networkBytes += wire; tracker.record(index,done,total) }, task.sha1, mirror, controller.signal, task.urls, { sha512: task.sha512, sha256: task.sha256, size: task.size, reuseDirs: task.reuseDirs, reuseFiles: task.reuseFiles, sourcePool, maxSegments: () => Math.max(1, Math.floor(downloadLimiter.maxConcurrent / Math.max(1, active.size))) })
          tracker.recordComplete(index, (await fs.promises.stat(task.dest)).size); active.delete(index)
        } catch (error) { firstError ??= error; controller.abort(error); return }
      }
    }))
    signal?.throwIfAborted()
    if (firstError) throw firstError
    report()
  } finally { clearInterval(timer); signal?.removeEventListener('abort', abort) }
}
