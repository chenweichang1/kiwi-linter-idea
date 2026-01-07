package com.github.chenweichang1.kiwilinteridea.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.github.chenweichang1.kiwilinteridea.i18n.I18nEntry
import com.github.chenweichang1.kiwilinteridea.services.I18nSubmitService
import com.github.chenweichang1.kiwilinteridea.settings.KiwiSettings
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

/**
 * Kiwi-linter 工具窗口面板
 */
class KiwiToolWindowPanel(private val project: Project) {
    
    private val keyField = JBTextField(30).apply {
        emptyText.text = "输入 Key，如: DPN.DataProcess.CalendarNotFound"
    }
    
    private val valueArea = JBTextArea(3, 30).apply {
        lineWrap = true
        wrapStyleWord = true
    }
    
    private val historyArea = JBTextArea(10, 30).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }
    
    private val submitButton = JButton("提交文案").apply {
        addActionListener { submitEntry() }
    }
    
    private val batchButton = JButton("批量录入").apply {
        addActionListener { openBatchDialog() }
    }
    
    private val clearButton = JButton("清空").apply {
        addActionListener { clearFields() }
    }
    
    fun getContent(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        
        // 输入区域
        val inputPanel = createInputPanel()
        
        // 按钮区域
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(submitButton)
            add(batchButton)
            add(clearButton)
        }
        
        // 历史记录区域
        val historyPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyTop(10)
            add(JBLabel("最近提交:"), BorderLayout.NORTH)
            add(JBScrollPane(historyArea), BorderLayout.CENTER)
        }
        
        // 状态区域
        val settings = KiwiSettings.getInstance(project)
        val statusLabel = JBLabel().apply {
            text = if (settings.state.projectId.isNotBlank()) {
                "项目 ID: ${settings.state.projectId} | 分支: ${settings.state.targetBranch}"
            } else {
                "⚠️ 请先配置项目信息 (Settings -> Tools -> Kiwi-linter)"
            }
        }
        
        val topPanel = JPanel(BorderLayout()).apply {
            add(inputPanel, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
        
        mainPanel.apply {
            border = JBUI.Borders.empty(10)
            add(topPanel, BorderLayout.NORTH)
            add(historyPanel, BorderLayout.CENTER)
            add(statusLabel, BorderLayout.SOUTH)
        }
        
        return mainPanel
    }
    
    private fun createInputPanel(): JPanel {
        val valueScrollPane = JBScrollPane(valueArea).apply {
            preferredSize = JBUI.size(300, 60)
        }
        
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Key:"), keyField, 1, false)
            .addLabeledComponent(JBLabel("中文文案:"), valueScrollPane, 1, false)
            .panel
    }
    
    private fun submitEntry() {
        val key = keyField.text.trim()
        val value = valueArea.text.trim()
        
        if (key.isBlank()) {
            Messages.showWarningDialog(project, "请输入 Key", "提示")
            return
        }
        
        if (value.isBlank()) {
            Messages.showWarningDialog(project, "请输入中文文案", "提示")
            return
        }
        
        val entry = I18nEntry(key, value)
        val submitService = I18nSubmitService.getInstance(project)
        
        when (val result = submitService.submitEntry(entry)) {
            is I18nSubmitService.SubmitResult.Success -> {
                // 根据实际变更决定是否添加到历史记录
                if (result.changedCount > 0) {
                    val history = historyArea.text
                    val newHistory = "${entry.toPropertiesLine()}\n$history"
                    historyArea.text = newHistory
                    // 清空输入
                    clearFields()
                }
                
                // 构建提示消息
                val msg = when {
                    result.skipped > 0 -> "文案已存在且内容相同，已跳过"
                    result.updated > 0 -> "文案已更新"
                    else -> "文案已新增"
                }
                val title = if (result.changedCount > 0) "提交成功" else "提交完成"
                Messages.showInfoMessage(project, msg, title)
            }
            is I18nSubmitService.SubmitResult.Failure -> {
                Messages.showErrorDialog(project, result.error, "提交失败")
            }
        }
    }
    
    private fun clearFields() {
        keyField.text = ""
        valueArea.text = ""
        keyField.requestFocus()
    }
    
    private fun openBatchDialog() {
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
                    // 只有实际变更的条目才添加到历史记录
                    if (result.changedCount > 0) {
                        val newEntries = entries.joinToString("\n") { it.toPropertiesLine() }
                        val history = historyArea.text
                        historyArea.text = "$newEntries\n$history"
                    }
                    
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
                    Messages.showErrorDialog(project, result.error, "批量录入失败")
                }
            }
        }
    }
}

