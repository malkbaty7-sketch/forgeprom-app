package com.forgeprom.studio.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** R2 — المشاريع: أنواع خمسة، تثبيت، أرشفة */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val kind: String = "prompt", // app | feature | edit | analysis | prompt
    val tags: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** R3 — الموجهات ونسخها (نسخة أولية عبر v2 إضافية) */
@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val content: String,
    val language: String = "ar",
    val qualityScore: Int = 0,
    val goal: String = "",
    val tech: String = "",
    val isImageInspired: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** R6 — التحليلات المحفوظة (مسودة معتمدة) */
@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val summary: String,
    val intent: String,
    val goalsJson: String = "[]",
    val featuresJson: String = "[]",
    val requirementsJson: String = "[]",
    val constraintsJson: String = "[]",
    val assumptionsJson: String = "[]",
    val questionsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

/** سجل النشاطات للوحة الرئيسية والإحصائيات */
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val detail: String,
    val at: Long = System.currentTimeMillis()
)

/** R3 — ملفات المحرر داخل المشروع */
@Entity(tableName = "editor_files")
data class EditorFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val fileName: String,
    val language: String = "plaintext",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** R8 — تحليلات الصور المحفوظة */
@Entity(tableName = "image_analyses")
data class ImageAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val screenType: String,
    val componentsJson: String = "[]",
    val colorsJson: String = "{}",
    val layout: String = "",
    val statesJson: String = "[]",
    val assumptionsJson: String = "[]",
    val confidence: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)
