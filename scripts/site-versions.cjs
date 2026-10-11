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
const rows = notes.map((rel, index) => {
  const items = (rel.changes || []).map((line) => `          <li>${esc(line)}</li>`).join('\n')
  const tag = index === 0 ? '<span class="tag">最新</span>' : ''
  return `        <article class="release">\n          <div class="rel-head"><b>QueAN v${esc(rel.version)}</b>${tag}<time>${esc(rel.date || '')}</time></div>\n          <ul>\n${items}\n          </ul>\n        </article>`
}).join('\n')

const htmlFile = path.join(root, 'site/index.html')
let html = fs.readFileSync(htmlFile, 'utf-8')
const re = /(<!--LOG:START-->)[\s\S]*?(<!--LOG:END-->)/
if (!re.test(html)) throw new Error('index.html 缺少 <!--LOG:START--> / <!--LOG:END--> 标记')
html = html.replace(re, `$1\n${rows}\n        $2`)
fs.writeFileSync(htmlFile, html, 'utf-8')

console.log(`已生成 ${path.relative(root, versionsFile)} 并把日志写入 index.html（${notes.length} 个版本，最新 v${notes[0].version}）`)
