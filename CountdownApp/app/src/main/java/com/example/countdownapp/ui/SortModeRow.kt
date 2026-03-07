package com.example.countdownapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A horizontally scrollable row of chips allowing the user to select the
 * sorting order for the countdown list.  The currently selected sort mode
 * is highlighted.  Clicking a chip invokes [onSortSelected] with the
 * corresponding [SortMode].
 */
@Composable
fun SortModeRow(sortMode: SortMode, onSortSelected: (SortMode) -> Unit) {
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        items(SortMode.values()) { mode ->
            FilterChip(
                selected = sortMode == mode,
                onClick = { onSortSelected(mode) },
                label = { Text(text = mode.display) },
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = ChipDefaults.filterChipColors()
            )
        }
    }
}