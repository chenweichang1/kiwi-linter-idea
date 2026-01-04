package org.jetbrains.plugins.template.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.template.i18n.I18nExtractor

/**
 * I18N 模式标注器
 * 在编辑器中高亮显示可提取的 ErrorCode 模式
 */
class I18nPatternAnnotator : Annotator {
    
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // 只处理文件级别，避免重复处理
        if (element !is PsiFile) return
        
        val text = element.text
        val pattern = Regex("""(\w+)\s*\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*(?:,\s*\w+\.\w+)?\s*\)""")
        
        pattern.findAll(text).forEach { matchResult ->
            val range = TextRange(matchResult.range.first, matchResult.range.last + 1)
            
            // 提取 key 和 value 用于提示信息
            val key = matchResult.groupValues.getOrNull(2) ?: ""
            val value = matchResult.groupValues.getOrNull(3) ?: ""
            
            holder.newAnnotation(HighlightSeverity.INFORMATION, "I18N: $key")
                .range(range)
                .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                .tooltip("可提取的 I18N 文案\nKey: $key\nValue: $value\n\n使用 Alt+Enter 或右键菜单提取")
                .create()
        }
    }
}

