package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormats {
    private val localeEs = Locale("es", "CO")
    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val isoTime = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val isoDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val displayDate = SimpleDateFormat("d MMM yyyy", localeEs)
    private val displayDateTime = SimpleDateFormat("d MMM, HH:mm", localeEs)

    fun today(): String = isoDate.format(Date())

    fun nowTime(): String = isoTime.format(Date())

    fun nowDateTime(): String = isoDateTime.format(Date())

    fun monthStart(date: Date = Date()): String {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return isoDate.format(cal.time)
    }

    fun monthEnd(date: Date = Date()): String {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return isoDate.format(cal.time)
    }

    fun displayFechaHora(fecha: String, hora: String): String {
        return try {
            val parsed = isoDateTime.parse("$fecha $hora") ?: return "$fecha $hora"
            displayDateTime.format(parsed)
        } catch (_: Exception) {
            "$fecha $hora"
        }
    }

    fun displayFechaLimite(fechaLimite: String): String {
        return try {
            val parsed = isoDateTime.parse(fechaLimite) ?: isoDate.parse(fechaLimite)
            if (parsed == null) fechaLimite else displayDateTime.format(parsed)
        } catch (_: Exception) {
            fechaLimite
        }
    }

    fun displayPeriodo(tipoPeriodo: String, fechaInicio: String, fechaFin: String): String {
        val etiqueta = if (tipoPeriodo.equals("SEMANAL", ignoreCase = true)) "Esta semana" else "Este mes"
        val inicio = try {
            isoDate.parse(fechaInicio)?.let { displayDate.format(it) } ?: fechaInicio
        } catch (_: Exception) {
            fechaInicio
        }
        val fin = try {
            isoDate.parse(fechaFin)?.let { displayDate.format(it) } ?: fechaFin
        } catch (_: Exception) {
            fechaFin
        }
        return "$etiqueta ($inicio – $fin)"
    }
}
