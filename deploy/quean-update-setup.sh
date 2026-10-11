#!/bin/sh
# QueAN 自建更新目录初始化（在服务器上跑一次）
# 作用：建目录 + 生成与 GitHub Release 同构的 latest.json（扫描 exe/zip 取 sha256 与大小）
set -e
DIR="${1:-/www/wwwroot/quean-update}"
mkdir -p "$DIR"
cat > "$DIR/build-manifest.sh" <<'SCRIPT'
#!/bin/sh
# 扫描本目录的 exe/zip，生成 latest.json（tag_name 取文件名里的版本号）
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
VERSION="$(ls *.exe *.zip 2>/dev/null | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | sort -V | tail -1)"
[ -n "$VERSION" ] || { echo "目录里没有带 x.y.z 版本号的 exe/zip"; exit 1; }
# 更新说明按单行处理：转义反斜杠与双引号（避免依赖 python 等额外解释器）
BODY="$(head -1 body.txt 2>/dev/null | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g')"
{
  printf '{\n  "tag_name": "v%s",\n  "body": "%s",\n  "assets": [\n' "$VERSION" "$BODY"
  FIRST=1
  for f in *.exe *.zip; do
    [ -f "$f" ] || continue
    SIZE="$(stat -c %s "$f")"
    SHA="$(sha256sum "$f" | cut -d' ' -f1)"
    BASE="$(grep -oE 'https?://[^ ]+' config.url 2>/dev/null | head -1)"
    [ -n "$BASE" ] || BASE="https://qafaka.cc.cd/quean-update"
    [ "$FIRST" = 0 ] && printf ',\n'
    FIRST=0
    printf '    { "name": "%s", "browser_download_url": "%s/%s", "digest": "sha256:%s", "size": %s }' "$f" "$BASE" "$f" "$SHA" "$SIZE"
  done
  printf '\n  ]\n}\n'
} > latest.json
echo "已生成 $DIR/latest.json（版本 $VERSION）"
SCRIPT
chmod +x "$DIR/build-manifest.sh"
printf '%s' 'https://qafaka.cc.cd/quean-update' > "$DIR/config.url"
printf '%s' '在这里写更新说明（一行纯文本；支持换行）' > "$DIR/body.txt"
echo "目录就绪：$DIR"
echo "下一步：把 exe/zip 放进该目录，然后执行 sh $DIR/build-manifest.sh"
