package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.github.chenweichang1.kiwilinteridea.i18n.I18nExtractor
import com.github.chenweichang1.kiwilinteridea.ui.BatchI18nDialog
import com.github.chenweichang1.kiwilinteridea.ui.KiwiToolWindowPanel

/**
 * 从当前文件批量提取 I18N 文案的 Action
 * 提取后添加到工具窗口的表格中，统一提交
 */
class BatchExtractI18nAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        
        // 从文件内容提取所有 I18N 条目
        val fileContent = editor.document.text
        val entries = I18nExtractor.extractFromFile(fileContent)
        
        if (entries.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "当前文件中未找到可提取的 I18N 文案模式",
                "提示"
            )
            return
        }
        
        // 显示批量选择对话框
        val dialog = BatchI18nDialog(project, entries)
        
        if (dialog.showAndGet()) {
            val selectedEntries = dialog.getSelectedEntries()
            
            if (selectedEntries.isEmpty()) {
                Messages.showInfoMessage(project, "未选择任何文案", "提示")
                return
            }
            
            // 获取工具窗口面板，添加到表格
            val panel = KiwiToolWindowPanel.getInstance(project)
            if (panel != null) {
                panel.addEntries(selectedEntries)
                
                // 打开工具窗口
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Kiwi-linter")
                toolWindow?.show()
                
                Messages.showInfoMessage(
                    project,
                    "已添加 ${selectedEntries.size} 条到待提交列表\n\n请在右侧工具窗口点击「统一上传」提交",
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
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}
