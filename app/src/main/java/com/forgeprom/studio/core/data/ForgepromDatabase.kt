package com.forgeprom.studio.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * قاعدة البيانات المحلية — Room، الإصدار 1.
 * الترحيل الآمن بين الإصدارات مطلوب قبل أي تغيير في المخطط (R2/R13).
 */
@Database(
    entities = [
        ProjectEntity::class,
        PromptEntity::class,
        AnalysisEntity::class,
        ActivityEntity::class,
        EditorFileEntity::class,
        ImageAnalysisEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ForgepromDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun promptDao(): PromptDao
    abstract fun analysisDao(): AnalysisDao
    abstract fun activityDao(): ActivityDao
    abstract fun editorFileDao(): EditorFileDao
    abstract fun imageAnalysisDao(): ImageAnalysisDao

    companion object {
        fun build(context: Context): ForgepromDatabase =
            Room.databaseBuilder(context, ForgepromDatabase::class.java, "forgeprom.db")
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}
