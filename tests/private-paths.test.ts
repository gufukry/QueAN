import assert from 'node:assert/strict'
import path from 'node:path'
import test from 'node:test'
import { privateFolderKey } from '../src/main/core/privatePaths'

test('文件夹 key 稳定：忽略尾部分隔符与重复分隔符', () => {
  const base = path.resolve('/tmp/quean-key')
  assert.equal(privateFolderKey(base), privateFolderKey(base + path.sep))
  assert.equal(privateFolderKey(base), privateFolderKey(base + path.sep + path.sep))
})

test('文件夹 key 区分不同目录，且不因目录是否存在而变化', () => {
  const a = path.resolve('/tmp/quean-key-a')
  const b = path.resolve('/tmp/quean-key-b')
  assert.notEqual(privateFolderKey(a), privateFolderKey(b))
  assert.match(privateFolderKey(a), /^[a-f0-9]{16}$/)
  // 同一路径重复计算结果一致（目录尚未创建也不影响）
  assert.equal(privateFolderKey(a), privateFolderKey(a))
})

test('文件夹 key 使用规范化路径：相对路径与等价绝对路径一致', () => {
  const abs = path.resolve('/tmp/quean-key/../quean-key')
  assert.equal(privateFolderKey(abs), privateFolderKey('/tmp/quean-key'))
})
