package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.github.chenweichang1.kiwilinteridea.i18n.I18nExtractor
import com.github.chenweichang1.kiwilinteridea.ui.I18nEntryDialog
import com.github.chenweichang1.kiwilinteridea.ui.KiwiToolWindowPanel

/**
 * 从选中代码提取 I18N 文案的 Action
 * 提取后添加到工具窗口的表格中，统一提交
 */
class ExtractI18nAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        
        // 获取选中的文本
        val selectedText = editor.selectionModel.selectedText
        
        val entry = if (!selectedText.isNullOrBlank()) {
            // 尝试从选中文本提取
            I18nExtractor.extractFromSelection(selectedText)
        } else {
            // 尝试从当前行提取
            val document = editor.document
            val offset = editor.caretModel.offset
            val lineNumber = document.getLineNumber(offset)
            val lineStart = document.getLineStartOffset(lineNumber)
            val lineEnd = document.getLineEndOffset(lineNumber)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
            I18nExtractor.extractFromLine(lineText)
        }
        
        // 显示对话框确认
        val dialog = I18nEntryDialog(
            project = project,
            initialEntry = entry,
            dialogTitle = if (entry != null) "确认提取的 I18N 文案" else "手动录入 I18N 文案"
        )
        
        if (dialog.showAndGet()) {
            val finalEntry = dialog.getEntry()
            
            // 获取工具窗口面板，添加到表格
            val panel = KiwiToolWindowPanel.getInstance(project)
            if (panel != null) {
                panel.addEntry(finalEntry)
                
                // 打开工具窗口
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Kiwi-linter")
                toolWindow?.show()
                
                Messages.showInfoMessage(
                    project,
                    "已添加到待提交列表：\n\nKey: ${finalEntry.key}\n\n请在右侧工具窗口点击「统一上传」提交",
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
