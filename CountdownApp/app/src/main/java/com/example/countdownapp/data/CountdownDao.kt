package com.example.countdownapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for performing CRUD operations on countdown entities.
 * Room generates the necessary implementations for each annotated method.
 */
@Dao
interface CountdownDao {
    /**
     * Observe all countdowns in the database.  The returned Flow emits a new
     * list whenever any countdown is inserted, updated or deleted.  The
     * ordering of the list is left unspecified; sorting is handled in the
     * ViewModel according to the selected [SortMode].
     */
    @Query("SELECT * FROM countdown")
    fun getAll(): Flow<List<Countdown>>

    /**
     * Retrieve all countdowns once without observing future changes.  This
     * method is used by the boot receiver to reschedule alarms after
     * device reboot.  Since Room cannot return Flow types off the main
     * thread in a broadcast receiver easily, we provide a suspend version.
     */
    @Query("SELECT * FROM countdown")
    suspend fun getAllOnce(): List<Countdown>

    /**
     * Insert a new countdown into the database.  If a row with the same
     * primary key already exists it will be replaced.  The returned value is
     * the row ID of the newly inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(countdown: Countdown): Long

    /**
     * Update an existing countdown.  All fields will be replaced with the
     * contents of the provided entity.  The primary key must match an
     * existing row.
     */
    @Update
    suspend fun update(countdown: Countdown)

    /**
     * Delete the specified countdown from the database.
     */
    @Delete
    suspend fun delete(countdown: Countdown)
}