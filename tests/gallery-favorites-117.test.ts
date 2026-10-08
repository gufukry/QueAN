import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import sharp from 'sharp'
import { build } from 'esbuild'
import { parse, compileScript, compileTemplate } from '@vue/compiler-sfc'
import { BUILTIN_LAUNCH_IMAGES } from '../src/shared/launchImages'
import { activeCarouselKeys, carouselImages, carouselKeys, carouselSelection, carouselTiming, MAX_CAROUSEL_IMAGES } from '../src/shared/appearancePolicy'

test('gallery controls and external favorite buttons compile across their actual Vue templates', () => {
  for (const file of ['views/HomeView.vue', 'components/HomeLayoutEditor.vue', 'views/CommunityView.vue', 'components/FavoriteModsPicker.vue', 'components/FileManager.vue']) {
    const { descriptor, errors } = parse(fs.readFileSync('src/renderer/src/' + file, 'utf8'))
    assert.deepEqual(errors, [])
    const script = compileScript(descriptor, { id: file })
    assert.deepEqual(compileTemplate({ source: descriptor.template!.content, filename: file, id: file, compilerOptions: { bindingMetadata: script.bindings } }).errors, [])
  }
})

let favoriteBundle: Promise<string> | undefined
async function favoriteRuntime(invoke: (channel: string, ...args: any[]) => Promise<any>) {
  favoriteBundle ??= build({ entryPoints: ['src/renderer/src/modFavorites.ts'], bundle: true, write: false, platform: 'node', format: 'cjs', packages: 'external', plugins: [{ name: 'isolated-renderer-services', setup(plugin) {
    plugin.onResolve({ filter: /^\.\/(api|store)$/ }, args => args.importer.endsWith('modFavorites.ts') ? { path: args.path, namespace: 'service' } : undefined)
    plugin.onLoad({ filter: /.*/, namespace: 'service' }, args => ({ contents: args.path === './api' ? 'export function errText(e){return e.message}' : 'export function toast(message){window.failures.push(message)}', loader: 'js' }))
  } }] }).then(result => result.outputFiles[0].text)
  const runtime = { exports: {} as any }, window = { quean: { invoke }, failures: [] as string[] }
  new Function('require', 'module', 'exports', 'window', await favoriteBundle)(createRequire(path.resolve('package.json')), runtime, runtime.exports, window)
  return { ...runtime.exports, failures: window.failures }
}
const tick = () => new Promise(resolve => setImmediate(resolve))
function deferred<T>() { let resolve!: (value: T) => void; const promise = new Promise<T>(r => { resolve = r }); return { resolve, promise } }
test('shared favorite queue preserves distinct projects and blocks repeated clicks while a write is pending', async () => {
  let records: any[] = [], calls = 0
  const blocked = deferred<void>()
  const runtime = await favoriteRuntime(async (channel, value, enabled) => {
    if (channel === 'mods:favorites') return records
    calls++; if (calls === 1) await blocked.promise
    records = records.filter(record => record.key !== value.source + ':' + value.projectId)
    if (enabled) records.push({ ...value, key: value.source + ':' + value.projectId })
    return [...records]
  })
  const first = runtime.toggleProject('modrinth', 'a', 'A')
  const repeated = runtime.toggleProject('modrinth', 'a', 'A')
  const second = runtime.toggleProject('curseforge', 'b', 'B')
  await tick(); assert.equal(calls, 1); assert.equal(await repeated, false)
  assert(runtime.favoriteBusy.value.has('modrinth:a')); blocked.resolve()
  assert.equal(await first, true); assert.equal(await second, true)
  assert.deepEqual(runtime.favorites.value.map((record: any) => record.key), ['modrinth:a', 'curseforge:b'])
  assert.equal(runtime.favoriteBusy.value.size, 0)
})
test('newest favorite reads win and initial reads precede startup toggles of existing favorites', async () => {
  const older = deferred<any[]>(), newer = deferred<any[]>(); let reads = 0
  const runtime = await favoriteRuntime(async () => ++reads === 1 ? older.promise : newer.promise)
  const first = runtime.loadFavorites(), second = runtime.loadFavorites()
  await tick(); newer.resolve([{ key: 'modrinth:latest' }]); await second
  older.resolve([]); await first
  assert.equal(runtime.favorites.value[0].key, 'modrinth:latest')
  const initial = deferred<any[]>(); let enabled: unknown
  const startup = await favoriteRuntime(async (channel, _value, on) => { if (channel === 'mods:favorites') return initial.promise; enabled = on; return [] })
  const load = startup.loadFavorites(), toggle = startup.toggleProject('modrinth', 'existing', 'Existing')
  initial.resolve([{ key: 'modrinth:existing' }]); await Promise.all([load, toggle])
  assert.equal(enabled, false)
})
test('favorite write failure retains the confirmed snapshot and allows retry', async () => {
  let fail = true
  const runtime = await favoriteRuntime(async () => { if (fail) throw new Error('写盘失败'); return [{ key: 'modrinth:a' }] })
  assert.equal(await runtime.toggleProject('modrinth', 'a', 'A'), false)
  assert.deepEqual(runtime.favorites.value, []); assert.equal(runtime.favoriteBusy.value.size, 0)
  assert.deepEqual(runtime.failures, ['写盘失败'])
  fail = false; assert.equal(await runtime.toggleProject('modrinth', 'a', 'A'), true)
  assert.equal(runtime.favorites.value[0].key, 'modrinth:a')
})
test('reading during a queued startup write does not reverse the requested toggle', async () => {
  const read = deferred<any[]>(); let on: unknown, records = [{ key: 'modrinth:existing' }], calls = 0
  const runtime = await favoriteRuntime(async (channel, _value, enabled) => {
    if (channel === 'mods:favorites') return ++calls === 1 ? read.promise : records
    on = enabled; records = []; return records
  })
  const initial = runtime.loadFavorites(), toggle = runtime.toggleProject('modrinth', 'existing', 'Existing'), later = runtime.loadFavorites()
  read.resolve(records); await Promise.all([initial, toggle, later])
  assert.equal(on, false); assert.deepEqual(runtime.favorites.value, [])
})
