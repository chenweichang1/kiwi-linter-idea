package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * 批量手动录入 I18N 文案的 Action
 * 直接打开工具窗口，在表格中进行批量录入
 */
class BatchManualI18nEntryAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // 直接打开工具窗口
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Kiwi-linter")
        toolWindow?.show()
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
