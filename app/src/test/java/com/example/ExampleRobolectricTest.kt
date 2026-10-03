package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.Movimiento
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
            contrasena = "123456",
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
        val mov = Movimiento(
            descripcion = "Almuerzo",
            monto = 12500.0,
            tipo = "GASTO",
            fechaTexto = "Hoy",
            categoriaNombre = "Alimentación",
            metodoPagoNombre = "Digital"
        )
        dao.insertMovimiento(mov)

        val list = dao.getAllMovimientos().first()
        assertEquals(1, list.size)
        assertEquals(12500.0, list[0].monto, 0.001)
    }
}
