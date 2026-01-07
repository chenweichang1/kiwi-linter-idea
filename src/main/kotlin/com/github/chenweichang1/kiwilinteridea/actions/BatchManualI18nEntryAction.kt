package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.github.chenweichang1.kiwilinteridea.services.I18nSubmitService
import com.github.chenweichang1.kiwilinteridea.ui.BatchManualI18nDialog

/**
 * 批量手动录入 I18N 文案的 Action
 * 支持一次性输入多条文案，统一用一个 commit 提交
 */
class BatchManualI18nEntryAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // 显示批量录入对话框
        val dialog = BatchManualI18nDialog(project)
        
        if (dialog.showAndGet()) {
            val entries = dialog.getEntries()
            
            if (entries.isEmpty()) {
                Messages.showInfoMessage(project, "没有需要提交的文案", "提示")
                return
            }
            
            // 批量提交到仓库
            val submitService = I18nSubmitService.getInstance(project)
            when (val result = submitService.submitEntries(entries)) {
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
                    
                    // 标题根据是否有实际变更来决定
                    val title = if (result.changedCount > 0) "批量录入成功" else "批量录入完成"
                    
                    Messages.showInfoMessage(
                        project,
                        statsMsg,
                        title
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
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

