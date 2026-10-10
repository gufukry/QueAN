/**
 * 一次性迁移：把 QueAN 私有数据移出游戏目录，并把老默认目录 .quean 换成 PCL 风格的 .minecraft。
 *
 * 设计要点：
 * - 先搬私有项（base/icons/thumbnails/modpack-cache）再改名目录：任意一步中断，旧目录仍可正常启动；
 * - 同卷 rename 为 O(1)，跨卷自动退化为复制 + 删除；
 * - 目标已存在时按文件合并、从不覆盖，最后才删除已搬空的源目录；
 * - 结果幂等：搬完后游戏目录里不再有 .quean，条件自然不再成立。
 */
import fs from 'node:fs'
import path from 'node:path'
import type { GameFolder } from '../../shared/types'
import { logScope } from './launcherLog'
import {
  privateBaseDir,
  privateIconsDir,
  privateModpackCacheDir,
  privateThumbnailsDir
} from './privatePaths'

const migrateLog = logScope('migrate')

const PRIVATE_ITEMS: Array<[string, (folder: string) => string]> = [
  ['base', privateBaseDir],
  ['icons', privateIconsDir],
  ['thumbnails', privateThumbnailsDir],
  ['modpack-cache', privateModpackCacheDir]
]

function moveFile(from: string, to: string): void {
  try {
    fs.renameSync(from, to)
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code !== 'EXDEV') throw error
    fs.copyFileSync(from, to)
    fs.rmSync(from, { force: true })
  }
}

/** 合并式搬迁：同名目标保留原样，只搬源里多出来的内容；完成后删除已搬空的源 */
function mergeInto(from: string, to: string): void {
  const stat = fs.lstatSync(from)
  if (!stat.isDirectory()) {
    if (fs.existsSync(to)) return
    fs.mkdirSync(path.dirname(to), { recursive: true })
    moveFile(from, to)
    return
  }
  fs.mkdirSync(to, { recursive: true })
  for (const entry of fs.readdirSync(from)) mergeInto(path.join(from, entry), path.join(to, entry))
  fs.rmSync(from, { recursive: true, force: true })
}

/** 搬走单个游戏文件夹里的私有数据；返回是否发生了搬迁 */
export function movePrivateData(folder: string): boolean {
  const source = path.join(folder, '.quean')
  if (!fs.existsSync(source)) return false
  let moved = false
  for (const [name, target] of PRIVATE_ITEMS) {
    const from = path.join(source, name)
    if (!fs.existsSync(from)) continue
    mergeInto(from, target(folder))
    moved = true
  }
  // 私有项搬空后删掉空目录；里面若还有未知内容则保留，不碰用户数据
  try {
    if (!fs.readdirSync(source).length) fs.rmSync(source, { recursive: true, force: true })
  } catch { /* 目录被占用则留到下次启动再处理 */ }
  return moved
}

export interface LegacyFolderMigration {
  folders: GameFolder[]
  activeFolder: string
  changed: boolean
}

/**
 * 迁移所有已登记文件夹：先把老默认目录 <appData>/.quean 改名为 <appData>/.minecraft
 * （目标已存在时退化为「保留旧目录 + 另登记 .minecraft」），再按各文件夹的最终路径搬走私有数据。
 *
 * 顺序很关键：私有目录以文件夹路径为 key，必须先改名再搬，否则数据会落在旧 key 下变成孤儿；
 * 若在中途中断，下次启动仍会按当前路径继续搬（自愈），且旧路径能通过 legacy 回退读到。
 */
export function migrateLegacyFolders(
  folders: GameFolder[],
  activeFolder: string,
  appData: string
): LegacyFolderMigration {
  const legacyRoot = path.join(appData, '.quean')
  const target = path.join(appData, '.minecraft')
  const same = (a: string, b: string): boolean => path.resolve(a) === path.resolve(b)

  let next = folders
  let active = activeFolder
  let changed = false
  const legacyIndex = folders.findIndex((folder) => same(folder.path, legacyRoot))

  if (legacyIndex >= 0) {
    try {
      if (!fs.existsSync(target)) {
        fs.renameSync(legacyRoot, target)
        next = folders.map((folder) => (same(folder.path, legacyRoot) ? { ...folder, path: target } : folder))
        if (same(active, legacyRoot)) active = target
        changed = true
        migrateLog.info(`默认游戏目录已迁移：${legacyRoot} → ${target}`)
      } else {
        // 目标已存在（例如用户另有官方/PCL 的 .minecraft）：不覆盖，改为登记新默认并保留旧目录
        const registered = folders.some((folder) => same(folder.path, target))
        const demoted = folders.map((folder, index) =>
          index === legacyIndex ? { ...folder, isDefault: false } : folder
        )
        next = registered
          ? demoted.map((folder) => (same(folder.path, target) ? { ...folder, isDefault: true } : folder))
          : [...demoted, { path: target, name: '.minecraft', isDefault: true }]
        active = target
        changed = true
        migrateLog.info(`目标 .minecraft 已存在：保留旧目录 ${legacyRoot}，默认切换为 ${target}（磁盘文件均保留）`)
      }
    } catch (error) {
      // 改名失败（占用/权限）时不改设置，继续用旧目录，保证可启动
      migrateLog.warn(`默认游戏目录改名失败，继续使用旧目录：${legacyRoot}`, error instanceof Error ? error : undefined)
    }
  }

  for (const folder of next) {
    try {
      if (movePrivateData(folder.path)) {
        changed = true
        migrateLog.info(`私有数据已移出游戏目录：${folder.path}`)
      }
    } catch (error) {
      migrateLog.warn(`私有数据迁移失败，保留原样：${folder.path}`, error instanceof Error ? error : undefined)
    }
  }

  return { folders: next, activeFolder: active, changed }
}
