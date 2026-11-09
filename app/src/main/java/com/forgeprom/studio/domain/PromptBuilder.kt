package com.forgeprom.studio.domain

import com.forgeprom.studio.core.settings.PromptLanguage

/**
 * R7 — مولد البرومبتات: 12 قسماً إلزامياً بثلاث لغات ناتج.
 * يتضمن قيود منع تضخم الكود — ميزة أساسية وليست اختيارية.
 */
object PromptBuilder {

    /** بناء البرومبت الكامل من مسودة معتمدة */
    fun build(draft: AnalysisDraft, tech: String, language: PromptLanguage, isImageInspired: Boolean): String {
        return when (language) {
            PromptLanguage.EN -> buildEnglish(draft, tech, isImageInspired)
            PromptLanguage.DUAL -> buildDual(draft, tech, isImageInspired)
            else -> buildArabic(draft, tech, isImageInspired)
        }
    }

    /** البرومبت ثنائي اللغة: إنجليزي تقني + ملخص عربي */
    fun buildDual(draft: AnalysisDraft, tech: String, isImageInspired: Boolean): String {
        return buildEnglish(draft, tech, isImageInspired) + "\n\n" +
            "═══ ملخص عربي ═══\n" +
            buildArabic(draft, tech, isImageInspired)
    }

    private fun buildArabic(draft: AnalysisDraft, tech: String, isImageInspired: Boolean): String {
        val techLine = if (tech.isBlank() || tech == "اقترح لي") "اقترح التقنية الأنسب لطبيعة المشروع"
        else "التقنية المستهدفة: " + tech

        return """
الدور:
- أنت مهندس برمجيات خبير تبني تطبيقات إنتاجية عالية الجودة.

الهدف:
${draft.goals.joinToString("\n- ", "- ")}

نوع الطلب: ${draft.intent}
السياق والمجال: ${draft.summary}

المتطلبات الوظيفية:
${draft.requirements.joinToString("\n- ", "- ")}

القيود:
${draft.constraints.joinToString("\n- ", "- ")}
${if (isImageInspired) "- الصورة المرجعية للاستلهام فقط: لا نسخ للأصول أو الهوية البصرية، واذكر ذلك كافتراض." else ""}

الافتراضات المعلنة:
${if (draft.assumptions.isEmpty()) "- لا افتراضات: الوصف كان كافياً" else draft.assumptions.joinToString("\n- ", "- ")}

التقنية المستهدفة:
- $techLine

مواصفات الواجهة:
- تخطيط واضح متجاوب مع الموبايل والتابلت والحاسوب
- دعم العربية واتجاه RTL والنص المختلط
- أحجام لمس مناسبة ومسافات متناسقة

الحالات الإلزامية:
- تحميل | خطأ (مع رسالة وسبيل إعادة محاولة) | فراغ | نجاح

التفاعلات:
- حدد إجراء كل زر ونموذج وتنقل
- تحقق من صحة المدخلات قبل الإرسال
- عالج نجاح وفشل كل عملية بوضوح

الوصولية:
- تسميات وصولية لكل عنصر تفاعلي
- تباين ألوان كافٍ ودعم قارئ الشاشة

قيود جودة الكود (منع التضخم):
- لا تولّد كوداً غير ضروري ولا اعتماديات غير مستخدمة
- لا شاشات placeholder إلا بطلب صريح
- لا تكرار مكونات أو منطقاً — أعد استخدام ما هو قائم
- لا تجريداً مفرطاً للميزات البسيطة
- ملفات الميزة المطلوبة فقط وفق بنية المشروع
- أزل الاستيرادات والكود المعلّق غير المستخدم

معايير القبول:
- الميزة تعمل دون أخطاء تشغيل
- كل الحالات الإلزامية مُعالجة
- لا كسر لأي وظيفة قائمة
- الكود قابل للقراءة والصيانة

شكل التسليم:
- ملفات كاملة قابلة للتشغيل مباشرة مع خطوات التنفيذ
- اذكر أي خطوة يدوية يحتاجها المستخدم
""".trim()
    }

    private fun buildEnglish(draft: AnalysisDraft, tech: String, isImageInspired: Boolean): String {
        val techLine = if (tech.isBlank() || tech == "اقترح لي") "Suggest the most suitable stack"
        else "Target stack: " + tech

        return """
ROLE:
- You are a senior software engineer building production-grade applications.

OBJECTIVE:
${draft.goals.joinToString("\n- ", "- ")}

REQUEST TYPE: ${draft.intent}
CONTEXT: ${draft.summary}

FUNCTIONAL REQUIREMENTS:
${draft.requirements.joinToString("\n- ", "- ")}

CONSTRAINTS:
${draft.constraints.joinToString("\n- ", "- ")}
${if (isImageInspired) "- The reference image is for inspiration only: do not copy assets or brand identity; state this as an assumption." else ""}

ASSUMPTIONS:
${if (draft.assumptions.isEmpty()) "- None: the description was sufficient" else draft.assumptions.joinToString("\n- ", "- ")}

TECH STACK:
- $techLine

UI SPECIFICATION:
- Clear responsive layout for mobile, tablet, and desktop
- Arabic-first with RTL support and mixed-script text
- Proper touch targets and consistent spacing

REQUIRED STATES:
- loading | error (with message + retry) | empty | success

INTERACTIONS:
- Specify the action of every button, form, and navigation
- Validate inputs before submission
- Handle success and failure of every operation explicitly

ACCESSIBILITY:
- Accessibility labels on all interactive elements
- Sufficient color contrast and screen reader support

CODE QUALITY CONSTRAINTS (ANTI-BLOAT):
- No unnecessary code or unused dependencies
- No placeholder screens unless explicitly requested
- No duplicated components or logic — reuse what exists
- No over-abstraction for simple features
- Only the files this feature requires, following the project structure
- Remove unused imports and commented-out code

ACCEPTANCE CRITERIA:
- Feature runs without runtime errors
- All required states handled
- No breakage of existing functionality
- Readable, maintainable code

OUTPUT FORMAT:
- Complete runnable files plus implementation steps
- Note any manual step the user must perform
""".trim()
    }

    /**
     * R9 — الإصلاح التلقائي: إضافة الأقسام الناقصة إلى برومبت قائم.
     * لا يدّعي إعادة كتابة كاملة — يضيف فقرات ناقصة بوضوح.
     */
    fun autoFix(prompt: String, missing: List<String>): String {
        if (missing.isEmpty()) return prompt
        val additions = missing.joinToString("\n") { section ->
            when {
                section.contains("الوصولية") || section.contains("ACCESSIBILITY") ->
                    "ACCESSIBILITY: add labels to all interactive elements; ensure contrast and screen-reader support."
                section.contains("معايير القبول") || section.contains("ACCEPTANCE") ->
                    "ACCEPTANCE CRITERIA: runs without errors; all states handled; no breakage; maintainable."
                section.contains("شكل التسليم") || section.contains("OUTPUT FORMAT") ->
                    "OUTPUT FORMAT: complete runnable files plus implementation steps."
                section.contains("الحالات") || section.contains("STATES") ->
                    "REQUIRED STATES: loading | error (message + retry) | empty | success."
                section.contains("التفاعلات") || section.contains("INTERACTIONS") ->
                    "INTERACTIONS: define every button, form, and navigation; validate inputs; handle success/failure."
                section.contains("مواصفات الواجهة") || section.contains("UI SPECIFICATION") ->
                    "UI SPECIFICATION: responsive layout; Arabic RTL; consistent spacing."
                else -> section + ": (auto-filled placeholder — edit before use)"
            }
        }
        return prompt + "\n\n═══ إصلاح تلقائي — أقسام ناقصة أضيفت ═══\n" + additions
    }
}
