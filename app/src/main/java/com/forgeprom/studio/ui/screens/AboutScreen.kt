package com.forgeprom.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.registry.FeatureRegistry
import com.forgeprom.studio.ui.components.StatusChip

/**
 * حول التطبيق — سجل الميزات الكامل بحالته الصادقة.
 */
@Composable
fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Forgeprom Code Studio", style = MaterialTheme.typography.headlineSmall)
        Text(
            "الإصدار 1.0.0-mvp (v2)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "استوديو عربي لصياغة موجزات هندسية احترافية من أفكارك — محلي أولاً، سحابي بموافقتك.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            "سجل الميزات (" + FeatureRegistry.readyCount() + "/" + FeatureRegistry.plannedCount() + " جاهزة)",
            style = MaterialTheme.typography.titleMedium
        )
        FeatureRegistry.features.forEach { f ->
            Card(Modifier.padding(0.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(f.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        StatusChip(label = f.phase, ready = f.isReady)
                    }
                    Text(f.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(f.statusNote, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text(
            "© 2026 Forgeprom — يعمل دون إنترنت بالمحرك القاعدي المحلي.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
