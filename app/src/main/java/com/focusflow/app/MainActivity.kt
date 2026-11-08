package com.focusflow.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.focusflow.app.ui.AppRoot

class MainActivity : ComponentActivity() {
    private var openTimerSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consumeIntent(intent)
        setContent {
            AppRoot(
                container = (application as FocusFlowApp).container,
                openTimerSignal = openTimerSignal,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
    }

    private fun consumeIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_TIMER, false) == true) {
            openTimerSignal += 1
            intent.removeExtra(EXTRA_OPEN_TIMER)
        }
    }

    companion object {
        const val EXTRA_OPEN_TIMER = "extra_open_timer"
    }
}
