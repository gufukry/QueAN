#!/bin/sh
# 生成 latest.json：自动从 exe 内读取真实版本号；只收录最新修改的那个包
# 供手动执行，也供定时任务自动刷新（文件没变则跳过，不做无谓的 87MB 校验）
DIR="$(cd "$(dirname "$0")" && pwd)"; cd "$DIR" || exit 1
NEWEST="$(ls -t 2>/dev/null | grep -E '\.(exe|zip)$' | grep -v '^quean-update-' | head -1)"
[ -n "$NEWEST" ] || { echo "目录里还没有 exe/zip"; exit 1; }
STAMP="$(stat -c '%n:%s:%Y' "$NEWEST")"
if [ "$1" != "--force" ] && [ -f .manifest-stamp ] && [ "$(cat .manifest-stamp)" = "$STAMP" ] && [ -f latest.json ]; then exit 0; fi
BASE="$(head -1 config.url 2>/dev/null)"; [ -n "$BASE" ] || BASE="https://qafaka.cc.cd/quean-update"
# 版本号优先级：version.txt（随版本维护）> 从 exe 内读取 > 文件名
VER="$(head -1 version.txt 2>/dev/null | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')"
[ -n "$VER" ] || VER="$(grep -aoE '"version"[[:space:]]*:[[:space:]]*"[0-9]+\.[0-9]+\.[0-9]+"' "$NEWEST" 2>/dev/null | head -1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')"
[ -n "$VER" ] || VER="$(echo "$NEWEST" | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1)"
[ -n "$VER" ] || { echo "无法从 $NEWEST 判断版本号，请写 version.txt"; exit 1; }
BODY="$(awk 'BEGIN{ORS="\\n"} {gsub(/\\/,"\\\\"); gsub(/"/,"\\\""); print}' body.txt 2>/dev/null)"
SIZE="$(stat -c %s "$NEWEST")"; SHA="$(sha256sum "$NEWEST" | cut -d' ' -f1)"
printf '{\n  "tag_name": "v%s",\n  "body": "%s",\n  "assets": [\n    { "name": "%s", "browser_download_url": "%s/%s", "digest": "sha256:%s", "size": %s }\n  ]\n}\n' "$VER" "$BODY" "$NEWEST" "$BASE" "$NEWEST" "$SHA" "$SIZE" > latest.json
printf '%s' "$STAMP" > .manifest-stamp
echo "已生成 latest.json（版本 $VER，包 $NEWEST）"
