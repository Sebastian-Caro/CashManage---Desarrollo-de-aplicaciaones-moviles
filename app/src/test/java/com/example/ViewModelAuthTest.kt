package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.viewmodel.CashManagerViewModel
import com.example.utils.PasswordHasher
import com.example.utils.SemesterUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ViewModelAuthTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var viewModel: CashManagerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(db.appDao())
        viewModel = CashManagerViewModel(repository)
        ShadowLooper.idleMainLooper()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun `login identifies when the email has no registered account`() = runTest {
        val deferred = CompletableDeferred<Pair<Boolean, String?>>()

        viewModel.login("inexistent@universidad.edu", "WrongPass123") { success, msg ->
            deferred.complete(Pair(success, msg))
        }

        val (success, message) = deferred.await()
        assertFalse(success)
        assertEquals("No encontramos una cuenta asociada a este correo.", message)
    }

    @Test
    fun `login identifies when the password is incorrect`() = runTest {
        val registration = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.register("Valeria Montes", "valeria@unal.edu.co", "Valeria2026") { success, message ->
            registration.complete(success to message)
        }
        assertTrue(registration.await().first)

        val login = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.login("valeria@unal.edu.co", "WrongPass2026") { success, message ->
            login.complete(success to message)
        }

        val (success, message) = login.await()
        assertFalse(success)
        assertEquals("La contraseña es incorrecta.", message)
    }

    @Test
    fun `login identifies which required field is empty`() = runTest {
        val emailResult = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.login("", "SomePassword2026") { success, message ->
            emailResult.complete(success to message)
        }
        assertEquals("Ingresa tu correo institucional.", emailResult.await().second)

        val passwordResult = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.login("student@unal.edu.co", "") { success, message ->
            passwordResult.complete(success to message)
        }
        assertEquals("Ingresa tu contraseña.", passwordResult.await().second)
    }

    @Test
    fun `register fails if password does not meet security criteria`() = runTest {
        val deferred = CompletableDeferred<Pair<Boolean, String?>>()

        viewModel.register("Valeria Montes", "valeria@unal.edu.co", "weak") { success, msg ->
            deferred.complete(Pair(success, msg))
        }

        val (success, message) = deferred.await()
        assertFalse(success)
        assertEquals("La contraseña debe tener al menos 8 caracteres.", message)
    }

    @Test
    fun `register succeeds with valid data, auto-detects university and assigns current semester`() = runTest {
        val deferred = CompletableDeferred<Pair<Boolean, String?>>()

        viewModel.register("Valeria Montes", "valeria@unal.edu.co", "Valeria2026") { success, msg ->
            deferred.complete(Pair(success, msg))
        }

        val (success, message) = deferred.await()
        assertTrue(success)
        assertNull(message)

        val activeUser = repository.getUsuarioByCorreo("valeria@unal.edu.co")
        assertNotNull(activeUser)
        assertEquals("Valeria Montes", activeUser?.nombre)
        assertEquals("valeria@unal.edu.co", activeUser?.correo)
        assertEquals("Universidad Nacional de Colombia", activeUser?.universidad)
        assertEquals(SemesterUtils.calculateSemester(), activeUser?.ciclo)
        assertTrue(activeUser?.contrasenaHash != "Valeria2026")
        assertTrue(PasswordHasher.verify("Valeria2026", activeUser!!.contrasenaHash))
        assertEquals(5, repository.allCategorias.first().size)
        assertEquals(3, repository.allMetodosPago.first().size)
        assertNotNull(repository.activePresupuesto.first())
    }

    @Test
    fun `registered user can log in with the original password`() = runTest {
        val registration = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.register("Valeria Montes", "valeria@unal.edu.co", "Valeria2026") { success, message ->
            registration.complete(success to message)
        }
        assertTrue(registration.await().first)

        repository.logout()
        val login = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.login("valeria@unal.edu.co", "Valeria2026") { success, message ->
            login.complete(success to message)
        }

        val (success, message) = login.await()
        assertTrue(success)
        assertNull(message)
    }

    @Test
    fun `updateProfile correctly updates user name, university, and profile avatar`() = runTest {
        val regDeferred = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.register("Valeria Montes", "valeria@unicaribe.mx", "Valeria2026") { success, msg ->
            regDeferred.complete(Pair(success, msg))
        }
        regDeferred.await()

        val expectedSemester = SemesterUtils.calculateSemester()

        viewModel.updateProfile(
            nombre = "Valeria Montes P.",
            universidad = "Universidad del Caribe Campus Cancun",
            ciclo = expectedSemester,
            carrera = "Ingeniería en Software",
            avatarUrl = "https://example.com/custom-avatar.jpg"
        )

        var updatedUser = repository.getUsuarioByCorreo("valeria@unicaribe.mx")
        var retries = 0
        while (updatedUser?.nombre != "Valeria Montes P." && retries < 10) {
            delay(50)
            updatedUser = repository.getUsuarioByCorreo("valeria@unicaribe.mx")
            retries++
        }

        assertNotNull(updatedUser)
        assertEquals("Valeria Montes P.", updatedUser?.nombre)
        assertEquals("Universidad del Caribe Campus Cancun", updatedUser?.universidad)
        assertEquals("https://example.com/custom-avatar.jpg", updatedUser?.avatarUrl)
        assertEquals("Ingeniería en Software", updatedUser?.carrera)

        viewModel.updateProfile(
            nombre = "Valeria Montes P.",
            universidad = "Universidad del Caribe Campus Cancun",
            ciclo = expectedSemester,
            carrera = "Ingeniería en Software",
            avatarUrl = ""
        )
        updatedUser = repository.getUsuarioByCorreo("valeria@unicaribe.mx")
        retries = 0
        while (updatedUser?.avatarUrl != "" && retries < 10) {
            delay(50)
            updatedUser = repository.getUsuarioByCorreo("valeria@unicaribe.mx")
            retries++
        }
        assertEquals("", updatedUser?.avatarUrl)
    }

    @Test
    fun `updateProfile rejects blank name and university without changing account`() = runTest {
        val registration = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.register("Valeria Montes", "valeria@unicaribe.mx", "Valeria2026") { success, message ->
            registration.complete(success to message)
        }
        assertTrue(registration.await().first)

        val nameResult = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.updateProfile("", "Universidad del Caribe", "", "", "") { success, message ->
            nameResult.complete(success to message)
        }
        assertEquals("El nombre es obligatorio.", nameResult.await().second)

        val universityResult = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.updateProfile("Valeria Montes", " ", "", "", "") { success, message ->
            universityResult.complete(success to message)
        }
        assertEquals("La universidad es obligatoria.", universityResult.await().second)

        val unchangedUser = repository.getUsuarioByCorreo("valeria@unicaribe.mx")
        assertEquals("Valeria Montes", unchangedUser?.nombre)
        assertEquals("Universidad del Caribe", unchangedUser?.universidad)
    }

    @Test
    fun `logout clears backstack and navigates to LOGIN screen`() = runTest {
        val regDeferred = CompletableDeferred<Pair<Boolean, String?>>()
        viewModel.register("Test Student", "test@universidad.edu", "Student2026") { success, msg ->
            regDeferred.complete(Pair(success, msg))
        }
        regDeferred.await()

        viewModel.logout()

        var activeUser = repository.activeUsuario.first()
        var retries = 0
        while (activeUser != null && retries < 10) {
            delay(50)
            activeUser = repository.activeUsuario.first()
            retries++
        }

        assertNull(activeUser)
    }
}
