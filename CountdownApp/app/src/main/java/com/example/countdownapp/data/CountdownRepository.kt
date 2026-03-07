package com.example.countdownapp.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to the underlying data source.  By
 * delegating all Room operations through this layer we make it easier to
 * swap out the persistence mechanism in the future and to test the
 * ViewModel in isolation.
 */
class CountdownRepository(private val dao: CountdownDao) {
    /** A Flow emitting all countdowns whenever the data changes. */
    val countdowns: Flow<List<Countdown>> = dao.getAll()

    /**
     * Insert a new countdown.  Returns the generated row ID.
     */
    suspend fun insert(countdown: Countdown): Long = dao.insert(countdown)

    /** Update an existing countdown. */
    suspend fun update(countdown: Countdown) = dao.update(countdown)

    /** Delete a countdown. */
    suspend fun delete(countdown: Countdown) = dao.delete(countdown)

    /**
     * Retrieve all countdowns once, without observing further changes.  This
     * method is primarily used by the boot receiver.
     */
    suspend fun getAllOnce(): List<Countdown> = dao.getAllOnce()
}