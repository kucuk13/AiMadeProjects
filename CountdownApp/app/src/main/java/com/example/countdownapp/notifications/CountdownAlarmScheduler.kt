package com.example.countdownapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.countdownapp.data.Countdown

/**
 * Helper class responsible for scheduling and cancelling exact alarms for
 * countdowns.  Each countdown alarm triggers a broadcast to
 * [CountdownBroadcastReceiver] at the specified date/time.  The receiver
 * subsequently shows a notification and updates the database.
 */
class CountdownAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule an exact alarm for the given countdown if its target time lies
     * in the future and notifications are enabled.  The alarm uses the
     * countdown's ID as the request code so it can be uniquely identified.
     */
    fun schedule(countdown: Countdown) {
        if (!countdown.notificationEnabled) return
        val now = System.currentTimeMillis()
        if (countdown.dateTime <= now) return
        val intent = Intent(context, CountdownBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_COUNTDOWN_ID, countdown.id)
            putExtra(EXTRA_COUNTDOWN_NAME, countdown.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            countdown.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingFlagImmutable()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                countdown.dateTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                countdown.dateTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancel any previously scheduled alarm for the given countdown.  If no
     * alarm exists this call has no effect.
     */
    fun cancel(countdown: Countdown) {
        val intent = Intent(context, CountdownBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            countdown.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingFlagImmutable()
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun pendingFlagImmutable(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    companion object {
        const val EXTRA_COUNTDOWN_ID = "extra_countdown_id"
        const val EXTRA_COUNTDOWN_NAME = "extra_countdown_name"
    }
}