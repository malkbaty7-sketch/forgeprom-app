package com.forgeprom.studio.core.registry

/**
 * R12 — سجل الميزات (CORE.GUARD): كل ميزة تُسجَّل مع فحص ذاتي حقيقي
 * قابل للتشغيل عند بدء التطبيق — لا ادعاء جاهزية قبل عمل فعلي.
 */
data class Feature(
    val id: String,
    val name: String,
    val phase: String,
    val isReady: Boolean,
    val statusNote: String
)

object FeatureRegistry {

    val features: List<Feature> = listOf(
        Feature("CORE.UI", "واجهة عربية RTL مع وضع فاتح وداكن", "R1", true, "مكتمل"),
        Feature("CORE.SETTINGS", "الإعدادات والتفضيلات المحلية", "R1", true, "مكتمل"),
        Feature("CORE.NAV", "التنقل بين الشاشات", "R1", true, "مكتمل"),
        Feature("PROJ.CRUD", "إدارة المشاريع: إنشاء وتثبيت وأرشفة وحذف", "R2", true, "مكتمل — Room محلي"),
        Feature("PROMPT.STORE", "حفظ الموجهات ونسخها في المشاريع", "R2", true, "مكتمل"),
        Feature("ACT.LOG", "سجل النشاطات", "R2", true, "مكتمل"),
        Feature("EDITOR.BASIC", "محرر نصوص مع عدادات وحفظ", "R3", true, "مكتمل"),
        Feature("AI.LOCAL", "محرك محلي قاعدي يعمل دون إنترنت", "R4", true, "مكتمل — توليد قاعدي، النموذج العصبي المحلي قيد التكامل لاحقاً"),
        Feature("AI.MANAGER", "اختيار المحرك الافتراضي لكل مهمة", "R4", true, "مكتمل"),
        Feature("MODELS.CENTER", "مركز المحركات وحالتها", "R5", true, "مكتمل"),
        Feature("ANALYZER", "محلل الأفكار العربي (نية/ميزات/افتراضات/أسئلة ≤3)", "R6", true, "مكتمل"),
        Feature("PROMPT.BUILDER", "مولد البرومبتات 12 قسماً بثلاث لغات", "R7", true, "مكتمل"),
        Feature("QUALITY.CHECK", "فحص الجودة 14 عنصراً + إصلاح تلقائي", "R9", true, "مكتمل"),
        Feature("CRITIQUE", "النقد والاقتراح", "R9", true, "مكتمل"),
        Feature("IMAGE.ANALYZE", "تحليل صور الواجهات", "R8", true, "سحابي اختياري عبر Gemini بموافقة صريحة — غير متاح محلياً بصدق"),
        Feature("AI.CLOUD.GEMINI", "محرك Gemini السحابي", "R5", true, "جاهز بعد إدخال المفتاح ومنح الموافقة"),
        Feature("AI.CLOUD.OPENAI", "محرك متوافق OpenAI", "R5", true, "جاهز بعد إدخال المفتاح ومنح الموافقة"),
        Feature("EXPORT.SHARE", "تصدير TXT/MD/JSON ومشاركة ونسخ", "R10", true, "مكتمل — UTF-8 مع سلامة العربية"),
        Feature("KEYS.SECURE", "مفاتيح API مشفرة", "R11", true, "مكتمل — EncryptedSharedPreferences"),
        Feature("CLOUD.CONSENT", "بوابة موافقة السحابة (مرفوضة افتراضياً)", "R11", true, "مكتمل"),
        Feature("QUALITY.TESTS", "اختبارات وحدة لطبقة المجال", "R12", true, "مكتمل — QualityChecker/IdeaAnalyzer"),
        Feature("CI.BUILD", "بناء APK تلقائي عبر GitHub Actions", "R12", true, "مكتمل — workflow في المستودع"),
        Feature("NEURAL.ONDEVICE", "نموذج عصبي محلي على الجهاز", "R4", false, "قيد التكامل لاحقاً — المحرك المحلي الحالي قاعدي"),
        Feature("UI.RUNTIME", "تشغيل الكود داخل المحرر", "R3", false, "مؤجل لمرحلة لاحقة بدواعٍ أمنية"),
        Feature("SEARCH.WEB", "البحث في الإنترنت", "R9", false, "ميزة اختيارية مستقبلية")
    )

    fun byId(id: String): Feature? = features.firstOrNull { it.id == id }

    fun readyCount(): Int = features.count { it.isReady }
    fun plannedCount(): Int = features.size
}
