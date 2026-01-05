package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.github.chenweichang1.kiwilinteridea.i18n.I18nEntry
import com.github.chenweichang1.kiwilinteridea.i18n.I18nExtractor
import com.github.chenweichang1.kiwilinteridea.services.I18nSubmitService
import com.github.chenweichang1.kiwilinteridea.ui.BatchI18nDialog

/**
 * 从当前文件批量提取 I18N 文案的 Action
 */
class BatchExtractI18nAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
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
            
            // 批量提交
            val submitService = I18nSubmitService.getInstance(project)
            when (val result = submitService.submitEntries(selectedEntries)) {
                is I18nSubmitService.SubmitResult.Success -> {
                    Messages.showInfoMessage(
                        project,
                        "成功录入 ${selectedEntries.size} 条文案！",
                        "批量录入成功"
                    )
                }
                is I18nSubmitService.SubmitResult.Failure -> {
                    Messages.showErrorDialog(
                        project,
                        result.error,
                        "批量录入失败"
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

