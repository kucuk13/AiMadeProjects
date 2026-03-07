package com.example.countdownapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.countdownapp.data.Countdown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialog that allows the user to create a new countdown or edit an existing
 * one.  The dialog contains fields for the name, date/time, optional photo
 * and notification toggle.  When the user confirms, [onSave] is called
 * with the current values; when cancelled [onDismiss] is invoked.  A
 * delete button is shown when editing existing items.
 */
@Composable
fun AddEditCountdownDialog(
    initial: Countdown?,
    onDismiss: () -> Unit,
    onSave: (name: String, dateTime: Long, imageUri: String?, notificationEnabled: Boolean, id: Int?) -> Unit,
    onDelete: (Countdown) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var dateTime by remember { mutableStateOf(initial?.dateTime ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)) }
    var imageUri by remember { mutableStateOf(initial?.imageUri) }
    var notificationEnabled by remember { mutableStateOf(initial?.notificationEnabled ?: true) }

    // Launcher for the image picker.  Stores the returned URI as a string.
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    fun openDateTimePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateTime }
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val tempCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, hour, minute)
                }
                // Once the date is selected, show a time picker
                val timePicker = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        tempCal.set(Calendar.HOUR_OF_DAY, selectedHour)
                        tempCal.set(Calendar.MINUTE, selectedMinute)
                        dateTime = tempCal.timeInMillis
                    },
                    hour,
                    minute,
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000L
        datePicker.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (initial == null) "Yeni Sayaç" else "Sayaç Düzenle")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("İsim") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Date/time picker row
                OutlinedButton(
                    onClick = { openDateTimePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = formatDateTime(dateTime))
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Image picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!imageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Seçilen fotoğraf",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = ImageIcon,
                            contentDescription = "Fotoğraf Seç",
                            modifier = Modifier
                                .size(64.dp)
                                .padding(8.dp)
                                .clickable { pickImageLauncher.launch("image/*") }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { pickImageLauncher.launch("image/*") }) {
                        Text(text = if (imageUri.isNullOrEmpty()) "Fotoğraf Ekle" else "Fotoğraf Değiştir")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Notification toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Bildirim", modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (initial != null) {
                    IconButton(onClick = { onDelete(initial) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Sil")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
                Button(
                    onClick = {
                        onSave(name, dateTime, imageUri, notificationEnabled, initial?.id)
                    },
                    enabled = name.isNotBlank() && dateTime > System.currentTimeMillis()
                ) {
                    Text("Kaydet")
                }
            }
        }
    )
}

private fun formatDateTime(millis: Long): String {
    val date = Date(millis)
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}