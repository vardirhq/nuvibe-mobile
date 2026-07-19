package dev.nuvibe.player.ui.util

import java.util.Calendar

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/** "1 hr 18 min" style summary used for playlist descriptions. */
fun formatLongDuration(ms: Long): String {
    val totalMinutes = (ms / 1000 / 60).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "$hours hr ${"%02d".format(minutes)} min"
        else -> "$minutes min"
    }
}

fun greeting(now: Calendar = Calendar.getInstance()): String = when (now.get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning."
    in 12..16 -> "Good afternoon."
    in 17..21 -> "Good evening."
    else -> "Good night."
}
