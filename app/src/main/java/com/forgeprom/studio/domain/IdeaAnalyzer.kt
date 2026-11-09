package com.forgeprom.studio.domain

/**
 * R6 — محلل الأفكار العربي (قاعدي، يعمل دون إنترنت).
 * مبني على قواعد لغوية عربية حقيقية: كلمات مفتاحية، أنماط طلب، استخراج ميزات.
 * المخرجات: نية، ملخص، أهداف، ميزات، متطلبات، قيود، افتراضات معلنة، أسئلة ≤3.
 */
data class AnalysisDraft(
    val intent: String,
    val summary: String,
    val goals: List<String>,
    val features: List<String>,
    val requirements: List<String>,
    val constraints: List<String>,
    val assumptions: List<String>,
    val questions: List<String>
)

object IdeaAnalyzer {

    /** أنواع النية — مكتشفة بقواعد لغوية عربية */
    private val intentRules = listOf(
        "واجهة فقط" to listOf("واجهة", "شاشة", "صفحة", "تصميم واجهة", "ui", "ux"),
        "إصلاح خطأ" to listOf("خطأ", "لا يعمل", "يتعطل", "فشل", "عطل", "bug", "يتوقف"),
        "تحسين واجهة" to listOf("تحسين", "أعد تصميم", "تطوير", "أحسن", "اجعله أسرع", "أسرع"),
        "إعادة هيكلة" to listOf("أعد هيكلة", "نظف الكود", "refactor", "هيكلة"),
        "ميزة لتطبيق موجود" to listOf("أضف", "إضافة", "أرفق", "ألحق", "أكمل")
    )

    /** كلمات ميزات عربية شائعة — تُستخرج حين تظهر في الوصف */
    private val featureRules = listOf(
        "تسجيل الدخول والمصادقة" to listOf("تسجيل الدخول", "دخول", "حساب", "مصادقة", "ايميل", "كلمة المرور", "أوتنتيك"),
        "التسجيل وإنشاء الحساب" to listOf("تسجيل جديد", "إنشاء حساب", "سجل", "حساب جديد"),
        "إدارة المهام" to listOf("مهام", "task", "todo", "قائمة مهام"),
        "المتجر والمنتجات" to listOf("متجر", "منتجات", "تجارة", "بيع", "سوق"),
        "السلة والدفع" to listOf("سلة", "شراء", "دفع", "فيزا", "بطاقة", " PayPal"),
        "المفضلة" to listOf("مفضلة", "أفضل", "مفضّل"),
        "البحث والفلترة" to listOf("بحث", "فلترة", "تصفية", "فلتر"),
        "الإشعارات" to listOf("إشعار", "اشعارات", "تنبيه", "notifications"),
        "الخرائط والموقع" to listOf("خريطة", "موقع", "gps", "خرائط"),
        "الدردشة والرسائل" to listOf("دردشة", "رسائل", "محادثة", "شات", "chat"),
        "لوحة تحكم / إحصائيات" to listOf("لوحة تحكم", "إحصائيات", "تقارير", "dashboard"),
        "قاعدة بيانات" to listOf("قاعدة بيانات", "بيانات", "تخزين", "database"),
        "الملف الشخصي" to listOf("ملف شخصي", "بروفايل", "صفحتي"),
        "المشاركة" to listOf("مشاركة", "شارك", "share"),
        "غير متصل" to listOf("بدون إنترنت", "دون إنترنت", "أوفلاين", "offline")
    )

    /** التقنيات المذكورة صريحة في النص */
    private val techRules = listOf(
        "React" to listOf("react"),
        "Next.js" to listOf("next.js", "nextjs"),
        "Flutter" to listOf("flutter"),
        "React Native" to listOf("react native"),
        "SwiftUI" to listOf("swift", "ios", "آيفون", "ايفون"),
        "Kotlin Compose" to listOf("kotlin", "أندرويد", "اندرويد", "android"),
        "HTML/CSS/Tailwind" to listOf("html", "css", "tailwind", "ويب", "موقع")
    )

    /**
     * تحليل فكرة عربية حرة إلى مسودة متطلبات.
     * كل مسودة تحمل افتراضاتها معلنةً — لا تخمين بصمت.
     */
    fun analyze(idea: String, goal: String, tech: String): AnalysisDraft {
        val lower = idea.lowercase()
        val intent = detectIntent(lower, goal)
        val features = featureRules.filter { (_, keys) -> keys.any { lower.contains(it) } }
            .map { it.first }
            .ifEmpty { listOf("ميزة أساسية وفق وصف المستخدم") }

        val goalsList = buildGoals(intent, idea)
        val requirements = buildRequirements(intent, features)
        val constraints = buildConstraints(lower)
        val assumptions = mutableListOf<String>()
        val questions = mutableListOf<String>()

        // افتراضات معلنة دائماً
        if (tech.isBlank() || tech == "اقترح لي") {
            assumptions.add("التقنية المستهدفة غير محددة من المستخدم — ستُقترح الأنسب للمشروع")
        }
        if (intent == "تطبيق كامل" && features.size <= 2) {
            assumptions.add("الوصف موجز — ستفهم الميزات الأساسية فقط وتُبنى عليها الميزات المشتقة")
        }
        if (lower.contains("مستخدم")) {
            // لا شيء — ذُكر المستخدمون
        } else {
            assumptions.add("لم تُذكر جمهور المستخدمين — سيفترض مستخدماً عادياً غير تقني")
        }
        if (!lower.contains("لغة")) {
            assumptions.add("لغة الواجهة: العربية RTL ما لم يذكر المستخدم خلاف ذلك")
        }

        // أسئلة توضيحية — 3 كحد أقصى (قاعدة أساسية)
        if (lower.contains("متجر") && !lower.contains("دفع")) {
            questions.add("هل تحتاج بوابة دفع فعلية (مثل Stripe) أم عرض سلة فقط في هذه النسخة؟")
        }
        if (lower.contains("حساب") && !lower.contains("توثيق")) {
            questions.add("هل تكفي مصادقة بريد/كلمة مرور أم تريد تسجيل دخول اجتماعي (جوجل مثلاً)؟")
        }
        if (intent == "تطبيق كامل" && features.none { it.contains("بيانات") }) {
            questions.add("أين تحفظ بيانات المستخدم — خدمة سحابية أم تخزين محلي على الجهاز؟")
        }
        if (lower.contains("بدون إنترنت") || lower.contains("دون إنترنت")) {
            questions.add("ماذا يفعل التطبيق بالضبط عند غياب الاتصال — عرض بيانات محفوظة أم إخفاء أقسام كاملة؟")
        }

        val summary = buildSummary(intent, features)

        return AnalysisDraft(
            intent = intent,
            summary = summary,
            goals = goalsList,
            features = features,
            requirements = requirements,
            constraints = constraints,
            assumptions = assumptions,
            questions = questions.take(3)
        )
    }

    private fun detectIntent(lower: String, goal: String): String {
        if (goal != "غير متأكد") return goal
        for ((intent, keys) in intentRules) {
            if (keys.any { lower.contains(it) }) return intent
        }
        return "تطبيق كامل"
    }

    private fun buildGoals(intent: String, idea: String): List<String> {
        val base = when (intent) {
            "واجهة فقط" -> "بناء واجهة مستخدم مطابقة للوصف دون منطق خلفي"
            "إصلاح خطأ" -> "تشخيص الخطأ المذكور وإصلاحه دون كسر الوظائف القائمة"
            "تحسين واجهة" -> "تحسين ما وُصف مع الحفاظ على الوظائف الحالية"
            "إعادة هيكلة" -> "تحسين بنية الكود دون تغيير سلوكه"
            "ميزة لتطبيق موجود" -> "إضافة الميزة المطلوبة إلى التطبيق القائم بسلاسة"
            else -> "بناء تطبيق كامل يعمل بالميزات الموصوفة"
        }
        return listOf(base)
    }

    private fun buildRequirements(intent: String, features: List<String>): List<String> {
        val req = mutableListOf(
            "حالات واجهة كاملة: تحميل، خطأ، فراغ، نجاح — لا شاشة بلا معالجة"
        )
        if (features.any { it.contains("دفع") }) req.add("دفع آمن عبر بوابة معتمدة (لا تخزين بيانات بطاقات)")
        if (features.any { it.contains("مصادقة") }) req.add("حماية بيانات الاعتماد وتجزئة كلمات المرور")
        if (intent == "تطبيق كامل" || intent == "ميزة لتطبيق موجود") {
            req.add("تنقل واضح بين الشاشات مع رجوع متسق")
        }
        return req
    }

    private fun buildConstraints(lower: String): List<String> {
        val c = mutableListOf(
            "لا كود متضخم: توليد الملفات المطلوبة للميزة فقط، دون مكتبات غير مستخدمة",
            "الحفاظ على أي وظائف قائمة عند التعديل — لا كسر لما يعمل"
        )
        if (lower.contains("عربي") || lower.contains("عربية") || !lower.contains("english")) {
            c.add("واجهة عربية RTL مع دعم النص المختلط")
        }
        return c
    }

    private fun buildSummary(intent: String, features: List<String>): String {
        return "طلب من نوع «" + intent + "» يشمل: " + features.joinToString("، ")
    }
}
