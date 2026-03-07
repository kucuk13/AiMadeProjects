package com.example.countdownapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main Room database for this application.  It holds a single table
 * of [Countdown] entities and exposes a [CountdownDao] for accessing them.
 * We configure the database as a singleton to prevent having multiple
 * instances open at the same time which could lead to conflicts.
 */
@Database(entities = [Countdown::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countdownDao(): CountdownDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the singleton instance of the database.  If no instance exists
         * a new one will be created.  Using the application context ensures
         * the database lives for the lifetime of the application.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "countdown_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}