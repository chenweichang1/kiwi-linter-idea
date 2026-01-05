package com.github.chenweichang1.kiwilinteridea.i18n

/**
 * 表示一个 I18N 文案条目
 * @param key 国际化 key，如 "DPN.DataProcess.CalendarNotFound"
 * @param value 中文文案，如 "根据id或者编码:{0} 找不到公共日历"
 * @param sourceLocation 源代码位置信息（可选）
 */
data class I18nEntry(
    val key: String,
    val value: String,
    val sourceLocation: String? = null
) {
    /**
     * 转换为 properties 文件格式
     */
    fun toPropertiesLine(): String {
        return "$key = $value"
    }
    
    companion object {
        /**
         * 从 properties 行解析
         */
        fun fromPropertiesLine(line: String): I18nEntry? {
            val parts = line.split("=", limit = 2)
            if (parts.size != 2) return null
            return I18nEntry(
                key = parts[0].trim(),
                value = parts[1].trim()
            )
        }
    }
}

