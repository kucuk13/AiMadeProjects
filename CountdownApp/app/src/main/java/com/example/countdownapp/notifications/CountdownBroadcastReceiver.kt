package com.example.countdownapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countdownapp.data.AppDatabase
import com.example.countdownapp.notifications.CountdownAlarmScheduler.Companion.EXTRA_COUNTDOWN_ID
import com.example.countdownapp.notifications.CountdownAlarmScheduler.Companion.EXTRA_COUNTDOWN_NAME
import com.example.countdownapp.notifications.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Broadcast receiver invoked by the alarm manager when a countdown reaches
 * its target date/time.  It displays a notification to the user and
 * disables further notifications for the countdown by clearing its flag
 * in the database.
 */
class CountdownBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val countdownId = intent.getIntExtra(EXTRA_COUNTDOWN_ID, -1)
        val countdownName = intent.getStringExtra(EXTRA_COUNTDOWN_NAME)
        if (countdownId == -1 || countdownName == null) return
        // Show the countdown completion notification
        Notifications.showCountdownNotification(context, "countdown_channel", countdownId, countdownName)
        // Update the countdown in the database to disable future notifications
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val dao = db.countdownDao()
            // Retrieve all countdowns once and update the matching entry
            val list = dao.getAllOnce()
            val existing = list.find { it.id == countdownId }
            if (existing != null && existing.notificationEnabled) {
                dao.update(existing.copy(notificationEnabled = false))
            }
        }
    }
}