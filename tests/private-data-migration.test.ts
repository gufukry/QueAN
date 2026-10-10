import assert from 'node:assert/strict'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import test from 'node:test'
import { createRequire } from 'node:module'
import { build } from 'esbuild'
import type { GameFolder } from '../src/shared/types'

/** 真实迁移逻辑 + 仅注入 Electron 的 app.getPath（userData/appData 都落在临时目录下） */
async function migrationHarness(root: string) {
  const built = await build({
    stdin: {
      contents: `export { migrateLegacyFolders, movePrivateData } from './src/main/core/privateDataMigration';
        export { privateGameRoot, privateBaseDir, privateIconsDir, privateThumbnailsDir, privateModpackCacheDir } from './src/main/core/privatePaths';`,
      resolveDir: process.cwd(), loader: 'ts'
    },
    bundle: true, write: false, format: 'cjs', platform: 'node', packages: 'external', logLevel: 'silent'
  })
  const require = createRequire(path.resolve('package.json'))
  const exported = { exports: {} as any }
  const electron = { app: { getPath: (name: string) => path.join(root, name), getName: () => 'QueAN-test' } }
  new Function('require', 'module', 'exports', built.outputFiles[0].text)(
    (name: string) => name === 'electron' ? electron : require(name),
    exported, exported.exports
  )
  return exported.exports as {
    migrateLegacyFolders: (folders: GameFolder[], active: string, appData: string) => { folders: GameFolder[]; activeFolder: string; changed: boolean }
    movePrivateData: (folder: string) => boolean
    privateGameRoot: (folder: string) => string
    privateBaseDir: (folder: string) => string
    privateIconsDir: (folder: string) => string
    privateThumbnailsDir: (folder: string) => string
    privateModpackCacheDir: (folder: string) => string
  }
}

function seedLegacyFolder(appData: string): string {
  const legacy = path.join(appData, '.quean')
  fs.mkdirSync(path.join(legacy, 'versions', '1.21.1'), { recursive: true })
  fs.writeFileSync(path.join(legacy, 'versions', '1.21.1', '1.21.1.json'), '{"id":"1.21.1"}')
  fs.mkdirSync(path.join(legacy, 'mods'), { recursive: true })
  fs.writeFileSync(path.join(legacy, 'mods', 'a.jar'), 'mod')
  fs.mkdirSync(path.join(legacy, '.quean', 'base', '1.21.1'), { recursive: true })
  fs.writeFileSync(path.join(legacy, '.quean', 'base', '1.21.1', '1.21.1.jar'), 'base')
  fs.mkdirSync(path.join(legacy, '.quean', 'icons'), { recursive: true })
  fs.writeFileSync(path.join(legacy, '.quean', 'icons', 'custom.png'), 'icon')
  fs.writeFileSync(path.join(legacy, '.quean-bridge.json'), '{"token":"t"}')
  return legacy
}

test('迁移：老默认目录改名为 .minecraft，私有数据移出游戏目录，游戏文件保持完好', async () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'quean-migrate-'))
  try {
    const appData = path.join(root, 'appData')
    fs.mkdirSync(appData, { recursive: true })
    const legacy = seedLegacyFolder(appData)
    const api = await migrationHarness(root)
    const target = path.join(appData, '.minecraft')

    const result = api.migrateLegacyFolders([{ path: legacy, name: '默认文件夹', isDefault: true }], legacy, legacy, target)

    assert.equal(result.changed, true)
    assert.equal(result.activeFolder, target)
    assert.equal(result.folders[0].path, target)
    assert.equal(fs.existsSync(legacy), false, '老目录应已改名')
    // 游戏数据随目录改名保留
    assert.equal(fs.readFileSync(path.join(target, 'versions', '1.21.1', '1.21.1.json'), 'utf8'), '{"id":"1.21.1"}')
    assert.equal(fs.readFileSync(path.join(target, 'mods', 'a.jar'), 'utf8'), 'mod')
    // 私有数据搬进私有根（<userData>/game/<key>/…）
    assert.equal(api.privateBaseDir(target).startsWith(path.join(root, 'userData', 'game')), true)
    assert.equal(fs.readFileSync(path.join(api.privateBaseDir(target), '1.21.1', '1.21.1.jar'), 'utf8'), 'base')
    assert.equal(fs.readFileSync(path.join(api.privateIconsDir(target), 'custom.png'), 'utf8'), 'icon')
    assert.equal(fs.existsSync(path.join(target, '.quean')), false, '游戏目录里不再有 .quean')
    // 桥接发现文件必须留在游戏目录（MOD 要读）
    assert.equal(fs.readFileSync(path.join(target, '.quean-bridge.json'), 'utf8'), '{"token":"t"}')
  } finally { fs.rmSync(root, { recursive: true, force: true }) }
})

test('迁移：目标 .minecraft 已存在时不覆盖，保留旧目录并切换默认', async () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'quean-migrate-existing-'))
  try {
    const appData = path.join(root, 'appData')
    fs.mkdirSync(appData, { recursive: true })
    const legacy = seedLegacyFolder(appData)
    const target = path.join(appData, '.minecraft')
    fs.mkdirSync(path.join(target, 'versions', 'pcl-only'), { recursive: true })
    fs.writeFileSync(path.join(target, 'versions', 'pcl-only', 'pcl-only.json'), '{"id":"pcl-only"}')
    const api = await migrationHarness(root)

    const result = api.migrateLegacyFolders([{ path: legacy, name: '默认文件夹', isDefault: true }], legacy, legacy, target)

    assert.equal(result.changed, true)
    assert.equal(result.activeFolder, target)
    assert.equal(fs.existsSync(legacy), true, '不覆盖已有 .minecraft，旧目录保留')
    assert.equal(fs.existsSync(path.join(legacy, 'versions', '1.21.1')), true, '旧目录里的版本仍在')
    assert.equal(fs.existsSync(path.join(api.privateIconsDir(legacy), 'custom.png')), true, '旧目录的私有数据仍被搬走')
    assert.equal(fs.readFileSync(path.join(target, 'versions', 'pcl-only', 'pcl-only.json'), 'utf8'), '{"id":"pcl-only"}')
    const defaults = result.folders.filter(folder => folder.isDefault)
    assert.equal(defaults.length, 1)
    assert.equal(path.resolve(defaults[0].path), path.resolve(target))
    assert.equal(result.folders.some(folder => path.resolve(folder.path) === path.resolve(legacy)), true, '旧目录仍登记在列表中')
  } finally { fs.rmSync(root, { recursive: true, force: true }) }
})

test('迁移幂等：第二次调用无改动；同名私有文件以目标为准不被覆盖', async () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'quean-migrate-idempotent-'))
  try {
    const appData = path.join(root, 'appData')
    fs.mkdirSync(appData, { recursive: true })
    const legacy = seedLegacyFolder(appData)
    const api = await migrationHarness(root)
    const target = path.join(appData, '.minecraft')

    const first = api.migrateLegacyFolders([{ path: legacy, name: '默认文件夹', isDefault: true }], legacy, legacy, target)
    assert.equal(first.changed, true)
    const second = api.migrateLegacyFolders(first.folders, first.activeFolder, legacy, target)
    assert.equal(second.changed, false, '第二次调用不应再有改动')

    // 私有根里已有同名文件：再搬一次时必须保留已有内容
    const icon = path.join(api.privateIconsDir(target), 'custom.png')
    fs.writeFileSync(icon, 'newer')
    fs.mkdirSync(path.join(target, '.quean', 'icons'), { recursive: true })
    fs.writeFileSync(path.join(target, '.quean', 'icons', 'custom.png'), 'legacy')
    assert.equal(api.movePrivateData(target), true)
    assert.equal(fs.readFileSync(icon, 'utf8'), 'newer')
    assert.equal(fs.existsSync(path.join(target, '.quean')), false)
  } finally { fs.rmSync(root, { recursive: true, force: true }) }
})
