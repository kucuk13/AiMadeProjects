package com.example.countdownapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.countdownapp.data.AppDatabase
import com.example.countdownapp.data.Countdown
import com.example.countdownapp.data.CountdownRepository
import com.example.countdownapp.notifications.CountdownAlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes countdown data, handles sorting, and manages
 * insertion, updating, deletion, and notification scheduling.
 */
class CountdownViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CountdownRepository
    private val scheduler: CountdownAlarmScheduler
    private val _sortMode = MutableStateFlow(SortMode.NEAREST)
    val sortMode: StateFlow<SortMode> = _sortMode
    private val _countdowns = MutableStateFlow<List<Countdown>>(emptyList())

    /** A stream of countdowns sorted according to the current sort mode. */
    val sortedCountdowns: StateFlow<List<Countdown>>

    init {
        val dao = AppDatabase.getDatabase(application).countdownDao()
        repository = CountdownRepository(dao)
        scheduler = CountdownAlarmScheduler(application.applicationContext)
        // Collect all countdowns from the database into the local state
        viewModelScope.launch {
            repository.countdowns.collect { list ->
                _countdowns.value = list
            }
        }
        // Combine local list and sort mode to produce sorted list
        sortedCountdowns = combine(_countdowns, _sortMode) { list, mode ->
            when (mode) {
                SortMode.NEAREST -> list.sortedBy { it.dateTime }
                SortMode.FARTHEST -> list.sortedByDescending { it.dateTime }
                SortMode.LAST_ADDED -> list.sortedByDescending { it.createdAt }
                SortMode.FIRST_ADDED -> list.sortedBy { it.createdAt }
                SortMode.A_TO_Z -> list.sortedBy { it.name.lowercase() }
                SortMode.Z_TO_A -> list.sortedByDescending { it.name.lowercase() }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Change the current sort mode. */
    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    /** Insert a new countdown and schedule its alarm if needed. */
    fun insertCountdown(name: String, dateTime: Long, imageUri: String?, notificationEnabled: Boolean) {
        viewModelScope.launch {
            val countdown = Countdown(
                name = name,
                dateTime = dateTime,
                imageUri = imageUri,
                createdAt = System.currentTimeMillis(),
                notificationEnabled = notificationEnabled
            )
            val id = repository.insert(countdown).toInt()
            // The ID returned by insert applies to the object stored in the database.
            val saved = countdown.copy(id = id)
            if (notificationEnabled) {
                scheduler.schedule(saved)
            }
        }
    }

    /** Update an existing countdown and reschedule or cancel its alarm accordingly. */
    fun updateCountdown(countdown: Countdown) {
        viewModelScope.launch {
            repository.update(countdown)
            // Cancel any existing alarm for this countdown
            scheduler.cancel(countdown)
            // Schedule a new alarm if notifications remain enabled and the date is still in the future
            if (countdown.notificationEnabled) {
                scheduler.schedule(countdown)
            }
        }
    }

    /** Toggle the notification state for a countdown. */
    fun toggleNotification(countdown: Countdown) {
        val updated = countdown.copy(notificationEnabled = !countdown.notificationEnabled)
        updateCountdown(updated)
    }

    /** Delete a countdown and cancel its alarm. */
    fun deleteCountdown(countdown: Countdown) {
        viewModelScope.launch {
            scheduler.cancel(countdown)
            repository.delete(countdown)
        }
    }
}