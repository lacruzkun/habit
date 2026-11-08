package com.focusflow.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusflow.app.FocusFlowApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val pendingResult = goAsync()
        val container = (context.applicationContext as FocusFlowApp).container
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                container.reminderScheduler.sync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
