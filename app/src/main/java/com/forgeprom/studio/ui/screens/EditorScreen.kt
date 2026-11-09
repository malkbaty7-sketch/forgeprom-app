package com.forgeprom.studio.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.util.Exporter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * R3 — المحرر: تحرير موجه محفوظ مع عدادات وحفظ ونسخ وتصدير.
 */
@Composable
fun EditorScreen(container: AppContainer, promptId: Long, onDone: () -> Unit) {
    val prompt = remember(promptId) {
        runBlocking { container.db.promptDao().byId(promptId) }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (prompt == null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("الموجه غير موجود", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onDone) { Text("رجوع") }
        }
        return
    }

    var title by remember { mutableStateOf(prompt.title) }
    var content by remember { mutableStateOf(prompt.content) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان الموجه") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("نص الموجه") },
            minLines = 10,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "الكلمات: " + content.trim().split(Regex("\\s+")).count { it.isNotBlank() } +
                " • الأحرف: " + content.length,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    container.db.promptDao().update(
                        prompt.copy(title = title, content = content, updatedAt = System.currentTimeMillis())
                    )
                    container.logActivity("editor", "تعديل موجه: " + title)
                    Toast.makeText(context, "تم الحفظ", Toast.LENGTH_SHORT).show()
                    onDone()
                }
            }) { Text("حفظ") }
            OutlinedButton(onClick = {
                if (Exporter.copyToClipboard(context, content))
                    Toast.makeText(context, "تم النسخ إلى الحافظة", Toast.LENGTH_SHORT).show()
            }) { Text("نسخ") }
            OutlinedButton(onClick = {
                val res = Exporter.export(context, title, content, "md")
                res.onSuccess { uri -> Exporter.share(context, uri, "text/markdown", "مشاركة الموجه") }
                    .onFailure { Toast.makeText(context, "فشل التصدير", Toast.LENGTH_SHORT).show() }
            }) { Text("تصدير MD") }
        }
    }
}
