package com.github.chenweichang1.kiwilinteridea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.github.chenweichang1.kiwilinteridea.services.I18nSubmitService
import com.github.chenweichang1.kiwilinteridea.ui.I18nEntryDialog

/**
 * 手动录入 I18N 文案的 Action
 * 用于数据库存储 key 等复杂场景
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
            
            // 提交到仓库
            val submitService = I18nSubmitService.getInstance(project)
            when (val result = submitService.submitEntry(entry)) {
                is I18nSubmitService.SubmitResult.Success -> {
                    Messages.showInfoMessage(
                        project,
                        "文案已成功录入！\n\nKey: ${entry.key}\nValue: ${entry.value}",
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
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

