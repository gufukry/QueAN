#!/usr/bin/env node
/**
 * 从 src/shared/updateNotes.ts 生成 site/versions.json（官网更新日志数据源）。
 * 发版时与版本号一起跑，避免日志三处手抄。
 */
const fs = require('node:fs')
const path = require('node:path')

const root = path.join(__dirname, '..')
const source = fs.readFileSync(path.join(root, 'src/shared/updateNotes.ts'), 'utf-8')
const match = source.match(/export const updateNotes[^=]*=\s*(\[[\s\S]*?\n\])/)
if (!match) throw new Error('未能从 updateNotes.ts 解析出 updateNotes 数组')
const notes = JSON.parse(match[1])
if (!Array.isArray(notes) || !notes.length) throw new Error('updateNotes 为空')

const out = path.join(root, 'site/versions.json')
fs.writeFileSync(out, JSON.stringify({ generatedAt: new Date().toISOString(), versions: notes }, null, 2) + '\n', 'utf-8')
console.log(`已生成 ${path.relative(root, out)}（${notes.length} 个版本，最新 v${notes[0].version}）`)
