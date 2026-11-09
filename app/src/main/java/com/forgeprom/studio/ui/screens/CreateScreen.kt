package com.forgeprom.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.domain.IdeaAnalyzer
import com.forgeprom.studio.ui.components.SectionCard
import kotlinx.coroutines.launch

private val GOALS = listOf(
    "تطبيق كامل", "ميزة لتطبيق موجود", "واجهة فقط",
    "تحسين واجهة", "إصلاح خطأ", "إعادة هيكلة", "غير متأكد"
)

private val TECHS = listOf(
    "اقترح لي", "React", "Next.js", "Flutter", "React Native",
    "SwiftUI", "Kotlin Compose", "HTML/CSS/Tailwind"
)

/**
 * R6 — شاشة الإنشاء: وصف حر + هدف + تقنية → تحليل فوري (محلي) ثم شاشة التحليل.
 */
@Composable
fun CreateScreen(container: AppContainer, onAnalyzed: () -> Unit) {
    var idea by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("غير متأكد") }
    var tech by remember { mutableStateOf("اقترح لي") }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("إنشاء موجه جديد", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = idea,
            onValueChange = { idea = it },
            label = { Text("اشرح ما تريد بناءه — بالعربية بحرية وبدون مصطلحات") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        SectionCard("نوع الطلب") {
            Column {
                GOALS.chunked(3).forEach { rowGoals ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowGoals.forEach { g ->
                            FilterChip(selected = goal == g, onClick = { goal = g }, label = { Text(g) })
                        }
                    }
                }
            }
        }

        SectionCard("التقنية المستهدفة") {
            Column {
                TECHS.chunked(3).forEach { rowTechs ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowTechs.forEach { t ->
                            FilterChip(selected = tech == t, onClick = { tech = t }, label = { Text(t) })
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (idea.isBlank()) return@Button
                scope.launch {
                    val draft = IdeaAnalyzer.analyze(idea, goal, tech)
                    container.session.clear()
                    container.session.draft = draft
                    container.session.sourceInstruction = idea
                    container.session.goal = goal
                    container.session.tech = tech
                    container.logActivity("analyze", draft.summary)
                    onAnalyzed()
                }
            },
            enabled = idea.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تحليل وتوليد البرومبت")
        }

        Text(
            "التحليل يتم محلياً على جهازك دون إنترنت. الافتراضات والأسئلة ستُعرض عليك قبل التوليد — لا تخمين بصمت.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
