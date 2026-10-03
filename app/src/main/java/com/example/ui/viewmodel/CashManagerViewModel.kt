package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.DateFormats
import com.example.data.model.DesgloseCuenta
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.MovimientoConMetodo
import com.example.data.model.Presupuesto
import com.example.data.model.PresupuestoMetodo
import com.example.data.model.PresupuestoMetodoDetalle
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import com.example.security.PasswordSecurity
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

    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val backStack = mutableListOf<AppScreen>()

    val activeUsuario: StateFlow<Usuario?> = repository.activeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePresupuesto: StateFlow<Presupuesto?> = repository.activePresupuesto
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMetodosPago: StateFlow<List<MetodoPago>> = repository.allMetodosPago
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val desglosePresupuesto: StateFlow<List<PresupuestoMetodoDetalle>> = repository.desglosePresupuesto
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovimientos: StateFlow<List<MovimientoConMetodo>> = repository.movimientosUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMovimientos: StateFlow<List<MovimientoConMetodo>> = allMovimientos
        .combine(activePresupuesto) { movimientos, presupuesto ->
            movimientos.filter { estaEnPeriodo(it.movimiento, presupuesto) }.take(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordatorios: StateFlow<List<Recordatorio>> = repository.recordatoriosUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val totalGastos: StateFlow<Double> = combine(allMovimientos, activePresupuesto) { movimientos, presupuesto ->
        movimientos
            .map { it.movimiento }
            .filter { it.tipo == "GASTO" && estaEnPeriodo(it, presupuesto) }
            .sumOf { it.monto }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIngresos: StateFlow<Double> = combine(allMovimientos, activePresupuesto) { movimientos, presupuesto ->
        movimientos
            .map { it.movimiento }
            .filter { it.tipo == "INGRESO" && estaEnPeriodo(it, presupuesto) }
            .sumOf { it.monto }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val saldoDisponible: StateFlow<Double> = combine(activePresupuesto, totalGastos) { presupuesto, gastos ->
        val meta = presupuesto?.montoLimite ?: 0.0
        (meta - gastos).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val porcentajeConsumido: StateFlow<Int> = combine(activePresupuesto, totalGastos) { presupuesto, gastos ->
        val meta = presupuesto?.montoLimite ?: 0.0
        if (meta <= 0) 0 else ((gastos / meta) * 100).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val desgloseCuentas: StateFlow<List<DesgloseCuenta>> = combine(
        desglosePresupuesto,
        allMovimientos,
        activePresupuesto
    ) { desglose, movimientos, presupuesto ->
        desglose.map { item ->
            val delPeriodo = movimientos
                .map { it.movimiento }
                .filter { it.fkMetodo == item.fkMetodo && estaEnPeriodo(it, presupuesto) }
            val gastado = delPeriodo.filter { it.tipo == "GASTO" }.sumOf { it.monto }
            val ingresado = delPeriodo.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
            DesgloseCuenta(
                fkMetodo = item.fkMetodo,
                nombre = item.metodoNombre,
                montoAsignado = item.montoAsignado,
                gastado = gastado,
                ingresado = ingresado,
                disponible = (item.montoAsignado - gastado + ingresado).coerceAtLeast(0.0)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
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

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun login(correo: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (correo.isBlank() || contrasena.isBlank()) {
                onResult(false, "Por favor ingresa correo y contraseña")
                return@launch
            }
            val normalizedCorreo = correo.trim()
            val normalizedPassword = contrasena.trim()
            val existing = repository.getUsuarioByCorreo(normalizedCorreo)
            if (existing == null) {
                onResult(false, "No existe una cuenta con este correo")
                return@launch
            }
            if (!PasswordSecurity.verify(normalizedPassword, existing.contrasena)) {
                onResult(false, "Contraseña incorrecta")
                return@launch
            }
            repository.loginUser(existing.pkUsuario)
            _currentScreen.value = AppScreen.HOME
            onResult(true, null)
        }
    }

    fun register(nombre: String, correo: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        register(nombre, correo, contrasena, contrasena, onResult)
    }

    fun register(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                onResult(false, "Todos los campos son obligatorios")
                return@launch
            }
            val normalizedNombre = nombre.trim()
            val normalizedCorreo = correo.trim()
            val normalizedPassword = contrasena.trim()
            val normalizedConfirm = confirmarContrasena.trim()

            if (normalizedNombre.length < 2) {
                onResult(false, "El nombre debe tener al menos 2 caracteres")
                return@launch
            }
            if (!normalizedCorreo.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                onResult(false, "Ingresa un correo válido")
                return@launch
            }
            if (normalizedPassword.length < 6) {
                onResult(false, "La contraseña debe tener al menos 6 caracteres")
                return@launch
            }
            if (normalizedPassword != normalizedConfirm) {
                onResult(false, "Las contraseñas no coinciden")
                return@launch
            }

            val existing = repository.getUsuarioByCorreo(normalizedCorreo)
            if (existing != null) {
                onResult(false, "Ya existe una cuenta con este correo institucional")
                return@launch
            }
            val newId = repository.insertUsuario(
                Usuario(
                    nombre = normalizedNombre,
                    correo = normalizedCorreo,
                    contrasena = PasswordSecurity.hash(normalizedPassword),
                    fechaRegistro = DateFormats.today()
                )
            )
            repository.loginUser(newId)
            _currentScreen.value = AppScreen.HOME
            onResult(true, null)
        }
    }

    fun quickGoogleLogin() {
        viewModelScope.launch {
            val existing = repository.getUsuarioByCorreo("estudiante@universidad.edu")
            if (existing != null) {
                repository.loginUser(existing.pkUsuario)
            } else {
                val newId = repository.insertUsuario(
                    Usuario(
                        nombre = "Alejandro Morales",
                        correo = "estudiante@universidad.edu",
                        contrasena = PasswordSecurity.hash("123456"),
                        fechaRegistro = DateFormats.today()
                    )
                )
                repository.loginUser(newId)
            }
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun logout() {
        repository.logout()
        backStack.clear()
        _currentScreen.value = AppScreen.LOGIN
    }

    fun updateProfile(nombre: String) {
        viewModelScope.launch {
            val current = activeUsuario.value ?: return@launch
            val trimmed = nombre.trim()
            if (trimmed.isBlank() || trimmed.length < 2) {
                showToast("El nombre debe tener al menos 2 caracteres")
                return@launch
            }
            repository.updateUsuario(current.copy(nombre = trimmed))
            showToast("Perfil actualizado correctamente")
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val currentUser = activeUsuario.value ?: run {
                onResult(false, "Debes iniciar sesión para cambiar tu contraseña")
                return@launch
            }

            val currentTrimmed = currentPassword.trim()
            val newTrimmed = newPassword.trim()
            val confirmTrimmed = confirmPassword.trim()

            if (currentTrimmed.isBlank() || newTrimmed.isBlank() || confirmTrimmed.isBlank()) {
                onResult(false, "Completa todos los campos de contraseña")
                return@launch
            }
            if (!PasswordSecurity.verify(currentTrimmed, currentUser.contrasena)) {
                onResult(false, "La contraseña actual no es correcta")
                return@launch
            }
            if (newTrimmed.length < 6) {
                onResult(false, "La nueva contraseña debe tener al menos 6 caracteres")
                return@launch
            }
            if (newTrimmed != confirmTrimmed) {
                onResult(false, "La nueva contraseña y la confirmación no coinciden")
                return@launch
            }

            repository.updateUsuario(currentUser.copy(contrasena = PasswordSecurity.hash(newTrimmed)))
            showToast("Contraseña actualizada correctamente")
            onResult(true, null)
        }
    }

    fun updatePresupuestoLimit(monto: Double) {
        viewModelScope.launch {
            val user = activeUsuario.value ?: return@launch
            if (monto <= 0) {
                showToast("El límite debe ser mayor que cero")
                return@launch
            }
            val current = activePresupuesto.value
            val desglose = desglosePresupuesto.value
            val presupuestoId = if (current != null) {
                repository.updatePresupuesto(current.copy(montoLimite = monto))
                current.pkPresupuesto
            } else {
                val newId = repository.insertPresupuesto(
                    Presupuesto(
                        tipoPeriodo = "MENSUAL",
                        fechaInicio = DateFormats.monthStart(),
                        fechaFin = DateFormats.monthEnd(),
                        montoLimite = monto
                    )
                )
                repository.updateUsuario(user.copy(fkPresupuesto = newId))
                val metodos = allMetodosPago.value
                if (metodos.isNotEmpty()) {
                    val parte = monto / metodos.size
                    repository.replaceDesglose(
                        newId,
                        metodos.map {
                            PresupuestoMetodo(
                                montoAsignado = parte,
                                fkPresupuesto = newId,
                                fkMetodo = it.pkMetodo
                            )
                        }
                    )
                }
                newId
            }

            if (current != null && desglose.isNotEmpty()) {
                val totalActual = desglose.sumOf { it.montoAsignado }
                val factor = if (totalActual <= 0) 0.0 else monto / totalActual
                repository.replaceDesglose(
                    presupuestoId,
                    desglose.map {
                        PresupuestoMetodo(
                            montoAsignado = it.montoAsignado * factor,
                            fkPresupuesto = presupuestoId,
                            fkMetodo = it.fkMetodo
                        )
                    }
                )
            }
            showToast("Límite de presupuesto actualizado a ${formatCurrency(monto)}")
        }
    }

    fun saveDesgloseCuentas(asignaciones: Map<Long, Double>) {
        viewModelScope.launch {
            val presupuesto = activePresupuesto.value ?: run {
                showToast("Primero define un presupuesto")
                return@launch
            }
            val suma = asignaciones.values.sum()
            if (kotlin.math.abs(suma - presupuesto.montoLimite) > 0.5) {
                showToast("La suma por cuentas debe coincidir con ${formatCurrency(presupuesto.montoLimite)}")
                return@launch
            }
            repository.replaceDesglose(
                presupuesto.pkPresupuesto,
                asignaciones.map { (metodoId, monto) ->
                    PresupuestoMetodo(
                        montoAsignado = monto,
                        fkPresupuesto = presupuesto.pkPresupuesto,
                        fkMetodo = metodoId
                    )
                }
            )
            showToast("Desglose por cuentas actualizado")
        }
    }

    fun addMovimiento(
        monto: Double,
        tipo: String,
        metodoId: Long,
        nota: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = activeUsuario.value?.pkUsuario
            if (userId == null) {
                showToast("Inicia sesión para registrar movimientos")
                return@launch
            }
            if (monto <= 0) {
                showToast("Por favor ingresa un monto válido")
                return@launch
            }
            if (metodoId <= 0) {
                showToast("Selecciona una cuenta o método de pago")
                return@launch
            }
            repository.insertMovimiento(
                Movimiento(
                    tipo = tipo,
                    monto = monto,
                    fecha = DateFormats.today(),
                    hora = DateFormats.nowTime(),
                    nota = nota.trim().ifBlank { null },
                    fkUsuario = userId,
                    fkMetodo = metodoId
                )
            )
            val etiqueta = if (tipo == "INGRESO") "Ingreso" else "Gasto"
            showToast("$etiqueta guardado exitosamente")
            onSuccess()
        }
    }

    fun deleteMovimiento(id: Long) {
        viewModelScope.launch {
            repository.deleteMovimiento(id)
            showToast("Movimiento eliminado")
        }
    }

    fun addRecordatorio(
        titulo: String,
        tipo: String,
        fechaLimite: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = activeUsuario.value?.pkUsuario
            if (userId == null) {
                showToast("Inicia sesión para crear recordatorios")
                return@launch
            }
            if (titulo.isBlank()) {
                showToast("El título es obligatorio")
                return@launch
            }
            repository.insertRecordatorio(
                Recordatorio(
                    titulo = titulo.trim(),
                    tipo = tipo,
                    fechaLimite = fechaLimite.ifBlank { DateFormats.nowDateTime() },
                    estado = "PENDIENTE",
                    fkUsuario = userId
                )
            )
            showToast("Recordatorio guardado")
            onSuccess()
        }
    }

    fun completarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            val nuevoEstado = if (recordatorio.estado == "PENDIENTE") "COMPLETADO" else "PENDIENTE"
            repository.updateRecordatorio(recordatorio.copy(estado = nuevoEstado))
        }
    }

    fun deleteRecordatorio(id: Long) {
        viewModelScope.launch {
            repository.deleteRecordatorio(id)
            showToast("Recordatorio eliminado")
        }
    }

    fun addPaymentMethod(nombre: String) {
        viewModelScope.launch {
            if (nombre.isNotBlank()) {
                repository.insertMetodoPago(MetodoPago(nombre = nombre.trim()))
                showToast("Cuenta/método guardado")
            }
        }
    }

    fun deletePaymentMethod(id: Long) {
        viewModelScope.launch {
            repository.deleteMetodoPago(id)
            showToast("Método de pago eliminado")
        }
    }

    private fun estaEnPeriodo(movimiento: Movimiento, presupuesto: Presupuesto?): Boolean {
        if (presupuesto == null) return true
        return movimiento.fecha >= presupuesto.fechaInicio && movimiento.fecha <= presupuesto.fechaFin
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
