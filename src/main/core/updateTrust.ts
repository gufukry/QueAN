import { app } from 'electron'
import { GITHUB_REPO, UPDATE_ENDPOINT } from '../../shared/branding'
import type { ReleaseInfo } from '../../shared/types'
import { updateAssetNames, updateArtifactName, type InstallationKind } from '../../shared/platform'
import { installationKind } from '../platform'

/** 测试覆盖只能用于开发进程，且必须同时指定独立数据目录和模拟 API。 */
export function isolatedUpdateTest(): boolean {
  return app?.isPackaged !== true && !!process.env.QueAN_USERDATA_DIR && !!process.env.QueAN_UPDATE_API_BASE
}

export function trustedUpdateRelease(release: ReleaseInfo | undefined): boolean {
  if (!release || !/^\d+\.\d+\.\d+$/.test(release.version)) return false
  if (isolatedUpdateTest()) return true
  const names = updateAssetNames(release.version, process.platform, process.arch, installationKind())
  const fromTrustedSource = names.some(name => release.assetUrl === `https://github.com/${GITHUB_REPO}/releases/download/v${release.version}/${name}`)
  return names.includes(release.assetName) && release.assetSize > 0 &&
    (fromTrustedSource || selfHostedAssetUrls(names).includes(release.assetUrl))
}

/** 自建更新源的资源直链（由 UPDATE_ENDPOINT 推导目录前缀后，与 GitHub 同样按资产名精确匹配）。 */
function selfHostedAssetUrls(names: string[]): string[] {
  if (!UPDATE_ENDPOINT) return []
  const base = UPDATE_ENDPOINT.replace(/[^/]+$/, '')
  return names.map(name => `${base}${name}`)
}

export function updateAssetName(version: string, platform: string = process.platform, arch: string = process.arch, installation: InstallationKind = installationKind()): string {
  return updateArtifactName(version, platform, arch, installation)
}

/** 当前平台可接受的更新资产名列表（Windows 兼容带/不带版本号）。 */
export function updateAssetNameList(version: string): string[] {
  return updateAssetNames(version, process.platform, process.arch, installationKind())
}
