package com.forgeprom.studio.core.ai

import com.forgeprom.studio.core.security.ApiKeyStore
import com.forgeprom.studio.core.settings.AppSettings
import com.forgeprom.studio.core.settings.SettingsStore
import com.forgeprom.studio.domain.IdeaAnalyzer
import com.forgeprom.studio.domain.PromptBuilder
import com.forgeprom.studio.core.settings.PromptLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * R4/R5 — الواجهة الموحدة للنماذج المحلية والسحابية.
 * جميع عمليات المحرك خارج خيط الواجهة (Dispatchers.IO/Default) — لا تجميد أبداً.
 */
data class GenerationRequest(
    val instruction: String,
    val goal: String,
    val tech: String,
    val projectContext: String = "",
    val promptLanguage: PromptLanguage = PromptLanguage.AR,
    val isImageInspired: Boolean = false
)

data class ImageAnalysisResult(
    val screenType: String,
    val components: List<String>,
    val colors: Map<String, String>,
    val layout: String,
    val states: List<String>,
    val assumptions: List<String>,
    val confidence: Float,
    val rawText: String
)

data class EngineStatus(
    val id: String,
    val label: String,
    val isReady: Boolean,
    val statusLabel: String
)

interface AiEngine {
    val id: String
    val label: String
    val isLocal: Boolean
    suspend fun status(settings: AppSettings): EngineStatus
    suspend fun generate(request: GenerationRequest): Result<String>
    suspend fun analyzeImage(bytes: ByteArray, mime: String, note: String): Result<ImageAnalysisResult>
}

/**
 * المحرك المحلي — يعمل فعلياً دون إنترنت عبر محلل قواعدي عربي.
 * ملاحظة صادقة: النموذج العصبي المحلي (على الجهاز) قيد التكامل لاحقاً؛
 * المحرك الحالي قاعدي (rules-based) وليس نموذجاً عصبياً.
 */
class LocalRulesEngine : AiEngine {
    override val id = "local.rules"
    override val label = "محرك محلي (قاعدي، دون إنترنت)"
    override val isLocal = true

    override suspend fun status(settings: AppSettings): EngineStatus = EngineStatus(
        id = id,
        label = label,
        isReady = true,
        statusLabel = "جاهز — توليد قاعدي يعمل دون إنترنت. النموذج العصبي المحلي قيد التكامل لاحقاً."
    )

    override suspend fun generate(request: GenerationRequest): Result<String> =
        withContext(Dispatchers.Default) {
            runCatching {
                val draft = IdeaAnalyzer.analyze(
                    idea = request.instruction + if (request.projectContext.isBlank()) "" else "\n" + request.projectContext,
                    goal = request.goal,
                    tech = request.tech
                )
                PromptBuilder.build(draft, request.tech, request.promptLanguage, request.isImageInspired)
            }
        }

    override suspend fun analyzeImage(bytes: ByteArray, mime: String, note: String): Result<ImageAnalysisResult> =
        Result.failure(UnsupportedOperationException(
            "تحليل الصور غير متاح في المحرك المحلي — يحتاج نموذج رؤية سحابياً بموافقتك، أو نموذجاً متعدد الوسائط محلياً قيد التكامل لاحقاً."
        ))
}

/**
 * R5 — مدير المحركات: اختيار افتراضي لكل مهمة + بوابة موافقة سحابية.
 */
class AiEngineManager(
    private val settingsStore: SettingsStore,
    private val apiKeyStore: ApiKeyStore,
    private val localEngine: LocalRulesEngine = LocalRulesEngine(),
    private val geminiEngine: GeminiEngine = GeminiEngine(apiKeyStore),
    private val openAiEngine: OpenAiCompatEngine = OpenAiCompatEngine(apiKeyStore)
) {
    fun allEngines(): List<AiEngine> = listOf(localEngine, geminiEngine, openAiEngine)

    fun engineById(id: String): AiEngine = when (id) {
        geminiEngine.id -> geminiEngine
        openAiEngine.id -> openAiEngine
        else -> localEngine
    }

    suspend fun defaultForTask(task: String): AiEngine {
        val s = settingsStore.settings.first()
        val id = when (task) {
            "code" -> s.engineCode
            "analysis" -> s.engineAnalysis
            else -> s.engineGeneral
        }
        return engineById(id)
    }

    /** بوابة الموافقة: لا إرسال سحابي دون موافقة صريحة — سياسة الخصوصية. */
    suspend fun cloudGate(): Result<AppSettings> {
        val s = settingsStore.settings.first()
        return if (!s.cloudConsent) {
            Result.failure(SecurityException("موافقة السحابة غير ممنوحة — لا يُرسل أي محتوى دون موافقة صريحة"))
        } else Result.success(s)
    }
}
