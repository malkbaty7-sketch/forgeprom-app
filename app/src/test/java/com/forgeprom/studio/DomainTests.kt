package com.forgeprom.studio

import com.forgeprom.studio.core.settings.PromptLanguage
import com.forgeprom.studio.domain.Critique
import com.forgeprom.studio.domain.IdeaAnalyzer
import com.forgeprom.studio.domain.PromptBuilder
import com.forgeprom.studio.domain.QualityChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * R12 — اختبارات وحدة لطبقة المجال (Kotlin نقي، دون Android).
 */
class DomainTests {

    private fun sampleDraft() = IdeaAnalyzer.analyze(
        "أريد تطبيق متجر إلكتروني مع سلة شراء ودفع بدون إنترنت",
        "غير متأكد",
        "اقترح لي"
    )

    @Test
    fun `full arabic prompt scores 100`() {
        val draft = sampleDraft()
        val prompt = PromptBuilder.build(draft, "Kotlin", PromptLanguage.AR, false)
        val report = QualityChecker.check(prompt, draft, false)
        assertEquals(100, report.score)
        assertTrue(report.missing.isEmpty())
    }

    @Test
    fun `image inspired prompt declares inspiration and scores at least 85`() {
        val draft = sampleDraft()
        val prompt = PromptBuilder.build(draft, "Compose", PromptLanguage.AR, true)
        val report = QualityChecker.check(prompt, draft, true)
        assertTrue(prompt.contains("استلهام"))
        assertTrue(report.score >= 85)
    }

    @Test
    fun `incomplete prompt scores below 70 and autoFix appends the fix`() {
        val draft = sampleDraft()
        val weak = "الهدف:\nشئ ما"
        val report = QualityChecker.check(weak, draft, false)
        assertTrue(report.score < 70)
        val fixed = PromptBuilder.autoFix(weak, report.missing)
        assertTrue(fixed.contains("إصلاح تلقائي"))
    }

    @Test
    fun `analyzer detects full app intent with limited questions and assumptions`() {
        val draft = sampleDraft()
        assertEquals("تطبيق كامل", draft.intent)
        assertTrue(draft.questions.size <= 3)
        assertTrue(draft.assumptions.isNotEmpty())
    }

    @Test
    fun `analyzer detects bug fix intent`() {
        val draft = IdeaAnalyzer.analyze(
            "زر الحفظ لا يعمل ويظهر خطأ عند الضغط", "غير متأكد", "اقترح لي"
        )
        assertEquals("إصلاح خطأ", draft.intent)
    }

    @Test
    fun `critique returns non-empty strengths and suggestions`() {
        val draft = sampleDraft()
        val prompt = PromptBuilder.build(draft, "Kotlin", PromptLanguage.AR, false)
        val report = QualityChecker.check(prompt, draft, false)
        val critique = Critique.critique(draft, report)
        assertTrue(critique.strengths.isNotEmpty())
        assertTrue(critique.suggestions.isNotEmpty())
    }

    @Test
    fun `english prompt contains anti-bloat and acceptance criteria`() {
        val draft = sampleDraft()
        val prompt = PromptBuilder.build(draft, "Kotlin", PromptLanguage.EN, false)
        assertTrue(prompt.contains("ANTI-BLOAT"))
        assertTrue(prompt.contains("ACCEPTANCE CRITERIA"))
        assertTrue(prompt.contains("OUTPUT FORMAT"))
    }

    @Test
    fun `dual prompt contains english role and arabic summary`() {
        val draft = sampleDraft()
        val prompt = PromptBuilder.build(draft, "Kotlin", PromptLanguage.DUAL, false)
        assertTrue(prompt.contains("ROLE:"))
        assertTrue(prompt.contains("ملخص عربي"))
    }
}
