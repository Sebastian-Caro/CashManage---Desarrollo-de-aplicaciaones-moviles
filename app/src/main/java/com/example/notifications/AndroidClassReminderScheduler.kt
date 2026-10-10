package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.Recordatorio
import com.example.utils.ClassReminderTime

interface ClassReminderScheduler {
    fun schedule(recordatorio: Recordatorio): Boolean
    fun cancel(id: Long)
    fun replaceAll(recordatorios: List<Recordatorio>)
    fun cancelAll()
}

class AndroidClassReminderScheduler(context: Context) : ClassReminderScheduler {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun schedule(recordatorio: Recordatorio): Boolean {
        if (!recordatorio.activo || recordatorio.notificarMinutosAntes < 0) {
            cancel(recordatorio.id)
            return true
        }
        val triggerAt = ClassReminderTime.nextTriggerTimeMillis(
            recordatorio.diaSemana,
            recordatorio.horaInicio,
            recordatorio.notificarMinutosAntes,
            System.currentTimeMillis()
        )
        if (triggerAt == null) {
            Log.e(TAG, "No se pudo programar el recordatorio ${recordatorio.id}: día u hora inválidos.")
            return false
        }

        val alarmManager = appContext.getSystemService(AlarmManager::class.java)
        if (alarmManager == null) {
            Log.e(TAG, "No se pudo programar el recordatorio: AlarmManager no está disponible.")
            return false
        }
        val pendingIntent = pendingIntent(recordatorio)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
            saveReminderId(recordatorio.id)
            true
        } catch (exception: SecurityException) {
            Log.e(TAG, "Android rechazó programar el recordatorio ${recordatorio.id}.", exception)
            false
        }
    }

    override fun cancel(id: Long) {
        if (id <= 0) return
        val alarmManager = appContext.getSystemService(AlarmManager::class.java)
            ?: error("AlarmManager no está disponible.")
        val intent = Intent(appContext, ClassReminderReceiver::class.java).apply {
            action = actionFor(id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        val ids = reminderIds().filterNot { it == id }
        preferences.edit().putStringSet(KEY_REMINDER_IDS, ids.map(Long::toString).toSet()).apply()
    }

    override fun replaceAll(recordatorios: List<Recordatorio>) {
        reminderIds().forEach(::cancel)
        recordatorios.filter { it.activo }.forEach(::schedule)
    }

    override fun cancelAll() {
        reminderIds().forEach(::cancel)
    }

    private fun pendingIntent(recordatorio: Recordatorio): PendingIntent {
        val intent = Intent(appContext, ClassReminderReceiver::class.java).apply {
            action = actionFor(recordatorio.id)
            putExtra(EXTRA_ID, recordatorio.id)
            putExtra(EXTRA_TITLE, recordatorio.titulo)
            putExtra(EXTRA_DAY, recordatorio.diaSemana)
            putExtra(EXTRA_TIME, recordatorio.horaInicio)
            putExtra(EXTRA_MINUTES_BEFORE, recordatorio.notificarMinutosAntes)
            putExtra(EXTRA_LOCATION, recordatorio.aula)
        }
        return PendingIntent.getBroadcast(
            appContext,
            recordatorio.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun saveReminderId(id: Long) {
        val ids = reminderIds() + id
        preferences.edit().putStringSet(KEY_REMINDER_IDS, ids.map(Long::toString).toSet()).apply()
    }

    private fun reminderIds(): Set<Long> = preferences.getStringSet(KEY_REMINDER_IDS, emptySet())
        .orEmpty()
        .mapNotNull(String::toLongOrNull)
        .toSet()

    private fun actionFor(id: Long) = "$ACTION_PREFIX.$id"

    companion object {
        private const val TAG = "ClassReminderScheduler"
        private const val PREFERENCES_NAME = "class_reminders"
        private const val KEY_REMINDER_IDS = "scheduled_ids"
        private const val ACTION_PREFIX = "com.example.CLASS_REMINDER"

        internal const val EXTRA_ID = "reminder_id"
        internal const val EXTRA_TITLE = "reminder_title"
        internal const val EXTRA_DAY = "reminder_day"
        internal const val EXTRA_TIME = "reminder_time"
        internal const val EXTRA_MINUTES_BEFORE = "reminder_minutes_before"
        internal const val EXTRA_LOCATION = "reminder_location"
        internal const val NOTIFICATION_CHANNEL_ID = "class_reminders"
        internal const val NOTIFICATION_ID_BASE = 4_000
    }
}
