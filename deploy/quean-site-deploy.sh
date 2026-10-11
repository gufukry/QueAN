#!/bin/sh
# 把 site/ 目录部署到站点根（只新增/覆盖站点文件，不动 quean-update/ 与 .user.ini）
set -e
SRC="${1:-site}"
DEST="${2:-/www/wwwroot/qafaka.cc.cd}"
[ -d "$SRC" ] || { echo "找不到源目录 $SRC"; exit 1; }
[ -d "$DEST" ] || { echo "找不到站点根 $DEST"; exit 1; }
node scripts/site-versions.cjs
for f in "$SRC"/index.html "$SRC"/style.css "$SRC"/app.js "$SRC"/versions.json "$SRC"/logo.png; do
  [ -f "$f" ] || { echo "缺少 $f"; exit 1; }
done
cp -f "$SRC"/index.html "$SRC"/style.css "$SRC"/app.js "$SRC"/versions.json "$SRC"/logo.png "$DEST"/
chown -R www:www "$DEST"/index.html "$DEST"/style.css "$DEST"/app.js "$DEST"/versions.json "$DEST"/logo.png 2>/dev/null || true
chmod 644 "$DEST"/index.html "$DEST"/style.css "$DEST"/app.js "$DEST"/versions.json "$DEST"/logo.png
echo "已部署到 $DEST（index.html / style.css / app.js / versions.json / logo.png）"
