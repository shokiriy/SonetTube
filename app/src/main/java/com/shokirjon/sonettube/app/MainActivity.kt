package com.shokirjon.sonettube.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.shokirjon.sonettube.navigation.SonetTubeNavigation
import com.shokirjon.sonettube.ui.theme.SonetTubeTheme

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SonetTubeApp
        setContent {
            CompositionLocalProvider(LocalAppContainer provides app.container) {
                SonetTubeTheme {
                    SonetTubeNavigation()
                }
            }
        }
    }
}
