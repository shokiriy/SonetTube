package com.shokirjon.sonettube.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shokirjon.sonettube.navigation.SonetTubeNavigation
import com.shokirjon.sonettube.ui.theme.SonetTubeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonetTubeTheme {
                SonetTubeNavigation()
            }
        }
    }
}
