package com.forgeprom.studio

import android.app.Application
import com.forgeprom.studio.core.AppContainer

/**
 * التطبيق — ينشئ الحاوية المركزية (حقن يدوي) ويسجل الحالة.
 */
class ForgepromApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
