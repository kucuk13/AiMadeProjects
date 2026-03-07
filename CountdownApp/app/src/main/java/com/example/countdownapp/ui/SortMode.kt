package com.example.countdownapp.ui

/**
 * Enum representing the available sort orders for countdown items. The display
 * values are in Turkish because the target audience uses that locale.
 */
enum class SortMode(val display: String) {
    /** En yakın: show the countdown that is nearest in time at the top. */
    NEAREST("En Yakın"),
    /** En uzak: show the countdown that is farthest in the future at the top. */
    FARTHEST("En Uzak"),
    /** Son eklenen: show most recently added items first. */
    LAST_ADDED("Son Eklenen"),
    /** İlk eklenen: show first added items first. */
    FIRST_ADDED("İlk Eklenen"),
    /** A-Z: alphabetical ascending. */
    A_TO_Z("A-Z"),
    /** Z-A: alphabetical descending. */
    Z_TO_A("Z-A")
}