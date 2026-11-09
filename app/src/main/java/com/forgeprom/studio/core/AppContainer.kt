package com.forgeprom.studio.core

import android.content.Context
import com.forgeprom.studio.core.ai.AiEngineManager
import com.forgeprom.studio.core.ai.GeminiEngine
import com.forgeprom.studio.core.ai.LocalRulesEngine
import com.forgeprom.studio.core.ai.OpenAiCompatEngine
import com.forgeprom.studio.core.data.ForgepromDatabase
import com.forgeprom.studio.core.security.ApiKeyStore
import com.forgeprom.studio.core.settings.SettingsStore
import com.forgeprom.studio.domain.AnalysisDraft
import com.forgeprom.studio.domain.CritiqueData
import com.forgeprom.studio.domain.QualityReportData
import androidx.room.Room

/** جلسة التوليد الحالية بين الشاشات (analysis → result) */
class SessionHolder {
    var draft: AnalysisDraft? = null
    var promptText: String = ""
    var report: QualityReportData? = null
    var critique: CritiqueData? = null
    var imageAnalysis: com.forgeprom.studio.core.ai.ImageAnalysisResult? = null
    var sourceInstruction: String = ""
    var goal: String = ""
    var tech: String = ""
    var isImageInspired: Boolean = false

    fun clear() {
        draft = null; promptText = ""; report = null; critique = null
        imageAnalysis = null; sourceInstruction = ""; goal = ""; tech = ""
        isImageInspired = false
    }
}

/**
 * R4 — الحاوية المركزية: حقن يدوي لكل الطبقات.
 */
class AppContainer(context: Context) {
    val db: ForgepromDatabase = Room.databaseBuilder(
        context, ForgepromDatabase::class.java, "forgeprom.db"
    ).build()

    val settings: SettingsStore = SettingsStore(context)
    val apiKeys: ApiKeyStore = ApiKeyStore(context)
    val engines: AiEngineManager = AiEngineManager(
        settingsStore = settings,
        apiKeyStore = apiKeys,
        localEngine = LocalRulesEngine(),
        geminiEngine = GeminiEngine(apiKeys),
        openAiEngine = OpenAiCompatEngine(apiKeys)
    )
    val session: SessionHolder = SessionHolder()

    /** تسجيل نشاط دون تعطيل أي مسار عند الفشل */
    fun logActivity(type: String, detail: String) {
        runCatching {
            kotlinx.coroutines.runBlocking {
                db.activityDao().insert(
                    com.forgeprom.studio.core.data.ActivityEntity(type = type, detail = detail)
                )
            }
        }
    }
}
