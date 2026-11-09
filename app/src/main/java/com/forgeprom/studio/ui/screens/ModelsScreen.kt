package com.forgeprom.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.ai.EngineStatus
import com.forgeprom.studio.core.settings.AppSettings
import com.forgeprom.studio.ui.components.PhaseNote
import com.forgeprom.studio.ui.components.SectionCard
import com.forgeprom.studio.ui.components.StatusChip
import kotlinx.coroutines.launch

/**
 * R5 — مركز المحركات: الحالة الصادقة + المحرك الافتراضي لكل مهمة.
 */
@Composable
fun ModelsScreen(container: AppContainer) {
    val settings by container.settings.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()
    var statuses by remember { mutableStateOf<List<EngineStatus>>(emptyList()) }

    LaunchedEffect(settings.cloudConsent) {
        statuses = container.engines.allEngines().map { it.status(settings) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("المحركات", style = MaterialTheme.typography.headlineSmall)

        statuses.forEach { st ->
            Card(Modifier.padding(0.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(st.label, style = MaterialTheme.typography.titleSmall)
                        StatusChip(label = if (st.isReady) "جاهز" else "غير جاهز", ready = st.isReady)
                    }
                    Text(
                        st.statusLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SectionCard("المحرك الافتراضي لكل مهمة") {
            listOf(
                "general" to "التوليد العام",
                "code" to "توليد الكود",
                "analysis" to "التحليل"
            ).forEach { (task, label) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(label, style = MaterialTheme.typography.titleSmall)
                    val current = when (task) {
                        "general" -> settings.engineGeneral
                        "code" -> settings.engineCode
                        else -> settings.engineAnalysis
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "local.rules" to "محلي",
                            "cloud.gemini" to "Gemini",
                            "cloud.openai_compat" to "متوافق OpenAI"
                        ).forEach { (id, name) ->
                            FilterChip(
                                selected = current == id,
                                onClick = { scope.launch { container.settings.setEngineForTask(task, id) } },
                                label = { Text(name) }
                            )
                        }
                    }
                }
            }
        }

        PhaseNote(
            "المحرك المحلي الحالي قاعدي (قواعد عربية) ويعمل دون إنترنت بالكامل. النموذج العصبي المحلي قيد التكامل لاحقاً. المحركات السحابية لا تُستخدم إلا بعد موافقتك الصريحة وإدخال مفتاحك."
        )
    }
}
