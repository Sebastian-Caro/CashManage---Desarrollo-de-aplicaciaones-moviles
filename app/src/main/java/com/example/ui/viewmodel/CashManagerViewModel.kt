package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
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
    val totalGastos: StateFlow<Double> = allMovimientos.combine(activePresupuesto) { movimientos, _ ->
        movimientos.filter { it.tipo == "GASTO" }.sumOf { it.monto }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val saldoDisponible: StateFlow<Double> = combine(activePresupuesto, totalGastos) { presupuesto, gastos ->
        val meta = presupuesto?.metaTotal ?: 500000.0
        val disp = meta - gastos
        if (disp < 0.0) 0.0 else disp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 345500.0)

    val porcentajeConsumido: StateFlow<Int> = combine(activePresupuesto, totalGastos) { presupuesto, gastos ->
        val meta = presupuesto?.metaTotal ?: 500000.0
        if (meta <= 0) 0 else ((gastos / meta) * 100).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 68)

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

    // --- Authentication ---
    fun login(correo: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (correo.isBlank() || contrasena.isBlank()) {
                onResult(false, "Por favor ingresa correo y contraseña")
                return@launch
            }
            val existing = repository.getUsuarioByCorreo(correo.trim())
            if (existing != null) {
                if (existing.contrasena == contrasena.trim()) {
                    repository.loginUser(existing.id)
                    _currentScreen.value = AppScreen.HOME
                    onResult(true, null)
                } else {
                    onResult(false, "Contraseña incorrecta")
                }
            } else {
                // If user doesn't exist yet, auto-register for student ease!
                val newId = repository.insertUsuario(
                    Usuario(
                        nombre = correo.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                        correo = correo.trim(),
                        contrasena = contrasena.trim(),
                        isLoggedIn = true
                    )
                )
                repository.loginUser(newId)
                _currentScreen.value = AppScreen.HOME
                onResult(true, null)
            }
        }
    }

    fun register(nombre: String, correo: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                onResult(false, "Todos los campos son obligatorios")
                return@launch
            }
            val existing = repository.getUsuarioByCorreo(correo.trim())
            if (existing != null) {
                onResult(false, "Ya existe una cuenta con este correo institucional")
                return@launch
            }
            val newId = repository.insertUsuario(
                Usuario(
                    nombre = nombre.trim(),
                    correo = correo.trim(),
                    contrasena = contrasena.trim(),
                    isLoggedIn = true
                )
            )
            repository.loginUser(newId)
            _currentScreen.value = AppScreen.HOME
            onResult(true, null)
        }
    }

    fun quickGoogleLogin() {
        viewModelScope.launch {
            val user = activeUsuario.value
            if (user != null) {
                _currentScreen.value = AppScreen.HOME
            } else {
                val newId = repository.insertUsuario(
                    Usuario(
                        nombre = "Alejandro Morales",
                        correo = "alejandro.morales@universidad.edu",
                        contrasena = "campus2025",
                        universidad = "Universidad Nacional",
                        isLoggedIn = true
                    )
                )
                repository.loginUser(newId)
                _currentScreen.value = AppScreen.HOME
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            backStack.clear()
            _currentScreen.value = AppScreen.LOGIN
        }
    }

    fun updateProfile(nombre: String, universidad: String, ciclo: String, carrera: String) {
        viewModelScope.launch {
            val current = activeUsuario.value ?: return@launch
            val updated = current.copy(
                nombre = nombre.trim(),
                universidad = universidad.trim(),
                ciclo = ciclo.trim(),
                carrera = carrera.trim()
            )
            repository.updateUsuario(updated)
            showToast("Perfil actualizado correctamente")
        }
    }

    // --- Presupuesto ---
    fun updatePresupuestoLimit(monto: Double, alerta: Int = 80) {
        viewModelScope.launch {
            val current = activePresupuesto.value
            val pres = if (current != null) {
                current.copy(metaTotal = monto, alertaPorcentaje = alerta)
            } else {
                Presupuesto(metaTotal = monto, alertaPorcentaje = alerta)
            }
            repository.savePresupuesto(pres)
            showToast("Límite de presupuesto actualizado a ${formatCurrency(monto)}")
        }
    }

    // --- Movimientos (Gastos) ---
    fun addGasto(
        monto: Double,
        categoria: String,
        metodoPago: String,
        nota: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (monto <= 0) {
                showToast("Por favor ingresa un monto válido")
                return@launch
            }
            val desc = if (nota.isNotBlank()) nota.trim() else "Gasto en $categoria"
            val mov = Movimiento(
                descripcion = desc,
                monto = monto,
                tipo = "GASTO",
                fechaTimestamp = System.currentTimeMillis(),
                fechaTexto = "Hoy, justo ahora",
                categoriaNombre = categoria,
                metodoPagoNombre = metodoPago,
                nota = nota.trim()
            )
            repository.insertMovimiento(mov)

            // Register notification if threshold exceeded
            val currentPres = activePresupuesto.value?.metaTotal ?: 500000.0
            val newTotal = (totalGastos.value + monto)
            if (newTotal > currentPres * 0.8) {
                repository.insertNotificacion(
                    Notificacion(
                        titulo = "Alerta de Presupuesto",
                        mensaje = "Has consumido más del 80% de tu presupuesto mensual.",
                        tipo = "FINANCIERA"
                    )
                )
            }

            showToast("¡Gasto guardado exitosamente!")
            onSuccess()
        }
    }

    fun deleteMovimiento(id: Long) {
        viewModelScope.launch {
            repository.deleteMovimiento(id)
            showToast("Movimiento eliminado")
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
                notificarMinutosAntes = 15
            )
            repository.insertRecordatorio(recordatorio)
            repository.insertNotificacion(
                Notificacion(
                    titulo = "Nueva materia agendada",
                    mensaje = "Se programó $titulo para los días $dia.",
                    tipo = "ACADEMICA"
                )
            )
            showToast("Materia agregada al horario")
            onSuccess()
        }
    }

    fun deleteClass(id: Long) {
        viewModelScope.launch {
            repository.deleteRecordatorio(id)
            showToast("Materia eliminada del horario")
        }
    }

    // --- Categories & Payment methods management ---
    fun addCategory(nombre: String, icono: String = "category") {
        viewModelScope.launch {
            if (nombre.isNotBlank()) {
                repository.insertCategoria(Categoria(nombre = nombre.trim(), icono = icono))
                showToast("Categoría agregada")
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategoria(id)
            showToast("Categoría eliminada")
        }
    }

    fun addPaymentMethod(nombre: String, tipo: String = "DIGITAL") {
        viewModelScope.launch {
            if (nombre.isNotBlank()) {
                val icon = when (tipo) {
                    "EFECTIVO" -> "payments"
                    "DEBITO" -> "credit_card"
                    else -> "account_balance_wallet"
                }
                repository.insertMetodoPago(MetodoPago(nombre = nombre.trim(), icono = icon, tipo = tipo))
                showToast("Método de pago guardado")
            }
        }
    }

    fun deletePaymentMethod(id: Long) {
        viewModelScope.launch {
            repository.deleteMetodoPago(id)
            showToast("Método de pago eliminado")
        }
    }

    // --- Notificaciones ---
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificacionAsRead(id)
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotificacion(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotificaciones()
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
