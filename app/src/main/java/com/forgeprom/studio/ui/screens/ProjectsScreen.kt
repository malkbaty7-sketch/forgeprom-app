package com.forgeprom.studio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeprom.studio.core.AppContainer
import com.forgeprom.studio.core.data.ProjectEntity
import kotlinx.coroutines.launch

/**
 * R2 — إدارة المشاريع: إنشاء، تثبيت، أرشفة، حذف، فتح التفاصيل.
 */
@Composable
fun ProjectsScreen(container: AppContainer, onOpen: (Long) -> Unit) {
    val projects by container.db.projectDao().activeProjects().collectAsState(initial = emptyList())
    val archived by container.db.projectDao().archivedProjects().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showCreate by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المشاريع", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" جديد")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (projects.isEmpty()) {
                item {
                    Text(
                        "لا مشاريع بعد. أنشئ مشروعاً أو احفظ موجهاً من شاشة النتيجة.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(projects, key = { it.id }) { p ->
                ProjectRow(
                    project = p,
                    onOpen = { onOpen(p.id) },
                    onPin = {
                        scope.launch {
                            container.db.projectDao().setPinned(p.id, !p.isPinned, System.currentTimeMillis())
                        }
                    },
                    onArchive = {
                        scope.launch {
                            container.db.projectDao().setArchived(p.id, true, System.currentTimeMillis())
                        }
                    },
                    onDelete = {
                        scope.launch { container.db.projectDao().delete(p.id) }
                    }
                )
            }
            if (archived.isNotEmpty()) {
                item {
                    Text(
                        "المؤرشفة (" + archived.size + ")",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                items(archived, key = { it.id }) { p ->
                    ProjectRow(
                        project = p,
                        onOpen = { onOpen(p.id) },
                        onPin = {},
                        onArchive = {
                            scope.launch {
                                container.db.projectDao().setArchived(p.id, false, System.currentTimeMillis())
                            }
                        },
                        onDelete = {
                            scope.launch { container.db.projectDao().delete(p.id) }
                        }
                    )
                }
            }
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("مشروع جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("وصف (اختياري)") })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            scope.launch {
                                container.db.projectDao().insert(ProjectEntity(name = name, description = desc))
                                container.logActivity("project", "إنشاء مشروع: " + name)
                            }
                        }
                        showCreate = false
                    },
                    enabled = name.isNotBlank()
                ) { Text("إنشاء") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun ProjectRow(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    (if (project.isPinned) "📌 " else "") + project.name,
                    style = MaterialTheme.typography.titleSmall
                )
                if (project.description.isNotBlank()) {
                    Text(
                        project.description.take(80),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onPin) { Icon(Icons.Filled.Star, contentDescription = "تثبيت") }
            IconButton(onClick = onArchive) { Icon(Icons.Filled.Archive, contentDescription = "أرشفة") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "حذف") }
        }
    }
}
