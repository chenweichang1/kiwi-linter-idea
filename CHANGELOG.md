<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Kiwi-linter Changelog

## [Unreleased]

## [2.4.0] - 2025-11-25

### Added

- 批量/手动录入 I18N 文案并提交到远程仓库
- 提交时仅更新本次提交的 key（未提交的历史 key 保持原样）

### Fixed

- properties 合并逻辑：避免无意义的“删了又加一行”（尽量减少 diff 噪音）

### Changed

- 插件改名为 **Kiwi-linter**
- 使用 Java 21（IntelliJ IDEA 2024.2+）

[Unreleased]: https://gitlab.alibaba-inc.com/nel/kiwi-linter-idea/compare/v2.4.0...HEAD
[2.4.0]: https://gitlab.alibaba-inc.com/nel/kiwi-linter-idea/commits/v2.4.0
