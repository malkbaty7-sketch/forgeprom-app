package com.forgeprom.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.settings.AppSettings
import com.forgeprom.studio.domain.Critique
import com.forgeprom.studio.domain.PromptBuilder
import com.forgeprom.studio.domain.QualityChecker
import com.forgeprom.studio.ui.components.SectionCard
import kotlinx.coroutines.launch

/**
 * R6/R7 — شاشة التحليل: مسودة قابلة للتحرير والاعتماد، ثم توليد عبر المحرك الافتراضي.
 */
@Composable
fun AnalysisScreen(container: AppContainer, onApproved: () -> Unit) {
    val draft = container.session.draft ?: return
    var summary by remember { mutableStateOf(draft.summary) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val settings by container.settings.settings.collectAsState(initial = AppSettings())

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("تحليل الفكرة", style = MaterialTheme.typography.headlineSmall)
        Text(
            "النية المكتشفة: " + draft.intent,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = summary,
            onValueChange = { summary = it },
            label = { Text("ملخص الفكرة (قابل للتحرير)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        SectionCard("الأهداف") {
            draft.goals.forEach { Text("• " + it) }
        }
        SectionCard("الميزات المكتشفة") {
            draft.features.forEach { Text("• " + it) }
        }
        SectionCard("المتطلبات والقيود") {
            (draft.requirements + draft.constraints).forEach { Text("• " + it) }
        }
        SectionCard("الافتراضات (معلنة — عدّلها بردّك أو تجاهلها)") {
            if (draft.assumptions.isEmpty()) Text("لا افتراضات — الوصف كافٍ")
            draft.assumptions.forEach { Text("• " + it) }
        }
        SectionCard("أسئلة توضيحية (اختياري — أجب في الملخص لرفع الدقة)") {
            if (draft.questions.isEmpty()) Text("لا أسئلة — الوصف واضح")
            draft.questions.forEach { Text("• " + it) }
        }

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        if (busy) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        error = ""
                        try {
                            val engine = container.engines.defaultForTask("general")
                            val editedDraft = draft.copy(summary = summary)
                            val result = engine.generate(
                                com.forgeprom.studio.core.ai.GenerationRequest(
                                    instruction = container.session.sourceInstruction,
                                    goal = container.session.goal,
                                    tech = container.session.tech,
                                    promptLanguage = settings.promptLanguage,
                                    isImageInspired = container.session.isImageInspired
                                )
                            )
                            val promptText = result.getOrElse {
                                // الصدق: عند فشل السحابي نستخدم المحلي القاعدي مع إعلان ذلك
                                val local = PromptBuilder.build(
                                    editedDraft, container.session.tech,
                                    settings.promptLanguage, container.session.isImageInspired
                                )
                                error = "تعذر المحرك الافتراضي (" + (it.message ?: "") + ") — تم التوليد بالمحرك المحلي القاعدي."
                                local
                            }
                            val report = QualityChecker.check(
                                promptText, editedDraft, container.session.isImageInspired
                            )
                            val critique = Critique.critique(editedDraft, report)
                            val fixed = if (report.score < 70) {
                                PromptBuilder.autoFix(promptText, report.missing)
                            } else promptText
                            val fixedReport = QualityChecker.check(fixed, editedDraft, container.session.isImageInspired)
                            container.session.draft = editedDraft
                            container.session.promptText = fixed
                            container.session.report = fixedReport
                            container.session.critique = critique
                            container.logActivity("generate", "توليد موجه — جودة " + fixedReport.score + "/100")
                            onApproved()
                        } finally {
                            busy = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("اعتماد وتوليد البرومبت")
            }
        }
    }
}
