package com.example.utils

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ClassReminderTime {
    private val timePattern = Regex("^(0?[1-9]|1[0-2]):([0-5][0-9])(AM|PM)$")

    private val weekdays = mapOf(
        "lun" to Calendar.MONDAY,
        "mar" to Calendar.TUESDAY,
        "mie" to Calendar.WEDNESDAY,
        "jue" to Calendar.THURSDAY,
        "vie" to Calendar.FRIDAY,
        "sab" to Calendar.SATURDAY,
        "dom" to Calendar.SUNDAY
    )

    fun isValid(day: String, time: String, minutesBefore: Int): Boolean =
        weekday(day) != null && parseTime(time) != null && minutesBefore in 0..10080

    fun nextTriggerTimeMillis(
        day: String,
        time: String,
        minutesBefore: Int,
        nowMillis: Long
    ): Long? {
        val weekday = weekday(day) ?: return null
        val parsedTime = parseTime(time) ?: return null
        if (minutesBefore !in 0..10080) return null

        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val event = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_WEEK, weekday)
            set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (event.timeInMillis <= nowMillis) event.add(Calendar.DAY_OF_YEAR, 7)

        val trigger = event.timeInMillis - minutesBefore * 60_000L
        return maxOf(trigger, nowMillis)
    }

    private fun weekday(day: String): Int? {
        val normalized = day.trim().lowercase(Locale.forLanguageTag("es-CO"))
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
        return weekdays[normalized.take(3)]
    }

    private fun parseTime(value: String): Calendar? {
        val normalized = value.trim().uppercase(Locale.US).replace(" ", "")
        if (!timePattern.matches(normalized)) return null
        val formatter = SimpleDateFormat("h:mm a", Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        val parsed = formatter.parse(normalized.replace(Regex("(AM|PM)$"), " $1"), position)
            ?: return null
        if (position.index != normalized.length + 1) return null
        return Calendar.getInstance().apply { time = parsed }
    }
}
