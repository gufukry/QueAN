import { abortableDelay } from './tasks'

export interface DownloadLimits { downloadThreads: number; downloadSpeedKBps: number }
export const DEFAULT_DOWNLOAD_LIMITS: DownloadLimits = { downloadThreads: 64, downloadSpeedKBps: 0 }
export function validateDownloadLimits(value: DownloadLimits): void {
  if (!Number.isInteger(value.downloadThreads) || value.downloadThreads < 1 || value.downloadThreads > 64)
    throw new Error('最大线程数必须是 1–64 的整数')
  if (!Number.isInteger(value.downloadSpeedKBps) || value.downloadSpeedKBps < 0 || value.downloadSpeedKBps > 1048576)
    throw new Error('速度限制必须是 0–1048576 KiB/s 的整数，0 表示不限速')
}

/**
 * 同一域名最多同时占用的连接数；多个镜像并存时避免其中一个吃满全局名额。
 * 参照 Axolotl/Theseus 的 per-authority 上限（32）；单源下载不传 origin，不受此限。
 */
export const HOST_CONNECTION_LIMIT = 32

/** 所有下载共用一个并发闸门与字节预算，跨安装任务也不会叠加突破限制。 */
export class DownloadLimiter {
  private limits = { ...DEFAULT_DOWNLOAD_LIMITS }
  private active = 0
  private queue: Array<() => void> = []
  private hostActive = new Map<string, number>()
  private tokens = 0
  private sampledAt = performance.now()
  /** 当前是否启用了限速（慢速连接检测在限速时跳过：慢是限速的预期行为） */
  get isThrottling(): boolean {
    return this.limits.downloadSpeedKBps > 0
  }
  get maxConcurrent(): number { return this.limits.downloadThreads }
  configure(limits: DownloadLimits): void {
    validateDownloadLimits(limits)
    this.limits = { ...limits }
    this.tokens = 0
    this.sampledAt = performance.now()
    this.wake()
  }
  private wake(): void {
    // 全局名额与单域名名额都满足才能放行；否则留在队列里等下一次唤醒。
    const remaining: Array<() => void> = []
    for (const start of this.queue) {
      if (this.active >= this.limits.downloadThreads) { remaining.push(start); continue }
      const origin = this.queuedOrigins.get(start)
      if (origin && (this.hostActive.get(origin) ?? 0) >= HOST_CONNECTION_LIMIT) { remaining.push(start); continue }
      start()
    }
    this.queue = remaining
  }
  private queuedOrigins = new Map<() => void, string>()

  async acquire(signal?: AbortSignal, origin?: string): Promise<() => void> {
    signal?.throwIfAborted()
    return new Promise((resolve, reject) => {
      const cancel = () => {
        this.queue = this.queue.filter(item => item !== start)
        this.queuedOrigins.delete(start)
        reject(signal?.reason ?? new Error('已取消'))
      }
      const start = () => {
        signal?.removeEventListener('abort', cancel)
        this.queuedOrigins.delete(start)
        this.active++
        if (origin) this.hostActive.set(origin, (this.hostActive.get(origin) ?? 0) + 1)
        let released = false
        resolve(() => {
          if (released) return
          released = true
          this.active--
          if (origin) {
            const count = (this.hostActive.get(origin) ?? 1) - 1
            if (count > 0) this.hostActive.set(origin, count)
            else this.hostActive.delete(origin)
          }
          this.wake()
        })
      }
      signal?.addEventListener('abort', cancel, { once: true })
      if (origin) this.queuedOrigins.set(start, origin)
      this.queue.push(start)
      this.wake()
    })
  }
  async consume(bytes: number, signal?: AbortSignal): Promise<void> {
    let remaining = bytes
    while (remaining > 0) {
      signal?.throwIfAborted()
      const rate = this.limits.downloadSpeedKBps * 1024
      if (!rate) return
      const now = performance.now()
      this.tokens = Math.min(rate * .15, this.tokens + (now - this.sampledAt) * rate / 1000)
      this.sampledAt = now
      const take = Math.min(remaining, Math.floor(this.tokens))
      this.tokens -= take
      remaining -= take
      if (remaining > 0) await abortableDelay(Math.min(100, Math.max(5, remaining / rate * 1000)), signal)
    }
  }
}
export const downloadLimiter = new DownloadLimiter()
