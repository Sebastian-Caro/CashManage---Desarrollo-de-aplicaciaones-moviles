package com.example.utils

object UniversityDetector {
    private val domainMap = mapOf(
        "universidad.edu" to "Universidad Nacional",
        "unal.edu.co" to "Universidad Nacional de Colombia",
        "unicaribe.mx" to "Universidad del Caribe",
        "unicaribe.edu.mx" to "Universidad del Caribe",
        "uniandes.edu.co" to "Universidad de los Andes",
        "javeriana.edu.co" to "Pontificia Universidad Javeriana",
        "udea.edu.co" to "Universidad de Antioquia",
        "univalle.edu.co" to "Universidad del Valle",
        "uis.edu.co" to "Universidad Industrial de Santander",
        "eafit.edu.co" to "Universidad EAFIT",
        "unam.mx" to "Universidad Nacional Autónoma de México"
    )

    fun detectUniversity(email: String): String {
        val trimmed = email.trim().lowercase()
        if (!trimmed.contains("@")) return "Universidad Nacional"

        val domain = trimmed.substringAfter("@")
        if (domain.isBlank()) return "Universidad Nacional"

        // Direct match
        domainMap[domain]?.let { return it }

        // Check subdomains or partial matches
        for ((knownDomain, university) in domainMap) {
            if (domain.endsWith(".$knownDomain") || domain == knownDomain) {
                return university
            }
        }

        // If domain ends with .edu or .edu.xx, format domain name nicely
        if (domain.contains(".edu")) {
            val primaryPart = domain.split(".")[0]
                .replace("-", " ")
                .replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            return "Universidad $primaryPart"
        }

        return "Universidad Nacional"
    }
}
