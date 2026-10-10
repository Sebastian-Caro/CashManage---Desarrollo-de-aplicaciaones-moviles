package com.example

import com.example.utils.PasswordHasher
import com.example.utils.PasswordValidator
import com.example.utils.SemesterUtils
import com.example.utils.UniversityDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class AuthValidationTest {

    @Test
    fun `password hashes are salted and verify only the original password`() {
        val firstHash = PasswordHasher.hash("Estudiante2025")
        val secondHash = PasswordHasher.hash("Estudiante2025")

        assertTrue(PasswordHasher.isHashed(firstHash))
        assertTrue(PasswordHasher.verify("Estudiante2025", firstHash))
        assertFalse(PasswordHasher.verify("OtraClave2025", firstHash))
        assertFalse(firstHash == secondHash)
    }

    // --- Password Validation Tests ---

    @Test
    fun `password shorter than 8 characters fails validation`() {
        val result = PasswordValidator.validate("Pass1")
        assertFalse(result.isValid)
        assertFalse(result.hasMinLength)
        assertEquals("La contraseña debe tener al menos 8 caracteres.", result.errorMessage)
    }

    @Test
    fun `password without uppercase letter fails validation`() {
        val result = PasswordValidator.validate("password123")
        assertFalse(result.isValid)
        assertTrue(result.hasMinLength)
        assertFalse(result.hasUppercase)
        assertEquals("La contraseña debe contener al menos una letra mayúscula.", result.errorMessage)
    }

    @Test
    fun `password without lowercase letter fails validation`() {
        val result = PasswordValidator.validate("PASSWORD123")
        assertFalse(result.isValid)
        assertTrue(result.hasMinLength)
        assertFalse(result.hasLowercase)
        assertEquals("La contraseña debe contener al menos una letra minúscula.", result.errorMessage)
    }

    @Test
    fun `password meeting all security criteria passes validation`() {
        val result = PasswordValidator.validate("Estudiante2025")
        assertTrue(result.isValid)
        assertTrue(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertNull(result.errorMessage)
    }

    // --- University Detector Tests ---

    @Test
    fun `detects known university domains correctly`() {
        assertEquals("Universidad Nacional", UniversityDetector.detectUniversity("estudiante@universidad.edu"))
        assertEquals("Universidad Nacional de Colombia", UniversityDetector.detectUniversity("user@unal.edu.co"))
        assertEquals("Universidad del Caribe", UniversityDetector.detectUniversity("estudiante@unicaribe.mx"))
        assertEquals("Universidad del Caribe", UniversityDetector.detectUniversity("estudiante@unicaribe.edu.mx"))
        assertEquals("Universidad de los Andes", UniversityDetector.detectUniversity("alumno@uniandes.edu.co"))
        assertEquals("Pontificia Universidad Javeriana", UniversityDetector.detectUniversity("alumno@javeriana.edu.co"))
    }

    @Test
    fun `handles unknown edu domain gracefully with fallback format`() {
        assertEquals("Universidad Harvard", UniversityDetector.detectUniversity("student@harvard.edu"))
    }

    @Test
    fun `handles blank or non-edu emails with default university`() {
        assertEquals("Universidad Nacional", UniversityDetector.detectUniversity(""))
        assertEquals("Universidad Nacional", UniversityDetector.detectUniversity("user@gmail.com"))
    }

    // --- Semester Calculation Tests ---

    @Test
    fun `calculates first semester for dates between Jan 1 and Jun 30`() {
        // Jan 1, 2026
        val jan1 = GregorianCalendar(2026, Calendar.JANUARY, 1)
        assertEquals("Semestre 2026-1", SemesterUtils.calculateSemester(jan1))

        // Jun 30, 2026
        val jun30 = GregorianCalendar(2026, Calendar.JUNE, 30)
        assertEquals("Semestre 2026-1", SemesterUtils.calculateSemester(jun30))
    }

    @Test
    fun `calculates second semester for dates between Jul 1 and Dec 31`() {
        // Jul 1, 2026
        val jul1 = GregorianCalendar(2026, Calendar.JULY, 1)
        assertEquals("Semestre 2026-2", SemesterUtils.calculateSemester(jul1))

        // Dec 31, 2026
        val dec31 = GregorianCalendar(2026, Calendar.DECEMBER, 31)
        assertEquals("Semestre 2026-2", SemesterUtils.calculateSemester(dec31))
    }

    @Test
    fun `handles leap years correctly for semester boundary calculation`() {
        // Feb 29, 2024 (leap year)
        val feb29 = GregorianCalendar(2024, Calendar.FEBRUARY, 29)
        assertEquals("Semestre 2024-1", SemesterUtils.calculateSemester(feb29))

        // Feb 29, 2028 (leap year)
        val feb292028 = GregorianCalendar(2028, Calendar.FEBRUARY, 29)
        assertEquals("Semestre 2028-1", SemesterUtils.calculateSemester(feb292028))
    }
}
