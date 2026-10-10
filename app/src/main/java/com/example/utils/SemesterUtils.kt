package com.example.utils

import java.util.Calendar

object SemesterUtils {
    /**
     * Calculates the semester string based on the provided Calendar date (defaults to local device date).
     * Rules:
     * - First semester: Jan 1 to Jun 30 -> "Semestre YYYY-1"
     * - Second semester: Jul 1 to Dec 31 -> "Semestre YYYY-2"
     * Works across any calendar year and leap years seamlessly.
     */
    fun calculateSemester(calendar: Calendar = Calendar.getInstance()): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // Calendar.JANUARY (0) .. Calendar.DECEMBER (11)

        val semesterNumber = if (month in Calendar.JANUARY..Calendar.JUNE) 1 else 2
        return "Semestre $year-$semesterNumber"
    }
}
