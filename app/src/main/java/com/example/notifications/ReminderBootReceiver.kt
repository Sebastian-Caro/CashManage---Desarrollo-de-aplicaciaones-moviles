package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context, this)
                val dao = database.appDao()
                val activeUser = dao.getActiveUsuarioOnce()
                val reminders = activeUser?.let { dao.getActiveRecordatoriosOnce(it.id) }.orEmpty()
                AndroidClassReminderScheduler(context).replaceAll(reminders)
            } catch (exception: IllegalStateException) {
                Log.e(TAG, "No se pudieron restaurar los recordatorios después del reinicio.", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderBootReceiver"
    }
}
