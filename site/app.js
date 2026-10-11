/* QueAN 官网脚本：下载信息实时读取更新清单；动效渐进增强（JS 不可用时页面完整可见） */
(function () {
  const MANIFEST = './quean-update/latest.json'
  const fmt = (n) => (!n || !Number.isFinite(n) ? '—' : n >= 1048576 ? (n / 1048576).toFixed(1) + ' MB' : Math.round(n / 1024) + ' KB')
  const set = (id, v) => { const el = document.getElementById(id); if (el && v != null) el.textContent = v }

  if (!matchMedia('(prefers-reduced-motion: reduce)').matches) {
    document.documentElement.classList.add('motion')
    const items = document.querySelectorAll('.release, .nums li, .feature, .card')
    items.forEach((el) => el.classList.add('reveal'))
    if ('IntersectionObserver' in window) {
      const io = new IntersectionObserver((entries) => {
        entries.forEach((e) => { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target) } })
      }, { rootMargin: '0px 0px -6% 0px', threshold: 0.06 })
      items.forEach((el) => io.observe(el))
    } else {
      items.forEach((el) => el.classList.add('in'))
    }
  }
  // 截图位：图片到位后隐藏占位标签与微光骨架
  document.querySelectorAll('.shot').forEach((el) => {
    const url = getComputedStyle(el).getPropertyValue('--shot').trim().replace(/^url\(['"]?/, '').replace(/['"]?\)$/, '')
    if (!url) return
    const probe = new Image()
    probe.onload = () => el.classList.add('loaded')
    probe.src = url
  })

  const onScroll = () => document.querySelector('.nav')?.classList.toggle('scrolled', window.scrollY > 8)
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })

  fetch(MANIFEST, { cache: 'no-store' })
    .then((r) => (r.ok ? r.json() : Promise.reject(new Error('HTTP ' + r.status))))
    .then((m) => {
      const version = String(m.tag_name || '').replace(/^v/, '')
      const asset = (m.assets || [])[0] || {}
      const digest = String(asset.digest || '').replace(/^sha256:/, '')
      if (version) { set('dlVersion', 'QueAN v' + version); set('heroNote', `v${version} · ${fmt(asset.size)}`) }
      set('dlSize', fmt(asset.size))
      set('dlDigestShort', digest ? 'SHA256 ' + digest.slice(0, 12) + '…' : '')
      set('dlSha', digest || '—')
      const a = document.getElementById('dlLink')
      if (a && asset.browser_download_url) { a.href = asset.browser_download_url; a.setAttribute('download', '') }
      if (!version) set('heroNote', '')
    })
    .catch(() => set('heroNote', '暂时读不到版本信息，可稍后刷新'))
})()
