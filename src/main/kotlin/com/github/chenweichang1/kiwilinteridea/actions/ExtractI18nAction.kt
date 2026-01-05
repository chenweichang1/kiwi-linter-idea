package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.github.chenweichang1.kiwilinteridea.i18n.I18nExtractor
import com.github.chenweichang1.kiwilinteridea.services.I18nSubmitService
import com.github.chenweichang1.kiwilinteridea.ui.I18nEntryDialog

/**
 * 从选中代码提取 I18N 文案的 Action
 * 可通过右键菜单或快捷键触发
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
        
        // 显示对话框
        val dialog = I18nEntryDialog(
            project = project,
            initialEntry = entry,
            dialogTitle = if (entry != null) "确认提取的 I18N 文案" else "手动录入 I18N 文案"
        )
        
        if (dialog.showAndGet()) {
            val finalEntry = dialog.getEntry()
            
            // 提交到仓库
            val submitService = I18nSubmitService.getInstance(project)
            when (val result = submitService.submitEntry(finalEntry)) {
                is I18nSubmitService.SubmitResult.Success -> {
                    Messages.showInfoMessage(
                        project,
                        "文案已成功录入！\n\nKey: ${finalEntry.key}\nValue: ${finalEntry.value}",
                        "录入成功"
                    )
                }
                is I18nSubmitService.SubmitResult.Failure -> {
                    Messages.showErrorDialog(
                        project,
                        result.error,
                        "录入失败"
                    )
                }
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}

