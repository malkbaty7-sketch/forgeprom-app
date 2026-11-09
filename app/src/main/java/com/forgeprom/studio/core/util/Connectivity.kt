package com.forgeprom.studio.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * فحص الاتصال — للمؤشرات الصادقة فقط.
 * لا تعتمد أي وظيفة أساسية على الشبكة (سياسة عدم الاتصال).
 */
object Connectivity {
    fun isOnline(context: Context): Boolean = runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return@runCatching false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(false)
}
