// 清空（删除）指定目录，供本地构建前清理残留产物使用。
// 用法：node scripts/clean-dir.cjs C:/QueanBuild
const fs = require('node:fs')
const target = process.argv[2]
if (!target) { console.error('用法：node scripts/clean-dir.cjs <目录>'); process.exitCode = 1 }
else {
  try { fs.rmSync(target, { recursive: true, force: true }); console.log('已清空 ' + target) }
  catch (error) { console.error('清理失败（可能有进程占用）：' + error.message); process.exitCode = 1 }
}
