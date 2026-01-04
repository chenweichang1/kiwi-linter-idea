# Kiwi-linter - IDEA 国际化文案录入插件

[![Build](https://github.com/JetBrains/intellij-platform-plugin-template/workflows/Build/badge.svg)][gh:build]

一个用于快速提交 I18N 国际化文案至二方包仓库的 IntelliJ IDEA 插件。

<!-- Plugin description -->

**Kiwi-linter** is a powerful IntelliJ IDEA plugin for managing I18N internationalization entries.

### Features

- **Auto Extract**: Automatically detect ErrorCode enum patterns and extract key-value pairs
- **Manual Entry**: Manually input key-value pairs for database-stored keys
- **Batch Extract**: Extract all I18N entries from current file at once
- **One-click Push**: Directly commit to remote properties repository

### Usage

- Right-click menu -> Kiwi-linter -> Extract / Manual Entry
- Alt+Enter intention menu -> Extract I18N to repository
- Tools menu -> Kiwi-linter

<!-- Plugin description end -->

## 使用方式

### 方式一：右键菜单

1. 选中包含 ErrorCode 定义的代码
2. 右键 -> `Kiwi-linter` -> `提取 I18N 文案`

### 方式二：灯泡菜单 (Alt+Enter)

1. 将光标放在 ErrorCode 定义行
2. 按 `Alt+Enter` 打开灯泡菜单
3. 选择 `提取 I18N 文案到仓库`

### 方式三：快捷键

- `Ctrl+Alt+I` - 提取 I18N 文案
- `Ctrl+Alt+Shift+I` - 手动录入 I18N 文案

### 方式四：工具窗口

- 打开右侧 `Kiwi-linter` 工具窗口
- 直接在表单中输入 key 和 value

## 配置

进入 `Settings/Preferences` -> `Tools` -> `Kiwi-linter` 进行配置：

| 配置项          | 说明               | 示例                                                  |
| --------------- | ------------------ | ----------------------------------------------------- |
| 仓库路径        | 二方包仓库的路径   | `group/i18n-repo`                                     |
| 目标分支        | 提交的目标分支     | `master`                                              |
| Properties 路径 | 中文文案文件路径   | `src/main/resources/dataphin_i18n_data_zh.properties` |
| 提交信息模板    | Git 提交信息模板   | `feat: 添加 I18N 文案 - {key}`                        |
| 自动推送        | 提交后是否自动推送 | 开启/关闭                                             |

## 支持的代码模式

### ErrorCode 枚举模式

```java
public enum PublicCalendarErrorCode implements Errors {
    // 根据id找不到公共日历
    CALENDAR_NOT_FOUND("DPN.DataProcess.CalendarNotFound", "根据id或者编码:{0} 找不到公共日历", ErrorLevel.LOGIC),

    // 根据id找不到标签
    TAG_NOT_FOUND("DPN.DataProcess.TagNotFound", "根据id或者编码:{0} 找不到标签", ErrorLevel.LOGIC),
}
```

提取结果：

```properties
DPN.DataProcess.CalendarNotFound = 根据id或者编码:{0} 找不到公共日历
DPN.DataProcess.TagNotFound = 根据id或者编码:{0} 找不到标签
```

## 项目结构

```
src/main/kotlin/org/jetbrains/plugins/template/
├── i18n/
│   ├── I18nEntry.kt           # I18N 条目数据类
│   └── I18nExtractor.kt       # 文案提取器
├── actions/
│   ├── ExtractI18nAction.kt      # 提取文案 Action
│   ├── ManualI18nEntryAction.kt  # 手动录入 Action
│   └── BatchExtractI18nAction.kt # 批量提取 Action
├── intentions/
│   └── ExtractI18nIntention.kt   # 灯泡菜单 Intention
├── annotator/
│   └── I18nPatternAnnotator.kt   # 代码标注器
├── services/
│   ├── I18nSubmitService.kt      # 文案提交服务
│   └── CodePlatformService.kt    # Code 平台服务
├── settings/
│   └── KiwiSettings.kt           # 插件设置
├── ui/
│   ├── I18nEntryDialog.kt        # 文案录入对话框
│   ├── BatchI18nDialog.kt        # 批量选择对话框
│   ├── KiwiSettingsConfigurable.kt # 设置页面
│   └── KiwiToolWindowPanel.kt    # 工具窗口面板
└── toolWindow/
    └── KiwiToolWindowFactory.kt  # 工具窗口工厂
```

## 开发

### 环境要求

- JDK 21+
- Gradle 9.2+
- IntelliJ IDEA 2024.2+（Build 242+）

### 构建

```bash
./gradlew build
```

### 运行插件

```bash
./gradlew runIde
```

### 测试

```bash
./gradlew test
```

## License

MIT License

[gh:build]: https://github.com/JetBrains/intellij-platform-plugin-template/actions?query=workflow%3ABuild
