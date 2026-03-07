package com.example.countdownapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.countdownapp.data.Countdown
import kotlinx.coroutines.delay

/**
 * Composable that displays an individual countdown with its image, name,
 * remaining time and a notification toggle.  The row is clickable to open
 * the edit dialog and the notification icon can be tapped independently.
 */
@Composable
fun CountdownItem(
    countdown: Countdown,
    onClick: () -> Unit,
    onToggleNotification: () -> Unit
) {
    // Maintain a state that updates every second to recompute remaining time
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = System.currentTimeMillis()
        }
    }
    // Compute remaining time and format it for display
    val diff = countdown.dateTime - now
    val timeText = remember(diff) { formatRemaining(diff) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // Show either the selected image or a default icon
            if (!countdown.imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = countdown.imageUri,
                    contentDescription = "Sayaç Fotoğrafı",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = ImageIcon,
                    contentDescription = "Varsayılan Fotoğraf",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = countdown.name,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.body2
                )
            }
            IconButton(onClick = onToggleNotification) {
                Icon(
                    imageVector = if (countdown.notificationEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    contentDescription = if (countdown.notificationEnabled) "Bildirim kapat" else "Bildirim aç"
                )
            }
        }
    }
}

/**
 * Format the difference between the target time and the current time into a
 * human-readable string.  Positive values represent the remaining time
 * until the event; negative values cause the string "Tamamlandı" to be
 * returned.  The breakdown uses days, hours, minutes and seconds.
 */
private fun formatRemaining(diffMillis: Long): String {
    if (diffMillis <= 0L) return "Tamamlandı"
    val totalSeconds = diffMillis / 1000
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return when {
        days > 0 -> String.format("%dg %dsa", days, hours)
        hours > 0 -> String.format("%dsa %ddk", hours, minutes)
        minutes > 0 -> String.format("%ddk %dsn", minutes, seconds)
        else -> String.format("%dsn", seconds)
    }
}