package com.forgeprom.studio.domain

/**
 * R9 — فحص جودة البرومبت: 14 عنصراً، درجة من 100 (خصم 7 لكل ناقص).
 * فحص حقيقي قائم على محتوى البرومبت — لا درجات شكلية.
 */
data class QualityReportData(
    val score: Int,
    val missing: List<String>,
    val findings: List<String>
)

object QualityChecker {

    private val checks = listOf(
        "الهدف" to listOf("الهدف", "OBJECTIVE"),
        "نوع الطلب" to listOf("نوع الطلب", "REQUEST TYPE"),
        "السياق" to listOf("السياق", "CONTEXT"),
        "التقنية المستهدفة" to listOf("التقنية", "TECH STACK"),
        "المتطلبات الوظيفية" to listOf("المتطلبات الوظيفية", "FUNCTIONAL REQUIREMENTS", "المتطلبات"),
        "مواصفات الواجهة" to listOf("مواصفات الواجهة", "UI SPECIFICATION"),
        "الحالات" to listOf("الحالات الإلزامية", "REQUIRED STATES", "الحالات"),
        "التفاعلات" to listOf("التفاعلات", "INTERACTIONS"),
        "الوصولية" to listOf("الوصولية", "ACCESSIBILITY"),
        "قيود جودة الكود" to listOf("منع التضخم", "ANTI-BLOAT", "قيود جودة الكود"),
        "معايير القبول" to listOf("معايير القبول", "ACCEPTANCE CRITERIA"),
        "شكل التسليم" to listOf("شكل التسليم", "OUTPUT FORMAT"),
        "الافتراضات" to listOf("الافتراضات", "ASSUMPTION"),
        "تحذير الاستلهام" to listOf("الاستلهام فقط", "inspiration only")
    )

    fun check(prompt: String, draft: AnalysisDraft, isImageInspired: Boolean): QualityReportData {
        val missing = mutableListOf<String>()
        val findings = mutableListOf<String>()

        checks.forEach { (label, keywords) ->
            val present = keywords.any { prompt.contains(it, ignoreCase = true) }
            if (present) {
                findings.add("مكتمل: " + label)
            } else {
                // تحذير الاستلهام مطلوب فقط مع الصور — عنصر اختياري بلا صورة
                if (label == "تحذير الاستلهام" && !isImageInspired) {
                    findings.add("غير مطلوب (لا صورة): " + label)
                    return@forEach
                }
                if (label == "الافتراضات" && draft.assumptions.isEmpty()) {
                    findings.add("غير مطلوب (لا افتراضات): " + label)
                    return@forEach
                }
                missing.add(label)
            }
        }

        val score = (100 - missing.size * 7).coerceIn(0, 100)
        if (missing.isNotEmpty()) {
            findings.add("ناقص (" + missing.size + "): " + missing.joinToString("، "))
        }
        return QualityReportData(score = score, missing = missing, findings = findings)
    }
}
