import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { IPC_EVENT } from '../src/shared/types'

const read = (file: string) => fs.readFileSync(path.join(process.cwd(), file), 'utf8')

/**
 * Electron 的单实例锁只能在真实主进程里生效，这里退一步做「源码契约」断言：
 * 防止以后有人误删这把锁，或删掉二次启动的聚焦/提示。
 */
test('单实例：主进程取锁、二次启动聚焦已有窗口并提示用户', () => {
  const main = read('src/main/index.ts')
  assert.match(main, /const singleInstanceLock = app\.requestSingleInstanceLock\(\)/, '必须获取单实例锁')
  assert.match(main, /if \(!singleInstanceLock\) \{\s*\n\s*launcherLogInfo[\s\S]*?app\.quit\(\)/, '拿不到锁要退出')
  assert.match(main, /app\.on\('second-instance'/, '要处理二次启动事件')
  assert.match(main, /if \(!singleInstanceLock\) return/, '锁失败时 whenReady 不得创建窗口')
  assert.match(main, /webContents\.send\(IPC_EVENT\.alreadyRunning\)/, '二次启动要给渲染层提示')
})

test('单实例：提示通道已贯通（事件常量 + api + 界面提示）', () => {
  assert.equal(IPC_EVENT.alreadyRunning, 'event:alreadyRunning')
  assert.match(read('src/renderer/src/api.ts'), /export const onAlreadyRunning/, 'api 要暴露订阅')
  const app = read('src/renderer/src/App.vue')
  assert.match(app, /onAlreadyRunning\(\(\) =>/, '界面要订阅')
  assert.match(app, /启动器已在运行/, '界面要有可见提示')
})
