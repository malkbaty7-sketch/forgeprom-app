package com.forgeprom.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.forgeprom.studio.core.settings.AppSettings
import com.forgeprom.studio.ui.navigation.AppNavHost
import com.forgeprom.studio.ui.theme.ForgepromTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ForgepromApp).container
        setContent {
            val settings by container.settings.settings.collectAsState(
                initial = AppSettings()
            )
            ForgepromTheme(themeMode = settings.themeMode) {
                // الواجهة عربية أولاً — اتجاه RTL دائم في هذه النسخة
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppNavHost(container = container)
                }
            }
        }
    }
}
