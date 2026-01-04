package org.jetbrains.plugins.template.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.jetbrains.plugins.template.ui.KiwiToolWindowPanel

/**
 * Kiwi I18N 工具窗口工厂
 */
class KiwiToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = KiwiToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel.getContent(), "文案管理", false)
        toolWindow.contentManager.addContent(content)
    }
    
    override fun shouldBeAvailable(project: Project) = true
}

