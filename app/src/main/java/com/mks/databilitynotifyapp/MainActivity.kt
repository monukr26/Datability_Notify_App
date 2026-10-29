package com.mks.databilitynotifyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.mks.databilitynotifyapp.presentation.navigation.NavGraph
import com.mks.databilitynotifyapp.ui.theme.DatabilityNotifyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as DatabilityApp).container

        setContent {
            DatabilityNotifyAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }
}
