#!/bin/sh
set -e
DIR="${1:-/www/wwwroot/qafaka.cc.cd/quean-update}"
cd "$DIR"
[ -f build-manifest.sh ] || { echo "缺少 build-manifest.sh"; exit 1; }
# 用最新脚本替换旧的（旧的必须带版本号文件名，这里统一成自动识别）
if [ -f quean-update-build.sh ]; then mv -f quean-update-build.sh build-manifest.sh; fi
chmod 755 build-manifest.sh
# 定时任务：每 5 分钟检查一次，包变了才重建清单
LINE="*/5 * * * * cd $DIR && sh build-manifest.sh >/dev/null 2>&1"
crontab -l 2>/dev/null | grep -F "$DIR && sh build-manifest.sh" >/dev/null || { crontab -l 2>/dev/null; echo "$LINE"; } | crontab -
sh build-manifest.sh --force
echo "自动刷新已开启（每 5 分钟）"
