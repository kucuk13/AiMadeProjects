package com.example.countdownapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Utility class that encapsulates common notification operations such as
 * creating channels and posting notifications.  All notifications posted by
 * this app share the same channel ID defined in [MainActivity].
 */
object Notifications {
    /**
     * Create the notification channel used for countdown alerts.  This method
     * is a no-op on API levels below 26 since channels are not supported.
     */
    fun createChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Geri Sayım Bildirimleri"
            val description = "Geri sayım tamamlandığında gösterilen bildirimler"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Display a countdown completion notification.  The `id` parameter is
     * reused as the notification ID so that multiple notifications do not
     * overwrite each other.  Content is localized to Turkish.
     */
    fun showCountdownNotification(context: Context, channelId: String, id: Int, countdownName: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Geri Sayım Tamamlandı")
            .setContentText("$countdownName gerçekleşti.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id, builder.build())
    }
}