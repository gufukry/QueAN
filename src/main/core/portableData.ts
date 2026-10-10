/**
 * 便携式数据目录（与 PCL 一致）：启动器数据放桌面「QueAN」，游戏数据放桌面「.minecraft」。
 *
 * PCL 便携版把两者建在启动器所在目录，从桌面运行时就是桌面；首次升级时把老的
 * %APPDATA%\QueAN 整体搬过来，让设置、账号、日志、私有游戏数据继续可用。
 */
import fs from 'node:fs'
import path from 'node:path'

/** 逐项复制（不覆盖已存在的目标文件），用于跨卷搬迁 */
function copyTree(from: string, to: string): void {
  fs.mkdirSync(to, { recursive: true })
  for (const entry of fs.readdirSync(from, { withFileTypes: true })) {
    const source = path.join(from, entry.name), target = path.join(to, entry.name)
    if (entry.isDirectory()) copyTree(source, target)
    else if (!fs.existsSync(target)) fs.copyFileSync(source, target)
  }
}

/**
 * 返回最终应使用的 userData 目录。默认路径已存在时整体搬移到便携位置；
 * 跨卷（EXDEV）退化为复制并保留原目录，避免中途失败丢数据。失败时返回原路径。
 */
export function applyPortableUserData(desktop: string, defaultUserData: string): string {
  const portable = path.join(desktop, 'QueAN')
  if (path.resolve(portable) === path.resolve(defaultUserData)) return portable
  if (fs.existsSync(portable)) return portable
  if (!fs.existsSync(defaultUserData)) {
    fs.mkdirSync(portable, { recursive: true })
    return portable
  }
  try {
    fs.mkdirSync(path.dirname(portable), { recursive: true })
    fs.renameSync(defaultUserData, portable)
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code !== 'EXDEV') throw error
    copyTree(defaultUserData, portable)
  }
  return portable
}
