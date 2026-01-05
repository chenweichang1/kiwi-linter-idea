package com.github.chenweichang1.kiwilinteridea.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.github.chenweichang1.kiwilinteridea.i18n.I18nExtractor
import com.github.chenweichang1.kiwilinteridea.services.I18nSubmitService
import com.github.chenweichang1.kiwilinteridea.ui.I18nEntryDialog

/**
 * I18N 文案提取的 Intention Action
 * 当光标在可识别的 ErrorCode 模式上时，会在灯泡菜单中显示
 */
class ExtractI18nIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getFamilyName(): String = "Kiwi-linter"
    
    override fun getText(): String = "提取 I18N 文案到仓库"
    
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) return false
        
        // 获取当前行文本
        val document = editor.document
        val offset = editor.caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        
        // 检查是否包含可提取的模式
        return I18nExtractor.containsI18nPattern(lineText)
    }
    
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        
        // 获取当前行文本
        val document = editor.document
        val offset = editor.caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        
        // 提取 I18N 条目
        val entry = I18nExtractor.extractFromLine(lineText)
        
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
    
    override fun startInWriteAction(): Boolean = false
}

