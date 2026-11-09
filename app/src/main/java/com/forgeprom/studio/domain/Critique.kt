package com.forgeprom.studio.domain

/**
 * R9 — النقد والاقتراح: نقاط قوة ونواقص ومخاطر واقتراحات
 * مشتقة من التحليل وتقرير الجودة — كل ملاحظة مربوطة بسبب واضح.
 */
data class CritiqueData(
    val strengths: List<String>,
    val weaknesses: List<String>,
    val risks: List<String>,
    val suggestions: List<String>
)

object Critique {

    fun critique(draft: AnalysisDraft, report: QualityReportData): CritiqueData {
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val risks = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        if (draft.goals.isNotEmpty()) strengths.add("الهدف مصاغ بوضوح في " + draft.goals.size + " نقطة — يسهل على منصة التوليد الالتزام به")
        if (draft.features.size > 1) strengths.add("الميزات محددة بالاسم (" + draft.features.size + " ميزة) — يقلل كوداً خارج النطاق")
        if (draft.constraints.isNotEmpty()) strengths.add("قيود صريحة تمنع تضخم الكود والحفاظ على الوظائف القائمة")

        if (report.missing.isNotEmpty()) {
            weaknesses.add("تقرير الجودة (" + report.score + "/100) وجد " + report.missing.size + " قسماً ناقصاً: " + report.missing.joinToString("، "))
            suggestions.add("استخدم زر الإصلاح التلقائي لإضافة الأقسام الناقصة ثم أعد الفحص")
        }
        if (draft.assumptions.isNotEmpty()) {
            weaknesses.add("هناك " + draft.assumptions.size + " افتراضاً لم يؤكده المستخدم — قد يبعد الناتج عن نيته")
            suggestions.add("راجع الافتراضات في تبويبها وعدّلها إن كانت تخالف مقصودك")
        }
        if (draft.questions.isNotEmpty()) {
            weaknesses.add("أسئلة مفتوحة (" + draft.questions.size + ") بلا إجابة — الوصف ناقص في هذه الجوانب")
            suggestions.add("أجب عن الأسئلة وأعد التوليد لرفع دقة البرومبت")
        }

        if (draft.features.any { it.contains("دفع") }) risks.add("ميزة الدفع تريد تشديداً أمنياً (HTTPS، تحقق مدخلات، عدم تخزين بيانات بطاقات)")
        if (draft.features.any { it.contains("تسجيل الدخول") }) risks.add("المصادقة تطلب حماية بيانات الاعتماد وتجزئة كلمات المرور")
        if (draft.intent == "تطبيق كامل" && draft.features.size < 3) risks.add("طلب تطبيق كامل بميزات قليلة جداً — قد يعني الوصف ناقصاً أو النية أضيق من الطلب")
        if (draft.intent == "ميزة لتطبيق موجود") risks.add("التعديل على تطبيق قائم يخاطر بكسر وظائف قائمة إن لم تُذكر قيود التوافق")

        if (suggestions.isEmpty()) suggestions.add("البرومبت متكامل — انسخه واستخدمه في منصة التوليد مباشرة")

        return CritiqueData(strengths, weaknesses, risks, suggestions)
    }
}
