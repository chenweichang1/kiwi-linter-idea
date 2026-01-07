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
                    // 构建详细的统计信息
                    val statsMsg = buildString {
                        if (result.added > 0) append("新增 ${result.added} 条")
                        if (result.updated > 0) {
                            if (isNotEmpty()) append("，")
                            append("更新 ${result.updated} 条")
                        }
                        if (result.skipped > 0) {
                            if (isNotEmpty()) append("，")
                            append("跳过 ${result.skipped} 条（已存在且内容相同）")
                        }
                        if (isEmpty()) append("没有需要变更的内容")
                    }
                    
                    val title = if (result.changedCount > 0) "批量录入成功" else "批量录入完成"
                    Messages.showInfoMessage(project, statsMsg, title)
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

