package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.template.services.I18nSubmitService
import org.jetbrains.plugins.template.ui.BatchManualI18nDialog

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
                    val summary = entries.take(3).joinToString("\n") { "• ${it.key}" } +
                        if (entries.size > 3) "\n... 等 ${entries.size} 条" else ""
                    
                    Messages.showInfoMessage(
                        project,
                        "成功录入 ${entries.size} 条文案！\n\n$summary",
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
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

