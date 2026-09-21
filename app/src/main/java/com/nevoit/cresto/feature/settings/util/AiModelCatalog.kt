package com.nevoit.cresto.feature.settings.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.net.HttpURLConnection
import java.net.URL

/** 常见的 OpenAI 兼容服务端点预设，方便一键填入 API 地址。 */
data class AiEndpointPreset(
    val name: String,
    val baseUrl: String
)

val AI_ENDPOINT_PRESETS = listOf(
    AiEndpointPreset("智谱 BigModel", "https://open.bigmodel.cn/api/paas/v4"),
    AiEndpointPreset("DeepSeek", "https://api.deepseek.com"),
    AiEndpointPreset("OpenAI", "https://api.openai.com"),
    AiEndpointPreset("DashScope", "https://dashscope.aliyuncs.com/compatible-mode/v1")
)

/**
 * 读取 OpenAI 兼容服务（/models）下的可用模型列表。
 *
 * 用户填写的地址可能已经是 chat/completions 接口，因此这里先把后缀裁掉再拼模型列表地址。
 */
object AiModelCatalog {

    private const val DEFAULT_MODELS_ENDPOINT = "https://open.bigmodel.cn/api/paas/v4/models"

    private val json = Json { ignoreUnknownKeys = true }

    fun resolveModelsEndpoint(rawUrl: String): String {
        val normalized = rawUrl.trim()
            .trimEnd('/')
            .removeSuffix("/chat/completions")
            .trimEnd('/')

        return when {
            normalized.isEmpty() -> DEFAULT_MODELS_ENDPOINT
            normalized.endsWith("/models") -> normalized
            normalized.endsWith("/v1") || normalized.endsWith("/v4") -> "$normalized/models"
            else -> "$normalized/v1/models"
        }
    }

    suspend fun fetchModels(apiUrl: String, apiKey: String): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalStateException("请先填写 API Key")
        }

        val endpoint = resolveModelsEndpoint(apiUrl)
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }

        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code !in 200..299) {
                throw IllegalStateException("获取模型失败($code)：${text.take(200)}")
            }

            parseModelIds(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseModelIds(rawText: String): List<String> {
        val element = try {
            json.parseToJsonElement(rawText)
        } catch (_: Exception) {
            return emptyList()
        }

        val entries = when (element) {
            is JsonArray -> element.toList()
            is JsonObject -> {
                val list = element["data"] ?: element["models"]
                if (list is JsonArray) list.toList() else emptyList()
            }

            else -> emptyList()
        }

        return entries.mapNotNull { entry ->
            when (entry) {
                is JsonPrimitive -> entry.contentOrNull
                is JsonObject -> listOf("id", "name", "model")
                    .firstNotNullOfOrNull { key -> (entry[key] as? JsonPrimitive)?.contentOrNull }
                else -> null
            }?.trim()?.takeIf { it.isNotEmpty() }
        }.distinct().sorted()
    }
}
