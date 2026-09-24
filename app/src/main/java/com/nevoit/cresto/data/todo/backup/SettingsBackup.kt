package com.nevoit.cresto.data.todo.backup

import com.tencent.mmkv.MMKV

/**
 * 把应用设置整理成可以写进备份文件的键值列表。
 *
 * 只收录下面列出的键：外观、排序、提醒与 AI 配置。彩蛋状态、首次运行标记、上次检查更新时间等
 * 运行期状态不导出，避免把设备相关的临时状态带到别的设备上。
 */
object SettingsBackup {

    private enum class ValueType { STRING, INT, BOOL }

    private val exportedKeys: List<Pair<String, ValueType>> = listOf(
        "color_mode" to ValueType.INT,
        "custom_primary_color_enabled" to ValueType.BOOL,
        "use_dynamic_color_enabled" to ValueType.BOOL,
        "lite_mode_enabled" to ValueType.BOOL,
        "liquid_glass_enabled" to ValueType.BOOL,
        "theme_primary_color" to ValueType.INT,
        "sort_option" to ValueType.INT,
        "sort_order" to ValueType.INT,
        "due_today_marker_enabled" to ValueType.BOOL,
        "overdue_marker_enabled" to ValueType.BOOL,
        "completion_sound_enabled" to ValueType.BOOL,
        "extract_screen_quick_tile_enabled" to ValueType.BOOL,
        "auto_add_to_system_calendar" to ValueType.BOOL,
        "check_updates_on_startup" to ValueType.BOOL,
        "app_icon" to ValueType.STRING,
        "ai_api_url" to ValueType.STRING,
        "ai_api_key" to ValueType.STRING,
        "ai_text_model" to ValueType.STRING,
        "ai_multimodal_model" to ValueType.STRING,
        "ai_extract_group" to ValueType.BOOL,
        "ai_extract_flag" to ValueType.BOOL
    )

    fun capture(): List<SettingBackupEntry> {
        val mmkv = MMKV.defaultMMKV() ?: return emptyList()
        return exportedKeys.mapNotNull { (key, type) ->
            when (type) {
                ValueType.STRING -> mmkv.decodeString(key)?.let { value ->
                    SettingBackupEntry(key, "string", value)
                }

                ValueType.INT -> SettingBackupEntry(key, "int", mmkv.decodeInt(key).toString())
                ValueType.BOOL -> SettingBackupEntry(key, "bool", mmkv.decodeBool(key).toString())
            }
        }
    }
}
