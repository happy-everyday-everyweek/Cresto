package com.nevoit.cresto.data.utils

import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    val quantity: Int,
    val items: List<EventItem>
)

@Serializable
data class EventItem(
    val title: String,
    val date: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val reminderMode: String? = null,
    val reminderOffsetMinutes: Int? = null,
    val reminderDayOffset: Int? = null,
    val reminderTime: String? = null,
    val subTasks: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    /** AI 可选读取的分组名（开关关闭时为空）。 */
    val groupName: String? = null,
    /** AI 可选判断的标记（颜色索引 0-7，0 表示无标记）。 */
    val flag: Int? = null
)
