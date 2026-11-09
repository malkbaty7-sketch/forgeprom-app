package com.forgeprom.studio.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.ai.ImageAnalysisResult
import com.forgeprom.studio.core.registry.FeatureRegistry
import com.forgeprom.studio.core.settings.AppSettings
import com.forgeprom.studio.core.util.Connectivity
import com.forgeprom.studio.ui.components.PhaseNote
import com.forgeprom.studio.ui.components.SectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * R8 — تحليل صورة واجهة (سحابي اختياري عبر Gemini بموافقة صريحة).
 * مبدأ الصدق: التحليل المحلي للصور غير متاح — يُعلن ذلك بوضوح.
 */
@Composable
fun ImageScreen(container: AppContainer, onUseAnalysis: () -> Unit) {
    val settings by container.settings.settings.collectAsState(initial = AppSettings())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var legalOnly by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<ImageAnalysisResult?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        result = null
        error = null
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("تحليل صورة واجهة", style = MaterialTheme.typography.headlineSmall)
        Text(
            "حوّل صورة تصميم إلى وصف بنيوي دقيق يُدمج في البرومبت.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(onClick = { picker.launch("image/*") }) {
            Text(if (imageUri == null) "اختيار صورة" else "تغيير الصورة")
        }
        if (imageUri != null) Text("تم اختيار صورة ✓", style = MaterialTheme.typography.bodySmall)

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = legalOnly, onCheckedChange = { legalOnly = it })
            Text("ألتزم أن الاستخدام استلهام فقط ولا أنسخ تصميباً محمياً", style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                error = null
                result = null
                val uri = imageUri
                if (uri == null) { error = "اختر صورة أولاً"; return@Button }
                if (!legalOnly) { error = "يجب الإقرار بأن الاستخدام استلهام فقط"; return@Button }
                if (!settings.cloudConsent) {
                    error = "موافقة السحابة غير ممنوحة — فعّلها من الإعدادات أولاً (لا يُرسل شيء دون موافقة)"
                    return@Button
                }
                if (!Connectivity.isOnline(context)) { error = "لا اتصال بالإنترنت — التحليل سحابي"; return@Button }
                if (container.apiKeys.geminiKey().isBlank()) { error = "أدخل مفتاح Gemini من الإعدادات"; return@Button }

                busy = true
                scope.launch {
                    val res = withContext(Dispatchers.IO) {
                        try {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: throw IllegalStateException("تعذر قراءة الصورة")
                            if (bytes.size > 5 * 1024 * 1024)
                                throw IllegalArgumentException("حجم الصورة يتجاوز 5MB")
                            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                            container.engines.engineById("cloud.gemini").analyzeImage(bytes, mime, note)
                        } catch (e: Exception) {
                            Result.failure<ImageAnalysisResult>(e)
                        }
                    }
                    busy = false
                    res.fold(
                        onSuccess = { r ->
                            result = r
                            container.logActivity("image", "تحليل صورة عبر Gemini — ثقة " + (r.confidence * 100).toInt() + "%")
                        },
                        onFailure = { e -> error = e.message ?: "فشل تحليل الصورة" }
                    )
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "جارٍ التحليل…" else "تحليل الصورة عبر Gemini") }

        if (busy) CircularProgressIndicator()
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        result?.let { r ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("نوع الشاشة: " + r.screenType, style = MaterialTheme.typography.titleSmall)
                    Text("المكونات: " + r.components.joinToString("، "))
                    Text("الألوان: " + r.colors.entries.joinToString("، ") { it.key + "=" + it.value })
                    Text("التخطيط: " + r.layout)
                    Text("الحالات: " + r.states.joinToString("، "))
                    if (r.assumptions.isNotEmpty())
                        Text("افتراضات: " + r.assumptions.joinToString("، "))
                    Text(
                        "الثقة: " + (r.confidence * 100).toInt() + "%",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = {
                    val s = container.session
                    s.clear()
                    s.isImageInspired = true
                    s.sourceInstruction = note.ifBlank { "واجهة مستوحاة من صورة: " + r.screenType }
                    s.goal = "بناء واجهة مستوحاة من صورة (استلهام فقط)"
                    s.tech = ""
                    s.imageAnalysis = r
                    s.draft = com.forgeprom.studio.domain.IdeaAnalyzer.analyze(
                        s.sourceInstruction, s.goal, s.tech
                    )
                    onUseAnalysis()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("اعتماد التحليل وفتح شاشة التحليل") }
        }

        val f = FeatureRegistry.byId("IMAGE.ANALYZE")
        SectionCard("حالة الميزة (مبدأ الصدق)") {
            Text(f?.name ?: "تحليل الصور")
            Text(
                f?.statusNote ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PhaseNote("التحليل السحابي يرسل الصورة إلى Google Gemini فقط بعد موافقتك الصريحة. لا تحليل محلي للصور في هذه النسخة.")
    }
}
