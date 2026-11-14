package com.foodhealth.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// One tab in the diary's "last 7 days" strip.
data class DayChip(val key: String, val weekday: String, val dayNumber: String)

// Meals are filed under a "dateKey" like "2026-11-14" (the user's LOCAL date),
// so the diary can ask Firestore for one day with a simple equality query.
// Uses Calendar/SimpleDateFormat because java.time needs API 26 and minSdk is 24.
object DateKeys {
    private const val PATTERN = "yyyy-MM-dd"

    private fun localFormat() = SimpleDateFormat(PATTERN, Locale.US)
    private fun utcFormat() = SimpleDateFormat(PATTERN, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun keyFor(millis: Long): String = localFormat().format(Date(millis))
    fun today(): String = keyFor(System.currentTimeMillis())

    // The Material date picker works in UTC midnight milliseconds.
    fun keyFromUtcMillis(millis: Long): String = utcFormat().format(Date(millis))
    fun utcMillisFor(key: String): Long? = runCatching { utcFormat().parse(key)?.time }.getOrNull()

    // Oldest first, ending with today.
    fun lastDays(count: Int): List<DayChip> {
        val weekday = SimpleDateFormat("EEE", Locale.getDefault())
        return (count - 1 downTo 0).map { ago ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -ago) }
            DayChip(
                key = localFormat().format(cal.time),
                weekday = weekday.format(cal.time),
                dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString()
            )
        }
    }

    fun label(key: String): String {
        if (key == today()) return "Today"
        val date = runCatching { localFormat().parse(key) }.getOrNull() ?: return key
        return SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(date)
    }

    fun timeLabel(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
}