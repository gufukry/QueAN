/**
 * 品牌与渠道常量（改这里，不要散落代码深处）。
 */

/** GitHub 仓库（自更新检查与 Release 下载来源） */
export const GITHUB_REPO = 'gufukry/QueAN'

/**
 * 启动器更新包/校验文件的 GitHub 加速镜像（按顺序尝试）。
 * 下载器会把不同分片分散到各镜像以叠加带宽，失败或超时的源自动沉底。
 */
export const UPDATE_MIRRORS = [
  'https://ghproxy.net/',
  'https://gh-proxy.com/',
  'https://ghfast.top/',
  'https://hk.gh-proxy.com/'
]

/**
 * 自建更新端点：返回与 GitHub Release 同构的 JSON，作为**第一更新来源**；不可用时自动回落 GitHub。
 * 期望格式：{ "tag_name": "v1.0.0", "body": "更新说明",
 *   "assets": [{ "name": "QueAN-1.0.0-portable.exe", "browser_download_url": "https://…",
 *                "digest": "sha256:<64位小写十六进制>", "size": 12345678 }] }
 * 服务器侧用 deploy/quean-update-setup.sh 生成（扫描目录里的 exe/zip）。
 */
export const UPDATE_ENDPOINT = 'https://xfmod.cc.cd/quean-update/latest.json'
