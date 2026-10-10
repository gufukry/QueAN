import assert from 'node:assert/strict'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import test from 'node:test'
import { applyPortableUserData } from '../src/main/core/portableData'

test('便携首启：默认数据目录整体搬到桌面 QueAN，内容完整保留', () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'quean-portable-'))
  try {
    const desktop = path.join(root, 'desktop'), defaultUserData = path.join(root, 'appdata', 'QueAN')
    fs.mkdirSync(path.join(defaultUserData, 'game', 'abc'), { recursive: true })
    fs.writeFileSync(path.join(defaultUserData, 'settings.json'), '{"ok":true}')
    fs.writeFileSync(path.join(defaultUserData, 'game', 'abc', 'base.jar'), 'base')
    const result = applyPortableUserData(desktop, defaultUserData)
    assert.equal(result, path.join(desktop, 'QueAN'))
    assert.equal(fs.readFileSync(path.join(result, 'settings.json'), 'utf8'), '{"ok":true}')
    assert.equal(fs.readFileSync(path.join(result, 'game', 'abc', 'base.jar'), 'utf8'), 'base')
    assert.equal(fs.existsSync(defaultUserData), false, '同卷搬迁后旧目录不再保留')
  } finally { fs.rmSync(root, { recursive: true, force: true }) }
})

test('已有桌面 QueAN 时不覆盖旧数据；全新安装直接创建便携目录', () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'quean-portable-existing-'))
  try {
    const desktop = path.join(root, 'desktop'), defaultUserData = path.join(root, 'appdata', 'QueAN')
    const portable = path.join(desktop, 'QueAN')
    fs.mkdirSync(portable, { recursive: true }); fs.writeFileSync(path.join(portable, 'settings.json'), 'existing')
    fs.mkdirSync(defaultUserData, { recursive: true }); fs.writeFileSync(path.join(defaultUserData, 'settings.json'), 'old')
    assert.equal(applyPortableUserData(desktop, defaultUserData), portable)
    assert.equal(fs.readFileSync(path.join(portable, 'settings.json'), 'utf8'), 'existing', '不覆盖已有桌面数据')
    assert.equal(fs.existsSync(defaultUserData), true, '旧目录保持原样')
  } finally { fs.rmSync(root, { recursive: true, force: true }) }
})

test('全新安装：默认目录不存在时直接建出桌面 QueAN', () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'quean-portable-fresh-'))
  try {
    const desktop = path.join(root, 'desktop')
    const result = applyPortableUserData(desktop, path.join(root, 'missing', 'QueAN'))
    assert.equal(result, path.join(desktop, 'QueAN'))
    assert(fs.statSync(result).isDirectory())
  } finally { fs.rmSync(root, { recursive: true, force: true }) }
})
