package com.example.countdownapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.countdownapp.data.Countdown
import com.example.countdownapp.ui.SortMode
import com.example.countdownapp.ui.theme.CountdownAppTheme
import com.example.countdownapp.ui.*
import com.example.countdownapp.notifications.Notifications
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.pm.PackageManager

/**
 * The main activity sets up the Compose UI and wires together the ViewModel and screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the notification channel so alarms can post notifications
        Notifications.createChannel(this, "countdown_channel")
        // Request notification permission on Android 13+ if not yet granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
        setContent {
            CountdownAppTheme {
                val viewModel: CountdownViewModel = viewModel()
                val sortMode by viewModel.sortMode.collectAsState()
                val countdowns by viewModel.sortedCountdowns.collectAsState()
                var showDialog by remember { mutableStateOf(false) }
                var editingCountdown by remember { mutableStateOf<Countdown?>(null) }

                if (showDialog) {
                    AddEditCountdownDialog(
                        initial = editingCountdown,
                        onDismiss = {
                            showDialog = false
                            editingCountdown = null
                        },
                        onSave = { name, dateTime, imageUri, notificationEnabled, id ->
                            if (id == null) {
                                viewModel.insertCountdown(name, dateTime, imageUri, notificationEnabled)
                            } else {
                                // We need createdAt for update. Retrieve existing countdown's createdAt if editing.
                                val existing = countdowns.find { it.id == id }
                                val createdAt = existing?.createdAt ?: System.currentTimeMillis()
                                viewModel.updateCountdown(
                                    Countdown(
                                        id = id,
                                        name = name,
                                        dateTime = dateTime,
                                        imageUri = imageUri,
                                        createdAt = createdAt,
                                        notificationEnabled = notificationEnabled
                                    )
                                )
                            }
                            showDialog = false
                            editingCountdown = null
                        },
                        onDelete = { countdown ->
                            viewModel.deleteCountdown(countdown)
                            showDialog = false
                            editingCountdown = null
                        }
                    )
                }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            editingCountdown = null
                            showDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Sayaç Ekle")
                        }
                    }
                ) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        // Sort mode row
                        SortModeRow(sortMode = sortMode, onSortSelected = { viewModel.setSortMode(it) })
                        // List of countdowns
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(countdowns) { countdown ->
                                CountdownItem(
                                    countdown = countdown,
                                    onClick = {
                                        editingCountdown = countdown
                                        showDialog = true
                                    },
                                    onToggleNotification = {
                                        viewModel.toggleNotification(countdown)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}