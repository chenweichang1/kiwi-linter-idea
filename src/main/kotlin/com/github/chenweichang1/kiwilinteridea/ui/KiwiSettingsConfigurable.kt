package com.github.chenweichang1.kiwilinteridea.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.github.chenweichang1.kiwilinteridea.settings.KiwiSettings
import javax.swing.*

/**
 * Kiwi-linter 设置界面
 */
class KiwiSettingsConfigurable(private val project: Project) : Configurable {
    
    private var settingsPanel: JPanel? = null
    private val projectIdField = JBTextField(20)
    private val targetBranchField = JBTextField(20)
    private val zhPropertiesPathField = JBTextField(40)
    private val commitTemplateField = JBTextField(40)
    private val privateTokenField = JBPasswordField()
    
    override fun getDisplayName(): String = "Kiwi-linter"
    
    override fun createComponent(): JComponent {
        val settings = KiwiSettings.getInstance(project)
        
        projectIdField.text = settings.state.projectId
        projectIdField.emptyText.text = "阿里 Code 平台项目 ID"
        
        targetBranchField.text = settings.state.targetBranch
        targetBranchField.emptyText.text = "例如: master, release"
        
        zhPropertiesPathField.text = settings.state.zhPropertiesPath
        zhPropertiesPathField.emptyText.text = "相对于仓库根目录的路径"
        
        commitTemplateField.text = settings.state.commitMessageTemplate
        commitTemplateField.emptyText.text = "支持 {key} 占位符"
        
        privateTokenField.text = settings.state.privateToken
        
        // 说明提示
        val tipLabel = JBLabel("<html><small>Private Token 可在阿里 Code 平台的右上角头像中获取</small></html>")
        
        settingsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("项目 ID:"), projectIdField, 1, false)
            .addLabeledComponent(JBLabel("目标分支:"), targetBranchField, 1, false)
            .addLabeledComponent(JBLabel("中文 Properties 路径:"), zhPropertiesPathField, 1, false)
            .addLabeledComponent(JBLabel("提交信息模板:"), commitTemplateField, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Private Token:"), privateTokenField, 1, false)
            .addComponentToRightColumn(tipLabel, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        settingsPanel?.border = JBUI.Borders.empty(10)
        return settingsPanel!!
    }
    
    override fun isModified(): Boolean {
        val settings = KiwiSettings.getInstance(project)
        return projectIdField.text != settings.state.projectId ||
                targetBranchField.text != settings.state.targetBranch ||
                zhPropertiesPathField.text != settings.state.zhPropertiesPath ||
                commitTemplateField.text != settings.state.commitMessageTemplate ||
                String(privateTokenField.password) != settings.state.privateToken
    }
    
    override fun apply() {
        val settings = KiwiSettings.getInstance(project)
        settings.state.projectId = projectIdField.text
        settings.state.targetBranch = targetBranchField.text
        settings.state.zhPropertiesPath = zhPropertiesPathField.text
        settings.state.commitMessageTemplate = commitTemplateField.text
        settings.state.privateToken = String(privateTokenField.password)
    }
    
    override fun reset() {
        val settings = KiwiSettings.getInstance(project)
        projectIdField.text = settings.state.projectId
        targetBranchField.text = settings.state.targetBranch
        zhPropertiesPathField.text = settings.state.zhPropertiesPath
        commitTemplateField.text = settings.state.commitMessageTemplate
        privateTokenField.text = settings.state.privateToken
    }
}
