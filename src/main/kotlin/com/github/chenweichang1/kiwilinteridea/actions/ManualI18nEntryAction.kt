package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.github.chenweichang1.kiwilinteridea.ui.I18nEntryDialog
import com.github.chenweichang1.kiwilinteridea.ui.KiwiToolWindowPanel

/**
 * 手动录入 I18N 文案的 Action
 * 用于数据库存储 key 等复杂场景
 * 录入后添加到工具窗口的表格中，统一提交
 */
class ManualI18nEntryAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // 显示空白的录入对话框
        val dialog = I18nEntryDialog(
            project = project,
            initialEntry = null,
            dialogTitle = "手动录入 I18N 文案"
        )
        
        if (dialog.showAndGet()) {
            val entry = dialog.getEntry()
            
            // 获取工具窗口面板，添加到表格
            val panel = KiwiToolWindowPanel.getInstance(project)
            if (panel != null) {
                panel.addEntry(entry)
                
                // 打开工具窗口
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Kiwi-linter")
                toolWindow?.show()
                
                Messages.showInfoMessage(
                    project,
                    "已添加到待提交列表：\n\nKey: ${entry.key}\n\n请在右侧工具窗口点击「统一上传」提交",
                    "✅ 添加成功"
                )
            } else {
                Messages.showWarningDialog(
                    project,
                    "请先打开 Kiwi-linter 工具窗口",
                    "提示"
                )
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
