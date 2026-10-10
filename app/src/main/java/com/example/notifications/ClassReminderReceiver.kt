package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.utils.ClassReminderTime

class ClassReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(AndroidClassReminderScheduler.EXTRA_ID, 0L)
        val title = intent.getStringExtra(AndroidClassReminderScheduler.EXTRA_TITLE).orEmpty()
        val day = intent.getStringExtra(AndroidClassReminderScheduler.EXTRA_DAY).orEmpty()
        val time = intent.getStringExtra(AndroidClassReminderScheduler.EXTRA_TIME).orEmpty()
        val minutesBefore = intent.getIntExtra(AndroidClassReminderScheduler.EXTRA_MINUTES_BEFORE, -1)
        val location = intent.getStringExtra(AndroidClassReminderScheduler.EXTRA_LOCATION).orEmpty()
        if (id <= 0 || title.isBlank() || !ClassReminderTime.isValid(day, time, minutesBefore)) {
            Log.e(TAG, "No se pudo entregar el recordatorio: sus datos son inválidos.")
            return
        }

        createNotificationChannel(context)
        val openAppIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            context,
            AndroidClassReminderScheduler.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Próxima clase: $title")
            .setContentText(
                buildString {
                    append("Comienza a las $time")
                    if (location.isNotBlank()) append(" · $location")
                }
            )
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = context.getSystemService(NotificationManager::class.java)
            ?: error("NotificationManager no está disponible.")
        manager.notify(notificationId(id), notification)

        val triggerAt = ClassReminderTime.nextTriggerTimeMillis(
            day,
            time,
            minutesBefore,
            System.currentTimeMillis()
        ) ?: run {
            Log.e(TAG, "No se pudo reprogramar el recordatorio $id.")
            return
        }
        val nextIntent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = "$ACTION_PREFIX.$id"
            putExtra(AndroidClassReminderScheduler.EXTRA_ID, id)
            putExtra(AndroidClassReminderScheduler.EXTRA_TITLE, title)
            putExtra(AndroidClassReminderScheduler.EXTRA_DAY, day)
            putExtra(AndroidClassReminderScheduler.EXTRA_TIME, time)
            putExtra(AndroidClassReminderScheduler.EXTRA_MINUTES_BEFORE, minutesBefore)
            putExtra(AndroidClassReminderScheduler.EXTRA_LOCATION, location)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: error("AlarmManager no está disponible.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AndroidClassReminderScheduler.NOTIFICATION_CHANNEL_ID,
                "Recordatorios de agenda",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisos antes de tus clases y actividades" }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
                ?: error("NotificationManager no está disponible.")
        }
    }

    private fun notificationId(id: Long) =
        AndroidClassReminderScheduler.NOTIFICATION_ID_BASE + id.hashCode()

    companion object {
        private const val TAG = "ClassReminderReceiver"
        private const val ACTION_PREFIX = "com.example.CLASS_REMINDER"
    }
}
