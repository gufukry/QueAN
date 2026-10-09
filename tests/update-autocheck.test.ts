import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { shouldPrompt } from '../src/main/core/selfUpdate'
import type { ReleaseInfo } from '../src/shared/types'

const read = (file: string) => fs.readFileSync(path.join(process.cwd(), file), 'utf8')

const release = (version: string): ReleaseInfo => ({
  version, publishedAt: '', body: '', assetUrl: `https://github.com/gufukry/QueAN/releases/download/v${version}/QueAN.exe`,
  assetName: 'QueAN.exe', assetSize: 1
})

test('启动自动检查：默认开启、可在设置里关掉、启动时强制刷新', () => {
  assert.match(read('src/main/core/settings.ts'), /autoCheckUpdate: true/, '默认开启')
  const main = read('src/main/index.ts')
  assert.match(main, /getSettings\(\)\.autoCheckUpdate === false/, '关掉后不再自动检查')
  assert.match(main, /void runUpdateCheck\(true\)/, '启动即检查（强制刷新，不吃 6 小时缓存）')
  const ui = read('src/renderer/src/views/SettingsView.vue')
  assert.match(ui, /启动时自动检查更新/, '设置里有开关')
  assert.match(ui, /save\(\{ autoCheckUpdate:/, '开关能保存')
})

test('更新弹窗提供「前往更新 / 不更新」两条路（立即更新 / 稍后 / 跳过此版本）', () => {
  const modal = read('src/renderer/src/components/UpdateModal.vue')
  assert.match(modal, /立即更新/, '前往更新')
  assert.match(modal, /稍后提醒/, '本次不更新')
  assert.match(modal, /跳过此版本/, '不再提示该版本')
})

test('提示判定：新版本提示、跳过的版本不再提示', () => {
  assert.equal(shouldPrompt(release('1.1.0'), undefined, '1.0.0'), true)
  assert.equal(shouldPrompt(release('1.0.0'), undefined, '1.0.0'), false, '同版本不提示')
  assert.equal(shouldPrompt(release('1.1.0'), '1.1.0', '1.0.0'), false, '已跳过该版本')
  assert.equal(shouldPrompt(release('1.1.0'), '1.0.5', '1.0.0'), true, '出现更新的版本继续提示')
  assert.equal(shouldPrompt(undefined, undefined, '1.0.0'), false)
})
