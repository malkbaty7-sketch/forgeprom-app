package com.forgeprom.studio.core.ai

import com.forgeprom.studio.core.security.ApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * R5 — محرك متوافق مع OpenAI (متعدد المزودين: OpenAI، Groq، Together، إلخ)
 * عبر baseUrl وmodel قابلين للتهيئة. يستخدم فقط بعد بوابة الموافقة.
 */
class OpenAiCompatEngine(private val apiKeyStore: ApiKeyStore) : AiEngine {
    override val id = "cloud.openai_compat"
    override val label = "متوافق OpenAI (سحابي اختياري)"
    override val isLocal = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    override suspend fun status(settings: AppSettings): EngineStatus {
        val hasKey = apiKeyStore.openAiKey().isNotBlank()
        return EngineStatus(
            id = id,
            label = label,
            isReady = hasKey,
            statusLabel = when {
                !hasKey -> "غير مهيأ — أضف المفتاح والعنوان في الإعدادات"
                !settings.cloudConsent -> "مفتاح موجود لكن موافقة السحابة غير ممنوحة"
                else -> "جاهز — " + apiKeyStore.openAiModel() + " @ " + apiKeyStore.openAiBaseUrl()
            }
        )
    }

    override suspend fun generate(request: GenerationRequest): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val key = apiKeyStore.openAiKey()
                require(key.isNotBlank()) { "مفتاح API غير مضبوط" }
                val messages = JSONArray()
                    .put(JSONObject().put("role", "system").put(
                        "content",
                        "You generate professional coding PROMPTS (not code) for AI code platforms. " +
                            "Always include: role, objective, context, request type, tech stack, UI spec, states, interactions, accessibility, anti-bloat code quality constraints, acceptance criteria, output format. State assumptions explicitly."
                    ))
                    .put(JSONObject().put("role", "user").put(
                        "content",
                        "Goal: " + request.goal + "\nTech: " + request.tech +
                            (if (request.projectContext.isNotBlank()) "\nProject context: " + request.projectContext else "") +
                            "\nRequest: " + request.instruction
                    ))
                val body = JSONObject()
                    .put("model", apiKeyStore.openAiModel())
                    .put("messages", messages)
                val url = apiKeyStore.openAiBaseUrl().trimEnd('/') + "/chat/completions"
                val httpRequest = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + key)
                    .post(body.toString().toRequestBody(jsonMedia))
                    .build()
                client.newCall(httpRequest).execute().use { resp ->
                    val text = resp.body?.string() ?: ""
                    require(resp.isSuccessful) { "فشل الطلب (HTTP " + resp.code + ")" }
                    JSONObject(text)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                }
            }
        }

    override suspend fun analyzeImage(bytes: ByteArray, mime: String, note: String): Result<ImageAnalysisResult> =
        Result.failure(UnsupportedOperationException(
            "تحليل الصور عبر المحرك المتوافق OpenAI غير مدعوم في هذه النسخة — استخدم Gemini."
        ))
}
