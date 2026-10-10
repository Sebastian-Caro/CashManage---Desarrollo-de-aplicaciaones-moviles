package com.example

import com.example.utils.ClassReminderTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ClassReminderTimeTest {
    @Test
    fun `computes the next weekly trigger including minutes before the class`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 5, 7, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expected = (now.clone() as Calendar).apply { set(Calendar.MINUTE, 45) }

        assertEquals(
            expected.timeInMillis,
            ClassReminderTime.nextTriggerTimeMillis("Lunes", "08:00 AM", 15, now.timeInMillis)
        )
    }

    @Test
    fun `schedules immediately when the alert lead time has already passed`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 5, 7, 50, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(
            now.timeInMillis,
            ClassReminderTime.nextTriggerTimeMillis("Lunes", "08:00 AM", 15, now.timeInMillis)
        )
    }

    @Test
    fun `rejects invalid weekdays times and lead intervals`() {
        assertFalse(ClassReminderTime.isValid("Funday", "08:00 AM", 15))
        assertFalse(ClassReminderTime.isValid("Lunes", "13:00 PM", 15))
        assertFalse(ClassReminderTime.isValid("Lunes", "08:00 AM", -1))
        assertNull(ClassReminderTime.nextTriggerTimeMillis("Lunes", "08:00 PM!", 15, 0))
    }

    @Test
    fun `advances to next week when the class time has passed`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 5, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expected = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 7)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 45)
        }

        assertEquals(
            expected.timeInMillis,
            ClassReminderTime.nextTriggerTimeMillis("Lunes", "08:00 AM", 15, now.timeInMillis)
        )
    }
}
