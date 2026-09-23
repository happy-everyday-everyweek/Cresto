package com.nevoit.cresto.feature.settings.util

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AISettingsViewModel : ViewModel() {

    val apiUrl = mutableStateOf(SettingsManager.aiApiUrl)
    val apiKey = mutableStateOf(SettingsManager.aiApiKey)
    val textModel = mutableStateOf(SettingsManager.aiTextModel)
    val multimodalModel = mutableStateOf(SettingsManager.aiMultimodalModel)
    val lastSavedAt = mutableLongStateOf(0L)

    // 自动获取模型列表相关的状态
    val availableModels = mutableStateOf<List<String>>(emptyList())
    val isFetchingModels = mutableStateOf(false)
    val modelFetchError = mutableStateOf<String?>(null)
    val modelTarget = mutableStateOf(AiModelTarget.TEXT)

    // 创建待办时可选的额外读取项
    val extractGroupWhenCreating = mutableStateOf(SettingsManager.aiExtractGroupWhenCreating)
    val extractFlagWhenCreating = mutableStateOf(SettingsManager.aiExtractFlagWhenCreating)

    fun onApiUrlChanged(value: String) {
        apiUrl.value = value
        saveSettingsIfChanged()
    }

    fun onApiKeyChanged(value: String) {
        apiKey.value = value
        saveSettingsIfChanged()
    }

    fun onTextModelChanged(value: String) {
        textModel.value = value
        saveSettingsIfChanged()
    }

    fun onMultimodalModelChanged(value: String) {
        multimodalModel.value = value
        saveSettingsIfChanged()
    }

    fun restoreDefaults() {
        SettingsManager.resetAiSettingsToDefaults()
        apiUrl.value = SettingsManager.aiApiUrl
        apiKey.value = SettingsManager.aiApiKey
        textModel.value = SettingsManager.aiTextModel
        multimodalModel.value = SettingsManager.aiMultimodalModel
        extractGroupWhenCreating.value = SettingsManager.aiExtractGroupWhenCreating
        extractFlagWhenCreating.value = SettingsManager.aiExtractFlagWhenCreating
        lastSavedAt.longValue = System.currentTimeMillis()
    }

    fun saveSettings() {
        val newApiUrl = apiUrl.value.trim()
        val newApiKey = apiKey.value.trim()
        val newTextModel = textModel.value.trim()
        val newMultimodalModel = multimodalModel.value.trim()

        SettingsManager.aiApiUrl = newApiUrl
        SettingsManager.aiApiKey = newApiKey
        SettingsManager.aiTextModel = newTextModel
        SettingsManager.aiMultimodalModel = newMultimodalModel
        lastSavedAt.longValue = System.currentTimeMillis()
    }

    private fun saveSettingsIfChanged() {
        val newApiUrl = apiUrl.value.trim()
        val newApiKey = apiKey.value.trim()
        val newTextModel = textModel.value.trim()
        val newMultimodalModel = multimodalModel.value.trim()

        if (
            newApiUrl == SettingsManager.aiApiUrl &&
            newApiKey == SettingsManager.aiApiKey &&
            newTextModel == SettingsManager.aiTextModel &&
            newMultimodalModel == SettingsManager.aiMultimodalModel
        ) {
            return
        }

        saveSettings()
    }

    fun onApiPresetSelected(baseUrl: String) {
        onApiUrlChanged(baseUrl)
    }

    fun onModelTargetChanged(target: AiModelTarget) {
        modelTarget.value = target
    }

    fun onExtractGroupChanged(enabled: Boolean) {
        extractGroupWhenCreating.value = enabled
        SettingsManager.aiExtractGroupWhenCreating = enabled
        lastSavedAt.longValue = System.currentTimeMillis()
    }

    fun onExtractFlagChanged(enabled: Boolean) {
        extractFlagWhenCreating.value = enabled
        SettingsManager.aiExtractFlagWhenCreating = enabled
        lastSavedAt.longValue = System.currentTimeMillis()
    }

    /** 选中模型列表里的某一项，写入当前选定的目标字段。 */
    fun onModelSelected(model: String) {
        when (modelTarget.value) {
            AiModelTarget.TEXT -> onTextModelChanged(model)
            AiModelTarget.MULTIMODAL -> onMultimodalModelChanged(model)
        }
    }

    /** 从当前 API 地址拉取可用模型列表（OpenAI 兼容的 /models 接口）。 */
    fun fetchModels() {
        if (isFetchingModels.value) return
        isFetchingModels.value = true
        modelFetchError.value = null
        viewModelScope.launch {
            try {
                val models = AiModelCatalog.fetchModels(
                    apiUrl = apiUrl.value.trim(),
                    apiKey = apiKey.value.trim()
                )
                availableModels.value = models
                if (models.isEmpty()) {
                    modelFetchError.value = "接口没有返回任何模型"
                }
            } catch (e: Exception) {
                availableModels.value = emptyList()
                modelFetchError.value = e.message ?: "获取模型失败"
            } finally {
                isFetchingModels.value = false
            }
        }
    }
}

/** 从模型列表选中的模型要写入哪个字段。 */
enum class AiModelTarget { TEXT, MULTIMODAL }

