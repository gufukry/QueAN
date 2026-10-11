#!/usr/bin/env node
/**
 * 1) 从 src/shared/updateNotes.ts 生成 site/versions.json
 * 2) 把更新日志直接烤进 site/index.html 的 <!--LOG:START-->...<!--LOG:END--> 之间
 *    —— 页面不依赖 JS 也能显示日志
 */
const fs = require('node:fs')
const path = require('node:path')

const root = path.join(__dirname, '..')
const source = fs.readFileSync(path.join(root, 'src/shared/updateNotes.ts'), 'utf-8')
const match = source.match(/export const updateNotes[^=]*=\s*(\[[\s\S]*?\n\])/)
if (!match) throw new Error('未能从 updateNotes.ts 解析出 updateNotes 数组')
const notes = JSON.parse(match[1])
if (!Array.isArray(notes) || !notes.length) throw new Error('updateNotes 为空')

const versionsFile = path.join(root, 'site/versions.json')
fs.writeFileSync(versionsFile, JSON.stringify({ generatedAt: new Date().toISOString(), versions: notes }, null, 2) + '\n', 'utf-8')

const esc = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
/** 单条版本卡片；isLatest 时挂「最新」标签 */
const renderRelease = (rel, isLatest) => {
  const items = (rel.changes || []).map((line) => `            <li>${esc(line)}</li>`).join('\n')
  const tag = isLatest ? '<span class="tag">最新</span>' : ''
  return `          <article class="release">\n            <div class="rel-head"><b>QueAN v${esc(rel.version)}</b>${tag}<time>${esc(rel.date || '')}</time></div>\n            <ul>\n${items}\n            </ul>\n          </article>`
}
// 默认只展开最新版本，其余收进折叠的「历史版本」
const latest = renderRelease(notes[0], true)
const older = notes.slice(1)
const history = older.length
  ? `          <details class="history">\n            <summary>历史版本（${older.length}）</summary>\n${older.map((r) => renderRelease(r, false)).join('\n')}\n          </details>`
  : ''
const rows = [latest, history].filter(Boolean).join('\n')

const htmlFile = path.join(root, 'site/index.html')
let html = fs.readFileSync(htmlFile, 'utf-8')
const re = /(<!--LOG:START-->)[\s\S]*?(<!--LOG:END-->)/
if (!re.test(html)) throw new Error('index.html 缺少 <!--LOG:START--> / <!--LOG:END--> 标记')
html = html.replace(re, `$1\n${rows}\n        $2`)
fs.writeFileSync(htmlFile, html, 'utf-8')

console.log(`已生成 ${path.relative(root, versionsFile)} 并把日志写入 index.html（${notes.length} 个版本，最新 v${notes[0].version}）`)
