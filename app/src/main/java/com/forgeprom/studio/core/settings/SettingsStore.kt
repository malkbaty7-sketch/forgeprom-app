package com.forgeprom.studio.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** وضع المظهر */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** لغة البرومبت الناتج */
enum class PromptLanguage { AR, EN, DUAL }

/** إعدادات التطبيق — قيم افتراضية صادقة (محلي + موافقة سحابة مرفوضة) */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val promptLanguage: PromptLanguage = PromptLanguage.AR,
    val cloudConsent: Boolean = false,
    val engineGeneral: String = "local.rules",
    val engineCode: String = "local.rules",
    val engineAnalysis: String = "local.rules"
)

private val Context.dataStore by preferencesDataStore(name = "forgeprom_settings")

/**
 * R1/R5/R11 — مخزن الإعدادات (DataStore).
 * الافتراضي: المحرك المحلي لكل المهام، وموافقة السحابة مرفوضة.
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANG = stringPreferencesKey("prompt_language")
        val CONSENT = booleanPreferencesKey("cloud_consent")
        val ENG_GENERAL = stringPreferencesKey("engine_general")
        val ENG_CODE = stringPreferencesKey("engine_code")
        val ENG_ANALYSIS = stringPreferencesKey("engine_analysis")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            promptLanguage = p[Keys.LANG]?.let { runCatching { PromptLanguage.valueOf(it) }.getOrNull() }
                ?: PromptLanguage.AR,
            cloudConsent = p[Keys.CONSENT] ?: false,
            engineGeneral = p[Keys.ENG_GENERAL] ?: "local.rules",
            engineCode = p[Keys.ENG_CODE] ?: "local.rules",
            engineAnalysis = p[Keys.ENG_ANALYSIS] ?: "local.rules"
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setPromptLanguage(language: PromptLanguage) {
        context.dataStore.edit { it[Keys.LANG] = language.name }
    }

    /** موافقة السحابة — مرفوضة افتراضياً ولا تُمنح إلا صراحةً */
    suspend fun setCloudConsent(granted: Boolean) {
        context.dataStore.edit { it[Keys.CONSENT] = granted }
    }

    /** المحرك الافتراضي لمهمة: general | code | analysis */
    suspend fun setEngineForTask(task: String, engineId: String) {
        context.dataStore.edit { p ->
            when (task) {
                "code" -> p[Keys.ENG_CODE] = engineId
                "analysis" -> p[Keys.ENG_ANALYSIS] = engineId
                else -> p[Keys.ENG_GENERAL] = engineId
            }
        }
    }
}
