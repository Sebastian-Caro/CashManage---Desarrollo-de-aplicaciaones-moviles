package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Cash Manager", appName)
    }

    @Test
    fun `insert and retrieve user in room database`() = runBlocking {
        val dao = db.appDao()
        val user = Usuario(
            nombre = "Alejandro Morales",
            correo = "alejandro@universidad.edu",
            contrasenaHash = "123456",
            isLoggedIn = true
        )
        dao.insertUsuario(user)

        val active = dao.getActiveUsuario().first()
        assertNotNull(active)
        assertEquals("Alejandro Morales", active?.nombre)
    }

    @Test
    fun `insert and retrieve expense movement`() = runBlocking {
        val dao = db.appDao()
        val userId = dao.insertUsuario(
            Usuario(nombre = "Student", correo = "student@university.edu", contrasenaHash = "hash")
        )
        val mov = Movimiento(
            descripcion = "Almuerzo",
            monto = 12500.0,
            tipo = "GASTO",
            fechaTexto = "Hoy",
            categoriaNombre = "Alimentación",
            metodoPagoNombre = "Digital",
            usuarioId = userId
        )
        dao.insertMovimiento(mov)

        val list = dao.getAllMovimientos(userId).first()
        assertEquals(1, list.size)
        assertEquals(12500.0, list[0].monto, 0.001)
    }

    @Test
    fun `account data is isolated across all personal data types`() = runBlocking {
        val dao = db.appDao()
        val repository = AppRepository(dao)
        val firstUserId = dao.insertUsuario(
            Usuario(nombre = "First", correo = "first@university.edu", contrasenaHash = "hash")
        )
        val secondUserId = dao.insertUsuario(
            Usuario(nombre = "Second", correo = "second@university.edu", contrasenaHash = "hash")
        )

        repository.loginUser(firstUserId)
        repository.insertCategoria(
            Categoria(nombre = "Personal", icono = "category", usuarioId = firstUserId),
            firstUserId
        )
        repository.insertMetodoPago(
            MetodoPago(nombre = "Tarjeta", icono = "credit_card", usuarioId = firstUserId),
            firstUserId
        )
        repository.savePresupuesto(
            Presupuesto(metaTotal = 1000.0, usuarioId = firstUserId),
            firstUserId
        )
        repository.insertMovimiento(
            Movimiento(
                descripcion = "First expense",
                monto = 100.0,
                fechaTexto = "Today",
                categoriaNombre = "Personal",
                metodoPagoNombre = "Tarjeta",
                usuarioId = firstUserId
            ),
            firstUserId
        )
        repository.insertRecordatorio(
            Recordatorio(
                titulo = "First class",
                horaInicio = "8:00 AM",
                horaFin = "9:00 AM",
                aula = "Room 1",
                grupoOProfesor = "Teacher",
                usuarioId = firstUserId
            ),
            firstUserId
        )
        repository.insertNotificacion(
            Notificacion(titulo = "First notice", mensaje = "Private", usuarioId = firstUserId),
            firstUserId
        )

        assertEquals(1, repository.allCategorias.first().size)
        assertEquals(1, repository.allMetodosPago.first().size)
        assertEquals(1000.0, repository.activePresupuesto.first()!!.metaTotal, 0.001)
        assertEquals(1, repository.allMovimientos.first().size)
        assertEquals(1, repository.allRecordatorios.first().size)
        assertEquals(1, repository.allNotificaciones.first().size)

        repository.loginUser(secondUserId)
        assertEquals(0, repository.allCategorias.first().size)
        assertEquals(0, repository.allMetodosPago.first().size)
        assertEquals(null, repository.activePresupuesto.first())
        assertEquals(0, repository.allMovimientos.first().size)
        assertEquals(0, repository.allRecordatorios.first().size)
        assertEquals(0, repository.allNotificaciones.first().size)

        repository.loginUser(firstUserId)
        val firstMovement = repository.allMovimientos.first().single()
        repository.deleteMovimiento(firstMovement.id, secondUserId)
        assertEquals(1, repository.allMovimientos.first().size)
    }

    @Test
    fun `initial budget limits expenses and restores source balance on deletion`() = runBlocking {
        val dao = db.appDao()
        val repository = AppRepository(dao)
        val userId = dao.insertUsuario(
            Usuario(
                nombre = "Student",
                correo = "budget@university.edu",
                contrasenaHash = "hash",
                isLoggedIn = true
            )
        )
        val cashId = repository.insertMetodoPago(
            MetodoPago(nombre = "Efectivo", icono = "payments", usuarioId = userId),
            userId
        )
        val digitalId = repository.insertMetodoPago(
            MetodoPago(nombre = "Nequi", icono = "wallet", usuarioId = userId),
            userId
        )
        repository.savePresupuesto(Presupuesto(usuarioId = userId), userId)

        repository.insertMovimiento(
            Movimiento(
                descripcion = "Historical expense",
                monto = 100.0,
                fechaTimestamp = 1L,
                fechaTexto = "Before setup",
                categoriaNombre = "Food",
                metodoPagoNombre = "Efectivo",
                metodoPagoId = cashId,
                usuarioId = userId
            ),
            userId
        )

        assertTrue(
            repository.initializeBudget(
                usuarioId = userId,
                balances = mapOf(cashId to 700.0, digitalId to 300.0),
                total = 1000.0,
                fechaInicioMillis = 1000L
            )
        )
        assertFalse(repository.initializeBudget(userId, mapOf(cashId to 500.0), 500.0, 2000L))
        assertEquals(true, repository.activePresupuesto.first()?.configurado)
        assertEquals(1000.0, repository.activePresupuesto.first()!!.metaTotal, 0.001)

        val firstExpense = Movimiento(
            descripcion = "Current expense",
            monto = 700.0,
            fechaTimestamp = 2000L,
            fechaTexto = "Today",
            categoriaNombre = "Food",
            metodoPagoNombre = "Efectivo",
            metodoPagoId = cashId,
            usuarioId = userId
        )
        assertEquals(
            com.example.data.dao.ExpenseRegistrationResult.SUCCESS,
            repository.registerExpense(firstExpense, userId)
        )
        assertEquals(
            com.example.data.dao.ExpenseRegistrationResult.BUDGET_LIMIT_EXCEEDED,
            repository.registerExpense(
                firstExpense.copy(
                    id = 0,
                    monto = 101.0,
                    metodoPagoNombre = "Nequi",
                    metodoPagoId = digitalId
                ),
                userId
            )
        )
        assertEquals(
            com.example.data.dao.ExpenseRegistrationResult.INSUFFICIENT_METHOD_BALANCE,
            repository.registerExpense(
                firstExpense.copy(
                    id = 0,
                    monto = 301.0,
                    metodoPagoNombre = "Nequi",
                    metodoPagoId = digitalId
                ),
                userId
            )
        )

        assertEquals(0.0, dao.getMetodoPago(cashId, userId)!!.saldoActual, 0.001)
        assertTrue(repository.deleteMovimiento(2L, userId))
        assertEquals(700.0, dao.getMetodoPago(cashId, userId)!!.saldoActual, 0.001)
        assertTrue(repository.deleteMovimiento(1L, userId))
        assertEquals(700.0, dao.getMetodoPago(cashId, userId)!!.saldoActual, 0.001)
    }

    @Test
    fun `migration links old expenses to the matching user's payment method`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(null)
                .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE metodos_pago (id INTEGER NOT NULL PRIMARY KEY, usuarioId INTEGER NOT NULL, nombre TEXT NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE presupuesto (id INTEGER NOT NULL PRIMARY KEY, usuarioId INTEGER NOT NULL)"
                        )
                        db.execSQL(
                            "CREATE TABLE movimientos (id INTEGER NOT NULL PRIMARY KEY, usuarioId INTEGER NOT NULL, metodoPagoNombre TEXT NOT NULL)"
                        )
                        db.execSQL("INSERT INTO metodos_pago VALUES (5, 8, 'Nequi')")
                        db.execSQL("INSERT INTO presupuesto VALUES (1, 8)")
                        db.execSQL("INSERT INTO movimientos VALUES (9, 8, 'Nequi')")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )

        try {
            val legacyDatabase = helper.writableDatabase
            AppDatabase.MIGRATION_4_5.migrate(legacyDatabase)
            legacyDatabase.query("SELECT metodoPagoId FROM movimientos WHERE id = 9").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(5L, cursor.getLong(0))
            }
            legacyDatabase.query(
                "SELECT saldoActual FROM metodos_pago WHERE id = 5"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0.0, cursor.getDouble(0), 0.001)
            }
            legacyDatabase.query(
                "SELECT configurado FROM presupuesto"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun `database migration assigns legacy personal data to the logged-in account`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(null)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE usuario (id INTEGER NOT NULL PRIMARY KEY, isLoggedIn INTEGER NOT NULL)"
                        )
                        db.execSQL("INSERT INTO usuario (id, isLoggedIn) VALUES (1, 0), (2, 1)")
                        listOf(
                            "categorias",
                            "metodos_pago",
                            "presupuesto",
                            "movimientos",
                            "recordatorio",
                            "notificaciones"
                        ).forEach { table ->
                            db.execSQL("CREATE TABLE $table (id INTEGER NOT NULL PRIMARY KEY)")
                            db.execSQL("INSERT INTO $table (id) VALUES (1)")
                        }
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )

        try {
            val legacyDatabase = helper.writableDatabase
            AppDatabase.MIGRATION_2_3.migrate(legacyDatabase)

            listOf(
                "categorias",
                "metodos_pago",
                "presupuesto",
                "movimientos",
                "recordatorio",
                "notificaciones"
            ).forEach { table ->
                legacyDatabase.query("SELECT usuarioId FROM $table").use { cursor ->
                    assertEquals(true, cursor.moveToFirst())
                    assertEquals(2L, cursor.getLong(0))
                }
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun `profile migration replaces the built-in remote avatar with initials`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val legacyAvatar =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDR9NaLIiGeoKYdPj6iP1rjyIj2UEC7aBktw67SUU55TsH79ALjL2Osl7DKQhhauOSCyCJ06nTxBGc4HQTm7yQxc9ldIqDbs61ss9J1V9xmvld70No-0GgxyngxcLGbiyxuCiBu-BE02q08Dnk8pOnguav9aG233AyOproswTF6MmexYSS9uCMOJKFnafBop3taiPi2uPLoM_GxHUL_xxx3BUJ3oa5xNZefEawGBntzXmcbQgvJrZ8Q"
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(null)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE usuario (id INTEGER NOT NULL PRIMARY KEY, avatarUrl TEXT NOT NULL)"
                        )
                        db.execSQL(
                            "INSERT INTO usuario (id, avatarUrl) VALUES (1, ?)",
                            arrayOf<Any>(legacyAvatar)
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )

        try {
            val legacyDatabase = helper.writableDatabase
            AppDatabase.MIGRATION_3_4.migrate(legacyDatabase)
            legacyDatabase.query("SELECT avatarUrl FROM usuario WHERE id = 1").use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals("", cursor.getString(0))
            }
        } finally {
            helper.close()
        }
    }
}
