package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val universidad: String = "Universidad Nacional",
    val ciclo: String = "Semestre 2025-1",
    val carrera: String = "Ingeniería de Sistemas",
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuDR9NaLIiGeoKYdPj6iP1rjyIj2UEC7aBktw67SUU55TsH79ALjL2Osl7DKQhhauOSCyCJ06nTxBGc4HQTm7yQxc9ldIqDbs61ss9J1V9xmvld70No-0GgxyngxcLGbiyxuCiBu-BE02q08Dnk8pOnguav9aG233AyOproswTF6MmexYSS9uCMOJKFnafBop3taiPi2uPLoM_GxHUL_xxx3BUJ3oa5xNZefEawGBntzXmcbQgvJrZ8Q",
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val icono: String, // e.g., "restaurant", "directions_bus", "menu_book", "sports_esports", "more_horiz"
    val colorHex: String = "#1E5C42",
    val tipo: String = "GASTO" // "GASTO" o "INGRESO"
)

@Entity(tableName = "metodos_pago")
data class MetodoPago(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String, // "Efectivo", "Digital (Nequi/Daviplata)", "Débito", "Tarjeta Crédito"
    val icono: String, // "payments", "account_balance_wallet", "credit_card"
    val tipo: String = "DIGITAL" // "EFECTIVO", "DIGITAL", "DEBITO"
)

@Entity(tableName = "presupuesto")
data class Presupuesto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodo: String = "Mayo", // e.g., "Este mes (Mayo)"
    val metaTotal: Double = 500000.0,
    val alertaPorcentaje: Int = 80,
    val activo: Boolean = true
)

@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: String = "GASTO", // "GASTO" o "INGRESO"
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val fechaTexto: String, // "Hoy, 1:30 PM", "Ayer", "18 May"
    val categoriaNombre: String,
    val metodoPagoNombre: String,
    val nota: String = ""
)

@Entity(tableName = "recordatorio")
data class Recordatorio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String, // "Cálculo Multivariable", "Estructuras de Datos", "Economía y Finanzas", "Entrega Economía Aplicada"
    val tipo: String = "CLASE", // "CLASE", "ENTREGA", "PARCIAL", "ACTIVIDAD"
    val diaSemana: String = "Martes", // "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    val diaNumero: Int = 18,
    val horaInicio: String, // "10:00 AM", "2:00 PM"
    val horaFin: String, // "12:00 PM", "4:00 PM"
    val aula: String, // "Aula 304 - Edificio de Ingeniería"
    val grupoOProfesor: String, // "Grupo B1", "Prof. M. Morales", "Dra. S. Valenzuela"
    val facultad: String = "Ingeniería",
    val modalidad: String = "Teórica", // "Teórica", "Práctica", "Laboratorio", "Entrega"
    val notificarMinutosAntes: Int = 15,
    val piso: String = "Piso 3",
    val activo: Boolean = true
)

@Entity(tableName = "notificaciones")
data class Notificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val mensaje: String,
    val fechaTexto: String = "Hoy",
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val leida: Boolean = false,
    val tipo: String = "ACADEMICA" // "ACADEMICA", "FINANCIERA", "SISTEMA"
)
