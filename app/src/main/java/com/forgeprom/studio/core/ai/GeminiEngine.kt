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
 * R5/R10 — محرك Gemini السحابي (اختياري).
 * لا يُستخدم إلا بعد بوابة الموافقة الصريحة (AiEngineManager.cloudGate).
 * المفتاح من ApiKeyStore المشفر ولا يظهر في أي سجل.
 */
class GeminiEngine(private val apiKeyStore: ApiKeyStore) : AiEngine {
    override val id = "cloud.gemini"
    override val label = "Gemini سحابي (اختياري، بموافقة)"
    override val isLocal = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    override suspend fun status(settings: AppSettings): EngineStatus {
        val hasKey = apiKeyStore.geminiKey().isNotBlank()
        return EngineStatus(
            id = id,
            label = label,
            isReady = hasKey,
            statusLabel = when {
                !hasKey -> "غير مهيأ — أضف مفتاح Gemini في الإعدادات"
                !settings.cloudConsent -> "مفتاح موجود لكن موافقة السحابة غير ممنوحة"
                else -> "جاهز"
            }
        )
    }

    override suspend fun generate(request: GenerationRequest): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val key = apiKeyStore.geminiKey()
                require(key.isNotBlank()) { "مفتاح Gemini غير مضبوط" }
                val promptText = buildSystemPrompt(request)
                val body = JSONObject()
                    .put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", promptText)
                        ))
                    ))
                val response = execute(body, key)
                extractText(response)
            }
        }

    override suspend fun analyzeImage(bytes: ByteArray, mime: String, note: String): Result<ImageAnalysisResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val key = apiKeyStore.geminiKey()
                require(key.isNotBlank()) { "مفتاح Gemini غير مضبوط" }
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val parts = JSONArray()
                    .put(JSONObject().put("inline_data", JSONObject().put("mime_type", mime).put("data", base64)))
                    .put(JSONObject().put("text", imageInstruction(note)))
                val body = JSONObject().put("contents", JSONArray().put(
                    JSONObject().put("parts", parts)
                ))
                val response = execute(body, key)
                parseImageResponse(extractText(response))
            }
        }

    private fun execute(body: JSONObject, key: String): JSONObject {
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$key")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        client.newCall(request).execute().use { resp ->
            val text = resp.body?.string() ?: ""
            require(resp.isSuccessful) { "فشل Gemini (HTTP " + resp.code + ")" }
            return JSONObject(text)
        }
    }

    private fun extractText(response: JSONObject): String {
        val candidates = response.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val parts = candidates.getJSONObject(0)
            .optJSONObject("content")?.optJSONArray("parts") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until parts.length()) sb.append(parts.getJSONObject(i).optString("text", ""))
        return sb.toString()
    }

    private fun imageInstruction(note: String): String {
        return "Analyze this mobile app UI screenshot. " +
            "The image is for INSPIRATION ONLY — never claim to recover source code. " +
            "Return ONLY valid JSON with these keys: screenType, components (array), " +
            "colors (object mapping role->hex), layout, states (array), assumptions (array), confidence (0-1). " +
            "Describe approximate layout/colors; do not claim exact measurements. " +
            (if (note.isNotBlank()) "User note: " + note else "")
    }

    private fun parseImageResponse(text: String): ImageAnalysisResult {
        val clean = text.substringAfter('{').substringBeforeLast('}')
        val json = runCatching { JSONObject("{" + clean + "}") }.getOrElse {
            return ImageAnalysisResult(
                screenType = "غير محدد", components = emptyList(), colors = emptyMap(),
                layout = "غير محدد", states = emptyList(), assumptions = emptyList(),
                confidence = 0f, rawText = text
            )
        }
        val components = mutableListOf<String>()
        val compArr = json.optJSONArray("components")
        if (compArr != null) for (i in 0 until compArr.length()) components.add(compArr.optString(i, ""))
        val states = mutableListOf<String>()
        val stArr = json.optJSONArray("states")
        if (stArr != null) for (i in 0 until stArr.length()) states.add(stArr.optString(i, ""))
        val assumptions = mutableListOf<String>()
        val asArr = json.optJSONArray("assumptions")
        if (asArr != null) for (i in 0 until asArr.length()) assumptions.add(asArr.optString(i, ""))
        val colors = mutableMapOf<String, String>()
        val colObj = json.optJSONObject("colors")
        if (colObj != null) {
            val keys = colObj.keys()
            while (keys.hasNext()) { val k = keys.next(); colors[k] = colObj.optString(k, "") }
        }
        return ImageAnalysisResult(
            screenType = json.optString("screenType", "غير محدد"),
            components = components.filter { it.isNotBlank() },
            colors = colors,
            layout = json.optString("layout", "غير محدد"),
            states = states.filter { it.isNotBlank() },
            assumptions = assumptions.filter { it.isNotBlank() },
            confidence = json.optDouble("confidence", 0.5).toFloat().coerceIn(0f, 1f),
            rawText = text
        )
    }

    companion object {
        private const val MODEL = "gemini-2.0-flash"

        fun buildSystemPrompt(request: GenerationRequest): String {
            return "Generate a complete professional coding prompt (the instructions to give an AI code-generation platform), " +
                "NOT the app code itself. " +
                (if (request.promptLanguage == com.forgeprom.studio.core.settings.PromptLanguage.EN)
                    "Write the prompt in technical English. "
                else "Write the prompt in Arabic with English section keywords. ") +
                "User request: " + request.instruction + "\n" +
                "Goal type: " + request.goal + "\n" +
                "Target tech: " + request.tech + "\n" +
                (if (request.projectContext.isNotBlank()) "Existing project context: " + request.projectContext + "\n" else "") +
                "Must include sections: ROLE, OBJECTIVE, CONTEXT, REQUEST TYPE, TECH STACK, UI SPECIFICATION, STATES, INTERACTIONS, ACCESSIBILITY, CODE QUALITY CONSTRAINTS (anti-bloat: no unused dependencies, no placeholder screens, no duplicated logic), ACCEPTANCE CRITERIA, OUTPUT FORMAT. " +
                "State assumptions explicitly; never guess silently."
        }
    }
}
