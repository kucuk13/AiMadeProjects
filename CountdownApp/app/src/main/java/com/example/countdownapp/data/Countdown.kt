package com.example.countdownapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model representing a single countdown entry.  Each countdown holds a
 * user-provided name, the target date/time in milliseconds since the epoch,
 * an optional URI string pointing to a photo selected by the user, the time
 * at which the countdown was created, and a flag indicating whether a
 * notification should be delivered when the countdown reaches zero.
 */
@Entity(tableName = "countdown")
data class Countdown(
    /**
     * Unique identifier for the countdown.  Room will auto-generate this
     * primary key when inserting new rows into the database.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * Display name for the countdown supplied by the user.
     */
    @ColumnInfo(name = "name")
    val name: String,
    /**
     * The future timestamp (milliseconds since epoch) at which the countdown
     * should complete.  This value is used to schedule alarms and to compute
     * the remaining time in the UI.
     */
    @ColumnInfo(name = "date_time")
    val dateTime: Long,
    /**
     * Optional URI string referencing an image.  When null or empty the UI
     * should display a default placeholder image.  We store the URI as a
     * String because Parcelables cannot directly persist complex types in
     * Room.  The actual URI is reconstructed as needed.
     */
    @ColumnInfo(name = "image_uri")
    val imageUri: String?,
    /**
     * Timestamp (milliseconds since epoch) representing when the countdown
     * record was created.  Used for sorting by creation date.
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /**
     * Whether notifications are currently enabled for this countdown.  When
     * true an exact alarm will be scheduled; when false any existing alarm
     * should be cancelled.  Once the alarm fires this flag should be cleared.
     */
    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean
)