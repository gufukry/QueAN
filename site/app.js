/* QueAN 官网脚本：下载信息实时读取更新清单，更新日志读取 versions.json */
(function () {
  const UPDATE_MANIFEST = './quean-update/latest.json'

  const fmtSize = (bytes) => {
    if (!bytes || !Number.isFinite(bytes)) return '—'
    return bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + ' MB' : Math.round(bytes / 1024) + ' KB'
  }
  const fmtDate = (value) => {
    const d = new Date(value)
    if (Number.isNaN(d.getTime())) return ''
    const p = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
  }
  const setText = (id, text) => {
    const el = document.getElementById(id)
    if (el && text) el.textContent = text
  }

  // 滚动浮现与顶栏状态
  const reveal = (el) => el && el.classList.add('in')
  if ('IntersectionObserver' in window) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => { if (e.isIntersecting) { reveal(e.target); io.unobserve(e.target) } })
    }, { rootMargin: '0px 0px -8% 0px', threshold: 0.08 })
    document.querySelectorAll('.reveal').forEach((el) => io.observe(el))
  } else {
    document.querySelectorAll('.reveal').forEach(reveal)
  }
  const onScroll = () => document.querySelector('header.top')?.classList.toggle('scrolled', window.scrollY > 8)
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })

  // 下载区：数据来自站点旁 /quean-update/latest.json（与你上传的包永远一致）
  fetch(UPDATE_MANIFEST, { cache: 'no-store' })
    .then((r) => (r.ok ? r.json() : Promise.reject(new Error('HTTP ' + r.status))))
    .then((manifest) => {
      const version = String(manifest.tag_name || '').replace(/^v/, '')
      const asset = (manifest.assets || [])[0] || {}
      setText('verBadge', version ? '最新版本 v' + version : '')
      setText('dlVersion', version ? 'QueAN v' + version : null)
      setText('dlSize', fmtSize(asset.size))
      setText('dlDate', manifest.published_at ? fmtDate(manifest.published_at) : '')
      setText('dlSha', String(asset.digest || '').replace(/^sha256:/, '') || '—')
      const link = document.getElementById('dlLink')
      if (link && asset.browser_download_url) {
        link.href = asset.browser_download_url
        link.setAttribute('download', '')
      }
      setText('dlMeta', version ? `最新版本 v${version} · ${fmtSize(asset.size)} · 可直接下载` : '')
    })
    .catch(() => {
      setText('dlMeta', '暂时读不到版本信息，可稍后刷新；更新包目录仍在 /quean-update/')
    })

  // 更新日志：由 scripts/site-versions.cjs 从 updateNotes.ts 生成
  fetch('./versions.json', { cache: 'no-store' })
    .then((r) => (r.ok ? r.json() : Promise.reject(new Error('HTTP ' + r.status))))
    .then((data) => {
      const box = document.getElementById('log')
      const versions = (data && data.versions) || []
      if (!box || !versions.length) throw new Error('empty')
      box.innerHTML = ''
      versions.forEach((rel, index) => {
        const card = document.createElement('article')
        card.className = 'rel reveal'
        card.style.setProperty('--i', String(index))
        const head = document.createElement('div')
        head.className = 'rel-head'
        const title = document.createElement('b')
        title.textContent = 'QueAN v' + rel.version
        head.appendChild(title)
        if (index === 0) {
          const tag = document.createElement('span')
          tag.className = 'tag'
          tag.textContent = '最新'
          head.appendChild(tag)
        }
        const time = document.createElement('time')
        time.textContent = rel.date || ''
        head.appendChild(time)
        card.appendChild(head)
        const list = document.createElement('ul')
        ;(rel.changes || []).forEach((line) => {
          const li = document.createElement('li')
          li.textContent = line
          list.appendChild(li)
        })
        card.appendChild(list)
        box.appendChild(card)
      })
      requestAnimationFrame(() => {
        box.querySelectorAll('.reveal').forEach((el) => el.classList.add('in'))
      })
    })
    .catch(() => {
      const box = document.getElementById('log')
      if (box) box.innerHTML = '<div class="skeleton">更新日志加载失败，请刷新页面重试。</div>'
    })
})()
