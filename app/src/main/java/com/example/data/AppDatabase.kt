package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import com.example.utils.PasswordHasher
import com.example.utils.SemesterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        MetodoPago::class,
        Presupuesto::class,
        Movimiento::class,
        Recordatorio::class,
        Notificacion::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val credentials = mutableListOf<Pair<Long, String>>()
                db.query("SELECT id, contrasena FROM usuario").use { cursor ->
                    while (cursor.moveToNext()) {
                        credentials += cursor.getLong(0) to cursor.getString(1)
                    }
                }
                credentials.forEach { (userId, password) ->
                    if (!PasswordHasher.isHashed(password)) {
                        db.execSQL(
                            "UPDATE usuario SET contrasena = ? WHERE id = ?",
                            arrayOf<Any>(PasswordHasher.hash(password), userId)
                        )
                    }
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val ownerUserId = db.query(
                    "SELECT id FROM usuario ORDER BY isLoggedIn DESC, id ASC LIMIT 1"
                ).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getLong(0) else 0L
                }
                listOf(
                    "categorias",
                    "metodos_pago",
                    "presupuesto",
                    "movimientos",
                    "recordatorio",
                    "notificaciones"
                ).forEach { table ->
                    db.execSQL("ALTER TABLE $table ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("UPDATE $table SET usuarioId = ?", arrayOf<Any>(ownerUserId))
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE usuario SET avatarUrl = '' WHERE avatarUrl = ?",
                    arrayOf<Any>(SEED_AVATAR_URL)
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE metodos_pago ADD COLUMN saldoActual REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    "ALTER TABLE presupuesto ADD COLUMN fechaInicioMillis INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE presupuesto ADD COLUMN configurado INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE movimientos ADD COLUMN metodoPagoId INTEGER"
                )
                db.execSQL(
                    """
                    UPDATE movimientos
                    SET metodoPagoId = (
                        SELECT id FROM metodos_pago
                        WHERE metodos_pago.usuarioId = movimientos.usuarioId
                          AND metodos_pago.nombre = movimientos.metodoPagoNombre
                        ORDER BY id ASC
                        LIMIT 1
                    )
                    WHERE EXISTS (
                        SELECT 1 FROM metodos_pago
                        WHERE metodos_pago.usuarioId = movimientos.usuarioId
                          AND metodos_pago.nombre = movimientos.metodoPagoNombre
                    )
                    """.trimIndent()
                )
            }
        }

        private const val SEED_AVATAR_URL =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDR9NaLIiGeoKYdPj6iP1rjyIj2UEC7aBktw67SUU55TsH79ALjL2Osl7DKQhhauOSCyCJ06nTxBGc4HQTm7yQxc9ldIqDbs61ss9J1V9xmvld70No-0GgxyngxcLGbiyxuCiBu-BE02q08Dnk8pOnguav9aG233AyOproswTF6MmexYSS9uCMOJKFnafBop3taiPi2uPLoM_GxHUL_xxx3BUJ3oa5xNZefEawGBntzXmcbQgvJrZ8Q"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_manager_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            // 1. Usuario inicial
            val seedUserId = dao.insertUsuario(
                Usuario(
                    nombre = "Alejandro Morales",
                    correo = "estudiante@universidad.edu",
                    contrasenaHash = PasswordHasher.hash("Estudiante123"),
                    universidad = "Universidad Nacional",
                    ciclo = SemesterUtils.calculateSemester(),
                    carrera = "Ingeniería de Sistemas",
                    avatarUrl = "",
                    isLoggedIn = false
                )
            )

            // 2. Categorias iniciales
            dao.insertCategoria(Categoria(nombre = "Alimentación", icono = "restaurant", colorHex = "#1E5C42", usuarioId = seedUserId))
            dao.insertCategoria(Categoria(nombre = "Transporte", icono = "directions_bus", colorHex = "#2E9B66", usuarioId = seedUserId))
            dao.insertCategoria(Categoria(nombre = "Academia", icono = "menu_book", colorHex = "#006D42", usuarioId = seedUserId))
            dao.insertCategoria(Categoria(nombre = "Ocio", icono = "sports_esports", colorHex = "#45554D", usuarioId = seedUserId))
            dao.insertCategoria(Categoria(nombre = "Otros", icono = "more_horiz", colorHex = "#707973", usuarioId = seedUserId))

            // 3. Métodos de pago iniciales
            dao.insertMetodoPago(MetodoPago(nombre = "Efectivo", icono = "payments", tipo = "EFECTIVO", usuarioId = seedUserId))
            dao.insertMetodoPago(MetodoPago(nombre = "Digital", icono = "account_balance_wallet", tipo = "DIGITAL", usuarioId = seedUserId))
            dao.insertMetodoPago(MetodoPago(nombre = "Débito", icono = "credit_card", tipo = "DEBITO", usuarioId = seedUserId))

            // 4. Presupuesto inicial ($500.000)
            dao.insertPresupuesto(
                Presupuesto(
                    periodo = "Mayo",
                    metaTotal = 500000.0,
                    alertaPorcentaje = 80,
                    activo = true,
                    usuarioId = seedUserId
                )
            )

            // 5. Movimientos iniciales (sumando gastos para que quede saldo disponible aprox $345.500 como en el screenshot)
            // 500000 - 154500 = 345500
            val now = System.currentTimeMillis()
            dao.insertMovimiento(
                Movimiento(
                    descripcion = "Almuerzo cafetería central",
                    monto = 14500.0,
                    tipo = "GASTO",
                    fechaTimestamp = now - 1000 * 60 * 60 * 2,
                    fechaTexto = "Hoy, 1:30 PM",
                    categoriaNombre = "Alimentación",
                    metodoPagoNombre = "Digital",
                    nota = "Almuerzo con compañeros de sistemas",
                    usuarioId = seedUserId
                )
            )
            dao.insertMovimiento(
                Movimiento(
                    descripcion = "Fotocopias y material de estudio",
                    monto = 4200.0,
                    tipo = "GASTO",
                    fechaTimestamp = now - 1000 * 60 * 60 * 24,
                    fechaTexto = "Ayer",
                    categoriaNombre = "Academia",
                    metodoPagoNombre = "Efectivo",
                    nota = "Guías de laboratorio de algoritmos",
                    usuarioId = seedUserId
                )
            )
            dao.insertMovimiento(
                Movimiento(
                    descripcion = "Transporte / Tarjeta metro",
                    monto = 8000.0,
                    tipo = "GASTO",
                    fechaTimestamp = now - 1000 * 60 * 60 * 48,
                    fechaTexto = "18 May",
                    categoriaNombre = "Transporte",
                    metodoPagoNombre = "Débito",
                    nota = "Recarga semanal de metro",
                    usuarioId = seedUserId
                )
            )
            dao.insertMovimiento(
                Movimiento(
                    descripcion = "Libro Guía y Cuadernos",
                    monto = 127800.0,
                    tipo = "GASTO",
                    fechaTimestamp = now - 1000 * 60 * 60 * 72,
                    fechaTexto = "15 May",
                    categoriaNombre = "Academia",
                    metodoPagoNombre = "Débito",
                    nota = "Material semestral de cálculo y física",
                    usuarioId = seedUserId
                )
            )

            // 6. Recordatorios / Horario universitario
            dao.insertRecordatorio(
                Recordatorio(
                    titulo = "Cálculo Multivariable",
                    tipo = "CLASE",
                    diaSemana = "Martes",
                    diaNumero = 18,
                    horaInicio = "10:00 AM",
                    horaFin = "12:00 PM",
                    aula = "Aula 304 - Edificio de Ingeniería",
                    grupoOProfesor = "Grupo B1",
                    facultad = "Ingeniería",
                    modalidad = "Teórica",
                    piso = "Piso 3",
                    notificarMinutosAntes = 15,
                    usuarioId = seedUserId
                )
            )
            dao.insertRecordatorio(
                Recordatorio(
                    titulo = "Estructuras de Datos",
                    tipo = "CLASE",
                    diaSemana = "Martes",
                    diaNumero = 18,
                    horaInicio = "2:00 PM",
                    horaFin = "4:00 PM",
                    aula = "Lab de Cómputo 2",
                    grupoOProfesor = "Prof. M. Morales",
                    facultad = "Sistemas",
                    modalidad = "Práctica",
                    piso = "Piso 1",
                    notificarMinutosAntes = 15,
                    usuarioId = seedUserId
                )
            )
            dao.insertRecordatorio(
                Recordatorio(
                    titulo = "Economía y Finanzas",
                    tipo = "CLASE",
                    diaSemana = "Martes",
                    diaNumero = 18,
                    horaInicio = "4:30 PM",
                    horaFin = "6:00 PM",
                    aula = "Aula Magna",
                    grupoOProfesor = "Dra. S. Valenzuela",
                    facultad = "General",
                    modalidad = "Teórica",
                    piso = "Piso 2",
                    notificarMinutosAntes = 15,
                    usuarioId = seedUserId
                )
            )
            dao.insertRecordatorio(
                Recordatorio(
                    titulo = "Bases de Datos Relacionales",
                    tipo = "CLASE",
                    diaSemana = "Miércoles",
                    diaNumero = 19,
                    horaInicio = "8:00 AM",
                    horaFin = "10:00 AM",
                    aula = "Aula 201 - Torre B",
                    grupoOProfesor = "Ing. Camilo R.",
                    facultad = "Sistemas",
                    modalidad = "Teórica",
                    piso = "Piso 2",
                    notificarMinutosAntes = 15,
                    usuarioId = seedUserId
                )
            )

            // 7. Notificaciones iniciales
            dao.insertNotificacion(
                Notificacion(
                    titulo = "Entrega pendiente",
                    mensaje = "Tienes una entrega de Economía Aplicada hoy a las 18:00 hrs.",
                    fechaTexto = "Hoy, 12:00 PM",
                    tipo = "ACADEMICA",
                    usuarioId = seedUserId
                )
            )
            dao.insertNotificacion(
                Notificacion(
                    titulo = "Control de Presupuesto",
                    mensaje = "¡Vas bien! Mantienes el 74% de tu presupuesto de comida esta quincena.",
                    fechaTexto = "Hoy, 9:00 AM",
                    tipo = "FINANCIERA",
                    usuarioId = seedUserId
                )
            )
            dao.insertNotificacion(
                Notificacion(
                    titulo = "Próxima Clase",
                    mensaje = "Cálculo Multivariable en 35 min en Aula 304.",
                    fechaTexto = "Hoy, 9:25 AM",
                    tipo = "ACADEMICA",
                    usuarioId = seedUserId
                )
            )
        }
    }
}
