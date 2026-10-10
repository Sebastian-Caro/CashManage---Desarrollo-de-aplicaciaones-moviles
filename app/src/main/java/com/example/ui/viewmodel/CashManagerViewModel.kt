package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.dao.ExpenseRegistrationResult
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import com.example.utils.PasswordValidator
import com.example.utils.PasswordHasher
import com.example.utils.SemesterUtils
import com.example.utils.UniversityDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

enum class AppScreen {
    LOGIN,
    HUB,
    HOME,
    ADD_EXPENSE,
    AGENDA,
    TRANSACTIONS,
    ACCOUNT
}

class CashManagerViewModel(private val repository: AppRepository) : ViewModel() {

    // Current navigation screen
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Navigation backstack
    private val backStack = mutableListOf<AppScreen>()

    // Selected day in Agenda
    private val _selectedDay = MutableStateFlow("Martes")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    // Agenda tab: 0 = Actividades, 1 = Horario de clases
    private val _agendaTab = MutableStateFlow(1)
    val agendaTab: StateFlow<Int> = _agendaTab.asStateFlow()

    // Active User
    val activeUsuario: StateFlow<Usuario?> = repository.activeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Budget
    val activePresupuesto: StateFlow<Presupuesto?> = repository.activePresupuesto
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All categories
    val allCategorias: StateFlow<List<Categoria>> = repository.allCategorias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All payment methods
    val allMetodosPago: StateFlow<List<MetodoPago>> = repository.allMetodosPago
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Movements
    val allMovimientos: StateFlow<List<Movimiento>> = repository.allMovimientos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMovimientos: StateFlow<List<Movimiento>> = repository.recentMovimientos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recordatorios (all)
    val allRecordatorios: StateFlow<List<Recordatorio>> = repository.allRecordatorios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notificaciones
    val allNotificaciones: StateFlow<List<Notificacion>> = repository.allNotificaciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback message (e.g. Snackbar or toast)
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Financial calculations
    val totalGastos: StateFlow<Double> = allMovimientos.combine(activePresupuesto) { movimientos, presupuesto ->
        movimientos
            .filter {
                it.tipo == "GASTO" &&
                    presupuesto?.configurado == true &&
                    it.fechaTimestamp >= presupuesto.fechaInicioMillis
            }
            .sumOf { it.monto }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val saldoDisponible: StateFlow<Double> = combine(activePresupuesto, totalGastos) { presupuesto, gastos ->
        val limiteGastable = if (presupuesto?.configurado == true) presupuesto.metaTotal * 0.8 else 0.0
        (limiteGastable - gastos).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val porcentajeConsumido: StateFlow<Int> = combine(activePresupuesto, totalGastos) { presupuesto, gastos ->
        val limiteGastable = if (presupuesto?.configurado == true) presupuesto.metaTotal * 0.8 else 0.0
        if (limiteGastable <= 0) 0 else ((gastos / limiteGastable) * 100).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Observe login state
        viewModelScope.launch {
            repository.activeUsuario.collect { user ->
                if (user == null && _currentScreen.value != AppScreen.LOGIN) {
                    _currentScreen.value = AppScreen.LOGIN
                } else if (user != null && _currentScreen.value == AppScreen.LOGIN) {
                    _currentScreen.value = AppScreen.HOME
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    fun setAgendaTab(tabIndex: Int) {
        _agendaTab.value = tabIndex
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    private suspend fun requireActiveUserId(): Long? {
        val userId = repository.activeUsuario.first()?.id
        if (userId == null) {
            showToast("Inicia sesión para gestionar tus datos.")
        }
        return userId
    }

    // --- Authentication ---
    fun login(correo: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = correo.trim()
            val cleanPass = contrasena.trim()
            if (cleanEmail.isBlank()) {
                onResult(false, "Ingresa tu correo institucional.")
                return@launch
            }
            if (cleanPass.isBlank()) {
                onResult(false, "Ingresa tu contraseña.")
                return@launch
            }
            val existing = repository.getUsuarioByCorreo(cleanEmail)
            if (existing == null) {
                onResult(false, "No encontramos una cuenta asociada a este correo.")
                return@launch
            }
            val passwordMatches = withContext(Dispatchers.Default) {
                PasswordHasher.verify(cleanPass, existing.contrasenaHash)
            }
            if (passwordMatches) {
                val currentSemester = SemesterUtils.calculateSemester()
                if (existing.ciclo != currentSemester) {
                    repository.updateUsuario(existing.copy(ciclo = currentSemester))
                }
                repository.ensureUserDefaults(existing.id)
                repository.loginUser(existing.id)
                _currentScreen.value = AppScreen.HOME
                onResult(true, null)
            } else {
                onResult(false, "La contraseña es incorrecta.")
            }
        }
    }

    fun register(nombre: String, correo: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val cleanName = nombre.trim()
            val cleanEmail = correo.trim()
            val cleanPass = contrasena.trim()

            if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPass.isBlank()) {
                onResult(false, "Todos los campos son obligatorios")
                return@launch
            }

            val passwordValidation = PasswordValidator.validate(cleanPass)
            if (!passwordValidation.isValid) {
                onResult(false, passwordValidation.errorMessage)
                return@launch
            }

            val existing = repository.getUsuarioByCorreo(cleanEmail)
            if (existing != null) {
                onResult(false, "Ya existe una cuenta con este correo institucional")
                return@launch
            }

            val autoUniversity = UniversityDetector.detectUniversity(cleanEmail)
            val autoSemester = SemesterUtils.calculateSemester()
            val passwordHash = withContext(Dispatchers.Default) {
                PasswordHasher.hash(cleanPass)
            }

            val newId = repository.insertUsuario(
                Usuario(
                    nombre = cleanName,
                    correo = cleanEmail,
                    contrasenaHash = passwordHash,
                    universidad = autoUniversity,
                    ciclo = autoSemester,
                    isLoggedIn = true
                )
            )
            repository.ensureUserDefaults(newId)
            repository.loginUser(newId)
            _currentScreen.value = AppScreen.HOME
            onResult(true, null)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            backStack.clear()
            _currentScreen.value = AppScreen.LOGIN
        }
    }

    fun updateProfile(
        nombre: String,
        universidad: String,
        ciclo: String,
        carrera: String,
        avatarUrl: String? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val cleanName = nombre.trim()
            val cleanUniversity = universidad.trim()
            if (cleanName.isBlank()) {
                onResult(false, "El nombre es obligatorio.")
                return@launch
            }
            if (cleanUniversity.isBlank()) {
                onResult(false, "La universidad es obligatoria.")
                return@launch
            }
            val current = activeUsuario.value ?: repository.activeUsuario.first()
            if (current == null) {
                onResult(false, "No hay una sesión activa para actualizar el perfil.")
                return@launch
            }
            val updated = current.copy(
                nombre = cleanName,
                universidad = cleanUniversity,
                ciclo = ciclo.trim().ifBlank { SemesterUtils.calculateSemester() },
                carrera = carrera.trim(),
                avatarUrl = avatarUrl?.trim().orEmpty()
            )
            repository.updateUsuario(updated)
            showToast("Perfil actualizado correctamente")
            onResult(true, null)
        }
    }

    // --- Presupuesto ---
    fun configureInitialBudget(
        balances: Map<Long, Double>,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val userId = requireActiveUserId()
            if (userId == null) {
                onResult(false, "Inicia sesión para configurar tu presupuesto.")
                return@launch
            }
            if (balances.isEmpty() || balances.values.any { !it.isFinite() || it < 0.0 }) {
                onResult(false, "Ingresa saldos válidos para tus fuentes de dinero.")
                return@launch
            }
            val total = balances.values.sum()
            if (!total.isFinite() || total <= 0.0) {
                onResult(false, "La suma de tus saldos debe ser mayor que cero.")
                return@launch
            }

            val initialized = repository.initializeBudget(
                usuarioId = userId,
                balances = balances,
                total = total,
                fechaInicioMillis = System.currentTimeMillis()
            )
            if (!initialized) {
                onResult(false, "No se pudo guardar el presupuesto. Verifica tus fuentes e inténtalo de nuevo.")
                return@launch
            }
            showToast("Presupuesto inicial configurado")
            onResult(true, null)
        }
    }

    // --- Movimientos (Gastos) ---
    fun addGasto(
        monto: Double,
        categoria: String,
        metodoPago: String,
        metodoPagoId: Long?,
        nota: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            if (monto <= 0) {
                showToast("Por favor ingresa un monto válido")
                return@launch
            }
            val desc = if (nota.isNotBlank()) nota.trim() else "Gasto en $categoria"
            if (!monto.isFinite()) {
                showToast("Por favor ingresa un monto válido")
                return@launch
            }
            val mov = Movimiento(
                descripcion = desc,
                monto = monto,
                tipo = "GASTO",
                fechaTimestamp = System.currentTimeMillis(),
                fechaTexto = "Hoy, justo ahora",
                categoriaNombre = categoria,
                metodoPagoNombre = metodoPago,
                metodoPagoId = metodoPagoId,
                nota = nota.trim(),
                usuarioId = userId
            )
            when (repository.registerExpense(mov, userId)) {
                ExpenseRegistrationResult.SUCCESS -> Unit
                ExpenseRegistrationResult.BUDGET_NOT_CONFIGURED -> {
                    showToast("Configura primero tu presupuesto inicial.")
                    return@launch
                }
                ExpenseRegistrationResult.PAYMENT_METHOD_NOT_FOUND -> {
                    showToast("Selecciona una fuente de dinero válida.")
                    return@launch
                }
                ExpenseRegistrationResult.INSUFFICIENT_METHOD_BALANCE -> {
                    showToast("El gasto supera el saldo disponible en $metodoPago.")
                    return@launch
                }
                ExpenseRegistrationResult.BUDGET_LIMIT_EXCEEDED -> {
                    showToast("El gasto supera tu límite del 80% disponible para gastar.")
                    return@launch
                }
                ExpenseRegistrationResult.INVALID_AMOUNT -> {
                    showToast("Por favor ingresa un monto válido")
                    return@launch
                }
            }

            // Register notification if threshold exceeded
            val currentPres = activePresupuesto.value
            val gastoMaximo = (currentPres?.metaTotal ?: 0.0) * 0.8
            val newTotal = totalGastos.value + monto
            if (
                currentPres?.configurado == true &&
                newTotal > gastoMaximo * (currentPres.alertaPorcentaje / 100.0)
            ) {
                repository.insertNotificacion(
                    Notificacion(
                        titulo = "Alerta de Presupuesto",
                        mensaje = "Has consumido más del ${currentPres.alertaPorcentaje}% del dinero asignado para gastos.",
                        tipo = "FINANCIERA",
                        usuarioId = userId
                    ),
                    userId
                )
            }

            showToast("¡Gasto guardado exitosamente!")
            onSuccess()
        }
    }

    fun deleteMovimiento(id: Long) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            if (repository.deleteMovimiento(id, userId)) {
                showToast("Movimiento eliminado")
            } else {
                showToast("No se encontró el movimiento para eliminar.")
            }
        }
    }

    // --- Recordatorios / Clases ---
    fun addClass(
        titulo: String,
        dia: String,
        horaInicio: String,
        horaFin: String,
        aula: String,
        profesor: String = "Profesor Titular",
        facultad: String = "Facultad",
        modalidad: String = "Teórica",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            if (titulo.isBlank()) {
                showToast("El nombre de la materia es requerido")
                return@launch
            }
            val recordatorio = Recordatorio(
                titulo = titulo.trim(),
                tipo = "CLASE",
                diaSemana = dia,
                horaInicio = horaInicio.trim(),
                horaFin = horaFin.trim(),
                aula = if (aula.isNotBlank()) aula.trim() else "Aula Asignada",
                grupoOProfesor = if (profesor.isNotBlank()) profesor.trim() else "Grupo Activo",
                facultad = facultad,
                modalidad = modalidad,
                notificarMinutosAntes = 15,
                usuarioId = userId
            )
            repository.insertRecordatorio(recordatorio, userId)
            repository.insertNotificacion(
                Notificacion(
                    titulo = "Nueva materia agendada",
                    mensaje = "Se programó $titulo para los días $dia.",
                    tipo = "ACADEMICA",
                    usuarioId = userId
                ),
                userId
            )
            showToast("Materia agregada al horario")
            onSuccess()
        }
    }

    fun deleteClass(id: Long) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            repository.deleteRecordatorio(id, userId)
            showToast("Materia eliminada del horario")
        }
    }

    // --- Categories & Payment methods management ---
    fun addCategory(nombre: String, icono: String = "category") {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            if (nombre.isNotBlank()) {
                repository.insertCategoria(
                    Categoria(nombre = nombre.trim(), icono = icono, usuarioId = userId),
                    userId
                )
                showToast("Categoría agregada")
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            repository.deleteCategoria(id, userId)
            showToast("Categoría eliminada")
        }
    }

    fun addPaymentMethod(nombre: String, tipo: String = "DIGITAL") {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            if (nombre.isNotBlank()) {
                val icon = when (tipo) {
                    "EFECTIVO" -> "payments"
                    "DEBITO" -> "credit_card"
                    else -> "account_balance_wallet"
                }
                repository.insertMetodoPago(
                    MetodoPago(nombre = nombre.trim(), icono = icon, tipo = tipo, usuarioId = userId),
                    userId
                )
                showToast("Método de pago guardado")
            }
        }
    }

    fun deletePaymentMethod(id: Long) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            if (repository.deleteMetodoPago(id, userId)) {
                showToast("Método de pago eliminado")
            } else {
                showToast("No puedes eliminar una fuente que tiene movimientos asociados.")
            }
        }
    }

    // --- Notificaciones ---
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            repository.markNotificacionAsRead(id, userId)
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            repository.deleteNotificacion(id, userId)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            val userId = requireActiveUserId() ?: return@launch
            repository.clearAllNotificaciones(userId)
            showToast("Notificaciones limpiadas")
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
            return "$ " + format.format(amount.toLong())
        }
    }
}

class CashManagerViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CashManagerViewModel::class.java)) {
            return CashManagerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
