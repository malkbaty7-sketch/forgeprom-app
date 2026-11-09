package com.forgeprom.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.registry.FeatureRegistry
import com.forgeprom.studio.ui.components.QuickActionCard
import com.forgeprom.studio.ui.components.SectionCard
import com.forgeprom.studio.ui.components.StatusChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * الرئيسية — إجراءات سريعة + آخر النشاطات + حالة الميزات الصادقة.
 */
@Composable
fun DashboardScreen(
    container: AppContainer,
    onCreate: () -> Unit,
    onProjects: () -> Unit,
    onImage: () -> Unit,
    onAbout: () -> Unit
) {
    val activities by container.db.activityDao().recent(10).collectAsState(initial = emptyList())
    val fmt = SimpleDateFormat("d MMM yyyy، HH:mm", Locale.getDefault())

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Forgeprom Code Studio", style = MaterialTheme.typography.headlineSmall)
        Text(
            "حوّل فكرتك إلى موجه (برومبت) هندسي احترافي — جاهز للنسخ في منصات بناء الكود.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        QuickActionCard(
            icon = Icons.Filled.Create,
            title = "إنشاء موجه جديد",
            subtitle = "وصف حر بالعربية → تحليل → برومبت جاهز",
            onClick = onCreate
        )
        QuickActionCard(
            icon = Icons.Filled.List,
            title = "المشاريع",
            subtitle = "إدارة الموجهات والمشاريع المحفوظة محلياً",
            onClick = onProjects
        )
        QuickActionCard(
            icon = Icons.Filled.Create,
            title = "تحليل صورة واجهة",
            subtitle = "سحابي اختياري عبر Gemini بموافقتك — للاستلهام فقط",
            onClick = onImage
        )

        SectionCard("آخر النشاطات") {
            if (activities.isEmpty()) {
                Text("لا نشاطات بعد — ابدأ بإنشاء موجه جديد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                activities.forEach { a ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(
                            (when (a.type) {
                                "analyze" -> "تحليل"
                                "generate" -> "توليد"
                                "save" -> "حفظ"
                                "export" -> "تصدير"
                                "image" -> "تحليل صورة"
                                else -> a.type
                            }) + ": " + a.detail.take(60),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            fmt.format(Date(a.at)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        SectionCard("حالة البناء (صادقة)") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusChip(
                    "جاهز: " + FeatureRegistry.readyCount() + " ميزة — منها المحرك المحلي دون إنترنت",
                    ready = true
                )
                StatusChip("قيد التكامل: النموذج العصبي على الجهاز (المحرك المحلي الحالي قاعدي)", ready = false)
                StatusChip("تحليل الصور: سحابي اختياري بموافقة — لا ادعاء عمل محلي", ready = false)
                Text(
                    "اضغط للاطلاع على القائمة الكاملة في «حول التطبيق» من الإعدادات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        androidx.compose.material3.OutlinedButton(onClick = onAbout) {
            Text("حول التطبيق وسجل الميزات")
        }
    }
}
