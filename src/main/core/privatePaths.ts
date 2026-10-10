/**
 * QueAN 私有数据目录。
 *
 * 游戏目录要保持 Minecraft / PCL 的标准结构（versions、libraries、assets…），
 * 以便被其它启动器共用；启动器自己的数据统一放在 <userData>/game/<文件夹 key> 下。
 */
import { app } from 'electron'
import crypto from 'node:crypto'
import path from 'node:path'

/** 文件夹路径 → 稳定 key：规范化后取 SHA1 前 16 位（不解析符号链接，保证目录创建前后一致） */
export function privateFolderKey(folder: string): string {
  const resolved = path.resolve(folder).replace(/[\\/]+$/, '')
  const identity = process.platform === 'win32' ? resolved.toLowerCase() : resolved
  return crypto.createHash('sha1').update(identity).digest('hex').slice(0, 16)
}

/** 某个游戏文件夹的私有数据根 */
export function privateGameRoot(folder: string): string {
  return path.join(app.getPath('userData'), 'game', privateFolderKey(folder))
}

/** 加载器实例依赖的原版 json/jar（不进版本列表） */
export function privateBaseDir(folder: string): string {
  return path.join(privateGameRoot(folder), 'base')
}

/** 自定义实例图标 */
export function privateIconsDir(folder: string): string {
  return path.join(privateGameRoot(folder), 'icons')
}

/** 实例缩略图 */
export function privateThumbnailsDir(folder: string): string {
  return path.join(privateGameRoot(folder), 'thumbnails')
}

/** 整合包文件缓存 */
export function privateModpackCacheDir(folder: string): string {
  return path.join(privateGameRoot(folder), 'modpack-cache')
}
