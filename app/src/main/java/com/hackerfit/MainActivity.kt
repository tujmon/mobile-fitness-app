package com.hackerfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hackerfit.ui.navigation.HackerFitNavHost
import com.hackerfit.ui.navigation.MainViewModel
import com.hackerfit.ui.theme.HackerFitTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleViewIntent(intent)
        setContent {
            HackerFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HackerFitNavHost(mainViewModel = mainViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleViewIntent(intent)
    }

    private fun handleViewIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                mainViewModel.setPendingImportUri(uri.toString())
            }
        }
    }
}
