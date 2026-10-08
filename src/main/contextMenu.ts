import { BrowserWindow, Menu, type MenuItemConstructorOptions } from 'electron'

/**
 * Electron 渲染层默认没有右键菜单。为可编辑控件与选中文字提供基础的
 * 剪切/复制/粘贴/全选，避免输入框里无法右键粘贴。
 */
export function installContextMenu(window: BrowserWindow): void {
  window.webContents.on('context-menu', (_event, params) => {
    const template: MenuItemConstructorOptions[] = []
    if (params.isEditable) {
      template.push(
        { role: 'undo' }, { role: 'redo' }, { type: 'separator' },
        { role: 'cut' }, { role: 'copy' }, { role: 'paste' }, { type: 'separator' },
        { role: 'selectAll' }
      )
    } else if (params.selectionText && params.selectionText.trim()) {
      template.push({ role: 'copy' }, { type: 'separator' }, { role: 'selectAll' })
    }
    if (!template.length) return
    Menu.buildFromTemplate(template).popup({ window })
  })
}
