package com.forgeprom.studio.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.registry.FeatureRegistry
import com.forgeprom.studio.core.settings.AppSettings
import com.forgeprom.studio.core.settings.PromptLanguage
import com.forgeprom.studio.core.settings.ThemeMode
import com.forgeprom.studio.ui.components.PhaseNote
import com.forgeprom.studio.ui.components.SectionCard
import com.forgeprom.studio.ui.components.StatusChip
import kotlinx.coroutines.launch

/**
 * R11 — الإعدادات: الثيم، لغة الموجه، بوابة الموافقة، المفاتيح المشفرة، تشخيص الميزات.
 */
@Composable
fun SettingsScreen(container: AppContainer, onAbout: () -> Unit) {
    val settings by container.settings.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var geminiKey by remember { mutableStateOf("") }
    var openAiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf(container.apiKeys.openAiBaseUrl()) }
    var model by remember { mutableStateOf(container.apiKeys.openAiModel()) }

    LaunchedEffect(Unit) {
        baseUrl = container.apiKeys.openAiBaseUrl()
        model = container.apiKeys.openAiModel()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("الإعدادات", style = MaterialTheme.typography.headlineSmall)

        SectionCard("المظهر") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ThemeMode.SYSTEM to "تلقائي",
                    ThemeMode.LIGHT to "فاتح",
                    ThemeMode.DARK to "داكن"
                ).forEach { (m, label) ->
                    FilterChip(
                        selected = settings.themeMode == m,
                        onClick = { scope.launch { container.settings.setThemeMode(m) } },
                        label = { Text(label) }
                    )
                }
            }
        }

        SectionCard("لغة البرومبت الناتج") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PromptLanguage.AR to "عربي",
                    PromptLanguage.EN to "English",
                    PromptLanguage.DUAL to "ثنائي"
                ).forEach { (l, label) ->
                    FilterChip(
                        selected = settings.promptLanguage == l,
                        onClick = { scope.launch { container.settings.setPromptLanguage(l) } },
                        label = { Text(label) }
                    )
                }
            }
        }

        SectionCard("موافقة السحابة") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("السماح بإرسال المحتوى إلى محركات سحابية")
                    Text(
                        "مرفوضة افتراضياً — لا يغادر أي محتوى جهازك دون موافقتك الصريحة هنا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.cloudConsent,
                    onCheckedChange = { scope.launch { container.settings.setCloudConsent(it) } }
                )
            }
        }

        SectionCard("مفاتيح API (تُحفظ مشفّرة على الجهاز)") {
            OutlinedTextField(
                value = geminiKey,
                onValueChange = { geminiKey = it },
                label = { Text("مفتاح Gemini") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = openAiKey,
                onValueChange = { openAiKey = it },
                label = { Text("مفتاح OpenAI أو مزود متوافق") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("عنوان المزود الأساسي") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("اسم النموذج") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                if (geminiKey.isNotBlank()) container.apiKeys.setGeminiKey(geminiKey.trim())
                if (openAiKey.isNotBlank()) container.apiKeys.setOpenAiKey(openAiKey.trim())
                container.apiKeys.setOpenAiBaseUrl(baseUrl.trim())
                container.apiKeys.setOpenAiModel(model.trim())
                geminiKey = ""
                openAiKey = ""
                Toast.makeText(context, "تم الحفظ المشفر", Toast.LENGTH_SHORT).show()
            }) { Text("حفظ") }
            Text(
                "لا تُعرض المفاتيح المحفوظة — الحقول فارغة افتراضياً. اتركها فارغة للاحتفاظ بالقيم الحالية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard("تشخيص الميزات (سجل صادق)") {
            Text(
                "جاهز: " + FeatureRegistry.readyCount() + " من " + FeatureRegistry.plannedCount(),
                style = MaterialTheme.typography.titleSmall
            )
            FeatureRegistry.features.filter { !it.isReady }.forEach { f ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(f.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    StatusChip(label = f.phase, ready = false)
                }
                Text(
                    f.statusNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("حول التطبيق") }
        PhaseNote("التشفير عبر EncryptedSharedPreferences من AndroidX Security. النموذج العصبي المحلي قيد التكامل — المحرك المحلي الحالي قاعدي.")
    }
}
