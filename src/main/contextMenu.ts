import { BrowserWindow, Menu, type MenuItemConstructorOptions } from 'electron'

/**
 * Electron 渲染层默认没有右键菜单。为可编辑控件与选中文字提供基础的
 * 剪切/复制/粘贴/全选，避免输入框里无法右键粘贴。
 * role 决定行为，label 显式写中文，避免默认跟随系统语言显示英文。
 */
export function installContextMenu(window: BrowserWindow): void {
  window.webContents.on('context-menu', (_event, params) => {
    const template: MenuItemConstructorOptions[] = []
    if (params.isEditable) {
      template.push(
        { role: 'undo', label: '撤销' },
        { role: 'redo', label: '重做' },
        { type: 'separator' },
        { role: 'cut', label: '剪切' },
        { role: 'copy', label: '复制' },
        { role: 'paste', label: '粘贴' },
        { type: 'separator' },
        { role: 'selectAll', label: '全选' }
      )
    } else if (params.selectionText && params.selectionText.trim()) {
      template.push({ role: 'copy', label: '复制' }, { type: 'separator' }, { role: 'selectAll', label: '全选' })
    }
    if (!template.length) return
    Menu.buildFromTemplate(template).popup({ window })
  })
}
