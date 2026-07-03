package com.ipcheck.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ipcheck.app.ui.LiquidGlassBackground
import com.ipcheck.app.ui.screens.HistoryScreen
import com.ipcheck.app.ui.screens.HomeScreen
import com.ipcheck.app.ui.theme.IPCheckTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            IPCheckTheme {
                var showHistory by remember { mutableStateOf(false) }
                LiquidGlassBackground {
                    if (showHistory) {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { showHistory = false }
                        )
                    } else {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenHistory = { showHistory = true }
                        )
                    }
                }
            }
        }
    }
}
