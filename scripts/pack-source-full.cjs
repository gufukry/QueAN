// 打出「完整源码包」：整个工作区（排除依赖/版本库/构建产物）压成一个 zip，
// 文件名带 时间戳 + 提交短号，并在包内附 BUILD-INFO.txt，便于区分新旧包。
//
// 与 pack-source.cjs（合规用的可审查源码包，只含 Git 跟踪的构建输入 + 逐文件哈希）不同，
// 这个包是给开发者直接解压使用的全量工作区。
//
// 用法：node scripts/pack-source-full.cjs
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const { execFileSync } = require('node:child_process')
const yazl = require('yazl')

const ROOT = path.resolve(__dirname, '..')
const EXCLUDE_DIRS = new Set(['node_modules', '.git', '.aicode', 'release', 'out'])
const SKIP_REL = new Set(['bridge/build', 'bridge/.cache', 'shell/bin', 'shell/obj'])

function git(args) {
  try { return execFileSync('git', args, { cwd: ROOT, encoding: 'utf8' }).trim() } catch { return '' }
}

function stamp() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}-${pad(now.getHours())}${pad(now.getMinutes())}`
}

const { version } = JSON.parse(fs.readFileSync(path.join(ROOT, 'package.json'), 'utf8'))
const commit = git(['rev-parse', '--short', 'HEAD']) || 'nogit'
const subject = git(['log', '-1', '--pretty=%s'])
const dirty = git(['status', '--porcelain']) ? '（工作区有未提交改动）' : ''
const buildInfo = [
  `QueAN ${version} 完整源码包`,
  `打包时间：${new Date().toISOString()}`,
  `提交：${commit}${dirty}`,
  `说明：${subject}`,
  '',
  '解压后：npm ci && npm run build（开发：npm run dev）',
  'Windows 打包：npm run dist:win；若构建目录被浏览器/杀软监视导致 EPERM，用 npm run dist:win:local'
].join('\n')

const name = `QueAN-${version}-source-full-${stamp()}-${commit}.zip`
const output = path.join(ROOT, 'release', name)
fs.mkdirSync(path.dirname(output), { recursive: true })

const zip = new yazl.ZipFile()
let count = 0
function walk(dir, rel) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const abs = path.join(dir, entry.name)
    const relative = rel ? rel + '/' + entry.name : entry.name
    if (entry.isDirectory()) {
      if (EXCLUDE_DIRS.has(entry.name) || SKIP_REL.has(relative)) continue
      walk(abs, relative)
    } else if (entry.isFile()) {
      if (SKIP_REL.has(relative)) continue
      zip.addFile(abs, relative)
      count++
    }
  }
}
walk(ROOT, '')
zip.addBuffer(Buffer.from(buildInfo, 'utf8'), 'BUILD-INFO.txt')
zip.end()

const stream = fs.createWriteStream(output)
zip.outputStream.pipe(stream)
stream.on('close', () => {
  const bytes = fs.readFileSync(output)
  console.log(JSON.stringify({
    output, name, files: count + 1, bytes: bytes.length,
    sha256: crypto.createHash('sha256').update(bytes).digest('hex'), commit
  }, null, 2))
})
