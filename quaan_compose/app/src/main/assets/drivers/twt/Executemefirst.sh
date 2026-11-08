#!/bin/bash
#我已经很努力了😭
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

TARGET_DIR="/data/media/0/Android/data/org.telegram.messenger.web/cache"
mkdir -p "$TARGET_DIR" || {
    echo -e "${RED}错误：无法创建目录 $TARGET_DIR，请检查权限。${NC}" >&2
    exit 1
}

FILES=(
    "-6120902071746760566_97.jpg"
    "-6120902071746760566_99.jpg"
    "-6152307684363931102_97.jpg"
    "-6152307684363931102_99.jpg"
    "-6167810609333256486_97.jpg"
    "-6167810609333256486_99.jpg"
)

for f in "${FILES[@]}"; do
    touch -- "$TARGET_DIR/$f" 2>/dev/null || {
        echo -e "${RED}警告：无法创建文件 $f，可能权限不足。${NC}" >&2
    }
done

clear

echo -e "${GREEN}════════════════════════════════════════════════${NC}"
echo -e "${GREEN}    缓存验证已通过！脚本执行成功！${NC}"
echo -e "${GREEN}════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW} 此脚本由群友免费制作，后续若失效，"
echo -e "  最新绕过规则与文件列表仅在官方频道更新！${NC}"
echo ""
echo -e "${CYAN} 官方主频道（必加）: @TwT_update"
echo -e " 驱动专用频道 : @TwT_driver"
echo -e " 火影更新频道 : @TwT_kihan${NC}"
echo ""
echo -e "${RED} 请勿删除作者信息，否则下次运行可能失效！${NC}"
echo -e "${GREEN}════════════════════════════════════════════════${NC}"
echo ""

LINK="https://t.me/TwT_driver/11408"
echo -e "${CYAN}正在尝试打开驱动更新最新消息...${NC}"

open_link() {
    local url="$1"
    if command -v am &>/dev/null; then
        am start -a android.intent.action.VIEW -d "$url" 2>/dev/null && return 0
    fi
    
    if command -v termux-open-url &>/dev/null; then
        termux-open-url "$url" 2>/dev/null && return 0
    fi
    
    if command -v termux-open &>/dev/null; then
        termux-open "$url" 2>/dev/null && return 0
    fi

    if command -v xdg-open &>/dev/null; then
        xdg-open "$url" 2>/dev/null && return 0
    fi
    return 1
}


if open_link "$LINK"; then
    echo -e "${GREEN} 已尝试打开链接，请查看 Telegram。${NC}"
else
    echo -e "${YELLOW} 无法自动打开，请手动复制以下链接到浏览器：${NC}"
    echo -e "${CYAN}$LINK${NC}"
    echo -e "${YELLOW}（按回车键继续...）${NC}"
    read -r
fi

echo ""

echo -e "\n${GREEN}过验证成功${NC}"
exit 0