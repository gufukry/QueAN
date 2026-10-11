/* QueAN 官网脚本：下载信息实时读取更新清单；动效为渐进增强（JS 不可用时页面照常完整显示） */
(function () {
  const MANIFEST = './quean-update/latest.json'
  const fmtSize = (n) => (!n || !Number.isFinite(n) ? '—' : n >= 1048576 ? (n / 1048576).toFixed(1) + ' MB' : Math.round(n / 1024) + ' KB')
  const setText = (id, v) => { const el = document.getElementById(id); if (el && v != null) el.textContent = v }

  // 动效：只有支持时才启用（默认样式可见，避免 JS 失败导致内容空白）
  if (!matchMedia('(prefers-reduced-motion: reduce)').matches) {
    document.documentElement.classList.add('motion')
    const reveal = (el) => el.classList.add('in')
    if ('IntersectionObserver' in window) {
      const io = new IntersectionObserver((entries) => {
        entries.forEach((e) => { if (e.isIntersecting) { reveal(e.target); io.unobserve(e.target) } })
      }, { rootMargin: '0px 0px -6% 0px', threshold: 0.06 })
      document.querySelectorAll('.reveal').forEach((el) => io.observe(el))
    } else {
      document.querySelectorAll('.reveal').forEach(reveal)
    }
  }
  const onScroll = () => document.querySelector('header.top')?.classList.toggle('scrolled', window.scrollY > 8)
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })

  fetch(MANIFEST, { cache: 'no-store' })
    .then((r) => (r.ok ? r.json() : Promise.reject(new Error('HTTP ' + r.status))))
    .then((m) => {
      const version = String(m.tag_name || '').replace(/^v/, '')
      const asset = (m.assets || [])[0] || {}
      const digest = String(asset.digest || '').replace(/^sha256:/, '')
      if (version) {
        setText('verBadge', '最新版本 v' + version)
        setText('dlVersion', 'QueAN v' + version)
        setText('dlTitle', 'QueAN v' + version)
      }
      setText('dlSize', fmtSize(asset.size))
      setText('dlSize2', fmtSize(asset.size))
      setText('dlDigestShort', digest ? 'SHA256 ' + digest.slice(0, 12) + '…' : '')
      setText('dlSha', digest || '—')
      for (const id of ['dlLink', 'dlLink2']) {
        const a = document.getElementById(id)
        if (a && asset.browser_download_url) { a.href = asset.browser_download_url; a.setAttribute('download', '') }
      }
      setText('dlMeta', version ? `当前最新版 v${version} · ${fmtSize(asset.size)} · 点击即可下载` : '')
    })
    .catch(() => {
      setText('dlMeta', '暂时读不到版本信息，可稍后刷新；安装包目录仍在 /quean-update/')
    })
})()
