package com.forgeprom.studio.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * R11 — تخزين مفاتيح API مشفراً (EncryptedSharedPreferences).
 * المفاتيح لا تُعرض مجدداً في الواجهة ولا تُكتب في أي سجل.
 * عند تعذر التهيئة (أجهزة قديمة/مفاتيح تالفة) نسقط إلى تخزين مؤقت
 * في الذاكرة مع الإعلان عن ذلك — لا كذب على المستخدم.
 */
class ApiKeyStore(context: Context) {

    companion object {
        const val DEFAULT_OPENAI_URL = "https://api.openai.com/v1"
        const val DEFAULT_OPENAI_MODEL = "gpt-4o-mini"
        private const val PREFS = "forgeprom_secure_keys"
    }

    private val prefs: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, PREFS, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse { InMemoryPrefs() }

    // ---- Gemini ----
    fun geminiKey(): String = prefs.getString("gemini_key", "") ?: ""
    fun setGeminiKey(key: String) { prefs.edit().putString("gemini_key", key).apply() }

    // ---- OpenAI-compatible ----
    fun openAiKey(): String = prefs.getString("openai_key", "") ?: ""
    fun setOpenAiKey(key: String) { prefs.edit().putString("openai_key", key).apply() }

    fun openAiBaseUrl(): String = prefs.getString("openai_base_url", DEFAULT_OPENAI_URL) ?: DEFAULT_OPENAI_URL
    fun setOpenAiBaseUrl(url: String) { prefs.edit().putString("openai_base_url", url).apply() }

    fun openAiModel(): String = prefs.getString("openai_model", DEFAULT_OPENAI_MODEL) ?: DEFAULT_OPENAI_MODEL
    fun setOpenAiModel(model: String) { prefs.edit().putString("openai_model", model).apply() }

    /** هل التخزين مشفر فعلاً؟ (للعرض الصادق في التشخيص) */
    val isEncrypted: Boolean get() = prefs !is InMemoryPrefs

    /** بديل مؤقت في الذاكرة فقط عند فشل التشفير — يفقد المفاتيح عند إغلاق التطبيق */
    private class InMemoryPrefs : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()
        private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()
        private val editor = object : SharedPreferences.Editor {
            override fun putString(key: String, value: String?) { map[key] = value; this }
            override fun putStringSet(key: String, values: MutableSet<String>?) { map[key] = values; this }
            override fun putInt(key: String, value: Int) { map[key] = value; this }
            override fun putLong(key: String, value: Long) { map[key] = value; this }
            override fun putFloat(key: String, value: Float) { map[key] = value; this }
            override fun putBoolean(key: String, value: Boolean) { map[key] = value; this }
            override fun remove(key: String) { map.remove(key); this }
            override fun clear() { map.clear(); this }
            override fun apply() {}
            override fun commit(): Boolean = true
        }
        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String, defValue: MutableSet<String>?): MutableSet<String>? =
            map[key] as? MutableSet<String> ?: defValue
        override fun getInt(key: String, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = editor
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.add(l) }
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.remove(l) }
    }
}
