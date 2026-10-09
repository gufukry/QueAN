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
