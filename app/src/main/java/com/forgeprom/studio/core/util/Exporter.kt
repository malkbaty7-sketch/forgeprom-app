package com.forgeprom.studio.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * R11 — التصدير: TXT (UTF-8) وMarkdown وJSON عبر مجلد مشترك + مشاركة + نسخ للحافظة.
 * أسماء ملفات عربية آمنة تُستبدل مسافاتها بشرطات.
 */
object Exporter {

    fun export(context: Context, baseName: String, content: String, ext: String): Result<Uri> {
        return runCatching {
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val safe = baseName.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "-").take(50).ifBlank { "prompt" }
            val file = File(dir, safe + "-" + System.currentTimeMillis() + "." + ext)
            file.writeText(content, Charsets.UTF_8)
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        }
    }

    fun share(context: Context, uri: Uri, mime: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun copyToClipboard(context: Context, text: String): Boolean = runCatching {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("Forgeprom", text))
        true
    }.getOrDefault(false)

    fun toJson(promptText: String, draftJson: String): String {
        return org.json.JSONObject()
            .put("app", "Forgeprom Code Studio")
            .put("version", "1.0.0-mvp")
            .put("prompt", promptText)
            .put("analysis", org.json.JSONObject(draftJson))
            .put("exportedAt", java.time.OffsetDateTime.now().toString())
            .toString(2)
    }
}
