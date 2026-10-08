// Fail before compression if platform file globs accidentally include the workspace.
const fs = require('node:fs'), path = require('node:path'), assert = require('node:assert/strict'), crypto = require('node:crypto')
const asar = require('asar')
function verifyWindowsRuntime(appOutDir) {
  const archive = path.join(appOutDir, 'resources/app.asar')
  const names = asar.listPackage(archive).map(n => n.replaceAll('\\', '/').replace(/^\//, ''))
  // 内置启动插画已移除；仅保留代码/依赖体积预算。
  assert(fs.statSync(archive).size < 16 * 1024 * 1024, 'Application code/dependencies exceed 16 MiB: review payload growth before release')
  for (const name of names) {
    const entry = asar.statFile(archive, name.split('/').join(path.sep))
    if (entry.files) continue
    assert(/^(node_modules\/|out\/(main|preload|renderer)\/|licenses\/|LICENSE$|THIRD_PARTY_NOTICES\.md$|package\.json$|docs\/CORRESPONDING_SOURCE\.md$)/.test(name), 'Unexpected packaged file: ' + name)
    assert(!/^node_modules\/koffi\/(doc|lib|vendor)(\/|$)/.test(name), 'Development-only Koffi files included')
    assert(!/^node_modules\/undici\/docs(\/|$)/.test(name), 'Development-only Undici docs included')
  }
  console.log(`Verified Windows ASAR: ${fs.statSync(archive).size} bytes, ${names.length} entries`)
}
module.exports = context => {
  if (context.electronPlatformName === 'win32') {
    verifyWindowsRuntime(context.appOutDir)
    require('./prune-windows-runtime.cjs').pruneWindowsRuntime(context)
  }
}
module.exports.verifyWindowsRuntime = verifyWindowsRuntime
