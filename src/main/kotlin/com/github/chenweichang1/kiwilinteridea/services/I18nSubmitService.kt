package com.github.chenweichang1.kiwilinteridea.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.chenweichang1.kiwilinteridea.i18n.I18nEntry
import com.github.chenweichang1.kiwilinteridea.settings.KiwiSettings

/**
 * I18N 文案提交服务
 * 负责将文案提交到远程仓库
 */
@Service(Service.Level.PROJECT)
class I18nSubmitService(private val project: Project) {
    
    private val logger = thisLogger()
    
    /**
     * 提交结果
     */
    sealed class SubmitResult {
        data class Success(val message: String) : SubmitResult()
        data class Failure(val error: String) : SubmitResult()
    }
    
    /**
     * 提交单个 I18N 条目到仓库
     */
    fun submitEntry(entry: I18nEntry): SubmitResult {
        val settings = KiwiSettings.getInstance(project)
        
        if (settings.state.projectId.isBlank()) {
            return SubmitResult.Failure("请先在设置中配置项目 ID (Settings -> Tools -> Kiwi-linter)")
        }
        
        return try {
            // 构建提交内容
            val newLine = entry.toPropertiesLine()
            val commitMessage = settings.state.commitMessageTemplate
                .replace("{key}", entry.key)
            
            logger.info("准备提交 I18N 文案: ${entry.key}")
            
            // 使用 CodePlatformService 提交
            val codePlatformService = CodePlatformService.getInstance(project)
            val result = codePlatformService.commitFile(
                repoPath = settings.state.projectId,
                branch = settings.state.targetBranch,
                filePath = settings.state.zhPropertiesPath,
                content = newLine,
                commitMessage = commitMessage,
                append = true
            )
            
            result.fold(
                onSuccess = { 
                    showNotification("I18N 文案录入成功", "Key: ${entry.key}", NotificationType.INFORMATION)
                    SubmitResult.Success("文案已成功提交到仓库") 
                },
                onFailure = { 
                    showNotification("I18N 文案录入失败", it.message ?: "未知错误", NotificationType.ERROR)
                    SubmitResult.Failure("提交失败: ${it.message}") 
                }
            )
        } catch (e: Exception) {
            logger.error("提交失败", e)
            showNotification("I18N 文案录入失败", e.message ?: "未知错误", NotificationType.ERROR)
            SubmitResult.Failure("提交失败: ${e.message}")
        }
    }
    
    /**
     * 批量提交多个 I18N 条目
     */
    fun submitEntries(entries: List<I18nEntry>): SubmitResult {
        if (entries.isEmpty()) {
            return SubmitResult.Failure("没有需要提交的文案")
        }
        
        val settings = KiwiSettings.getInstance(project)
        
        if (settings.state.projectId.isBlank()) {
            return SubmitResult.Failure("请先在设置中配置项目 ID (Settings -> Tools -> Kiwi-linter)")
        }
        
        return try {
            val content = entries.joinToString("\n") { it.toPropertiesLine() }
            val keys = entries.take(3).map { it.key }.joinToString(", ") + 
                if (entries.size > 3) " 等${entries.size}条" else ""
            val commitMessage = "feat: 批量添加 I18N 文案 - $keys"
            
            val codePlatformService = CodePlatformService.getInstance(project)
            val result = codePlatformService.commitFile(
                repoPath = settings.state.projectId,
                branch = settings.state.targetBranch,
                filePath = settings.state.zhPropertiesPath,
                content = content,
                commitMessage = commitMessage,
                append = true
            )
            
            result.fold(
                onSuccess = { 
                    showNotification("批量录入成功", "成功录入 ${entries.size} 条文案", NotificationType.INFORMATION)
                    SubmitResult.Success("成功提交 ${entries.size} 条文案") 
                },
                onFailure = { 
                    showNotification("批量录入失败", it.message ?: "未知错误", NotificationType.ERROR)
                    SubmitResult.Failure("批量提交失败: ${it.message}") 
                }
            )
        } catch (e: Exception) {
            logger.error("批量提交失败", e)
            showNotification("批量录入失败", e.message ?: "未知错误", NotificationType.ERROR)
            SubmitResult.Failure("批量提交失败: ${e.message}")
        }
    }
    
    /**
     * 显示通知
     */
    private fun showNotification(title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Kiwi-linter")
            .createNotification(title, content, type)
            .notify(project)
    }
    
    companion object {
        fun getInstance(project: Project): I18nSubmitService {
            return project.service<I18nSubmitService>()
        }
    }
}
