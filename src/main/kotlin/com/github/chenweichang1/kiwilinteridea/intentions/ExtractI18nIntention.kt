package com.github.chenweichang1.kiwilinteridea.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.github.chenweichang1.kiwilinteridea.i18n.I18nExtractor
import com.github.chenweichang1.kiwilinteridea.ui.I18nEntryDialog
import com.github.chenweichang1.kiwilinteridea.ui.KiwiToolWindowPanel

/**
 * I18N 文案提取的 Intention Action
 * 当光标在可识别的 ErrorCode 模式上时，会在灯泡菜单中显示
 * 提取后添加到工具窗口的表格中，统一提交
 */
class ExtractI18nIntention : PsiElementBaseIntentionAction(), IntentionAction {
    
    override fun getFamilyName(): String = "Kiwi-linter"
    
    override fun getText(): String = "提取 I18N 文案到待提交列表"
    
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
    
    override fun startInWriteAction(): Boolean = false
}
