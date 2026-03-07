package com.example.countdownapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.countdownapp.data.AppDatabase
import com.example.countdownapp.data.CountdownRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver that listens for the BOOT_COMPLETED broadcast in order to
 * reschedule any active countdown alarms.  When the device restarts the
 * system clears all scheduled alarms; this class restores them using
 * information stored in the database.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Offload work to a coroutine running on the IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val repository = CountdownRepository(db.countdownDao())
                val scheduler = CountdownAlarmScheduler(context)
                val now = System.currentTimeMillis()
                val countdowns = repository.getAllOnce()
                countdowns.forEach { countdown ->
                    if (countdown.notificationEnabled && countdown.dateTime > now) {
                        scheduler.schedule(countdown)
                    }
                }
            }
        }
    }
}