package com.example.data

import android.content.SharedPreferences
import com.example.data.dao.AppDao
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.MovimientoConMetodo
import com.example.data.model.Presupuesto
import com.example.data.model.PresupuestoMetodo
import com.example.data.model.PresupuestoMetodoDetalle
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class AppRepository(
    private val dao: AppDao,
    private val sessionPrefs: SharedPreferences
) {
    companion object {
        private const val KEY_SESSION_USER_ID = "session_user_id"
    }

    private val _sessionUserId = MutableStateFlow(sessionPrefs.getLong(KEY_SESSION_USER_ID, 0L))
    val sessionUserId: StateFlow<Long> = _sessionUserId.asStateFlow()

    val activeUsuario: Flow<Usuario?> = _sessionUserId.flatMapLatest { id ->
        if (id <= 0L) flowOf(null) else dao.getUsuarioById(id)
    }

    val allMetodosPago: Flow<List<MetodoPago>> = dao.getAllMetodosPago()

    val activePresupuesto: Flow<Presupuesto?> = activeUsuario.flatMapLatest { user ->
        val presupuestoId = user?.fkPresupuesto
        if (presupuestoId == null || presupuestoId <= 0L) {
            flowOf(null)
        } else {
            dao.getPresupuestoById(presupuestoId)
        }
    }

    val desglosePresupuesto: Flow<List<PresupuestoMetodoDetalle>> = activeUsuario.flatMapLatest { user ->
        val presupuestoId = user?.fkPresupuesto
        if (presupuestoId == null || presupuestoId <= 0L) {
            flowOf(emptyList())
        } else {
            dao.getDesglosePresupuesto(presupuestoId)
        }
    }

    val movimientosUsuario: Flow<List<MovimientoConMetodo>> = _sessionUserId.flatMapLatest { id ->
        if (id <= 0L) flowOf(emptyList()) else dao.getMovimientosByUsuario(id)
    }

    val recordatoriosUsuario: Flow<List<Recordatorio>> = _sessionUserId.flatMapLatest { id ->
        if (id <= 0L) flowOf(emptyList()) else dao.getRecordatoriosByUsuario(id)
    }

    suspend fun getUsuarioByCorreo(correo: String): Usuario? = dao.getUsuarioByCorreo(correo)

    suspend fun insertUsuario(usuario: Usuario): Long = dao.insertUsuario(usuario)

    suspend fun updateUsuario(usuario: Usuario) = dao.updateUsuario(usuario)

    fun loginUser(userId: Long) {
        sessionPrefs.edit().putLong(KEY_SESSION_USER_ID, userId).apply()
        _sessionUserId.value = userId
    }

    fun logout() {
        sessionPrefs.edit().remove(KEY_SESSION_USER_ID).apply()
        _sessionUserId.value = 0L
    }

    suspend fun insertMetodoPago(metodoPago: MetodoPago): Long = dao.insertMetodoPago(metodoPago)

    suspend fun deleteMetodoPago(id: Long) = dao.deleteMetodoPago(id)

    suspend fun insertPresupuesto(presupuesto: Presupuesto): Long = dao.insertPresupuesto(presupuesto)

    suspend fun updatePresupuesto(presupuesto: Presupuesto) = dao.updatePresupuesto(presupuesto)

    suspend fun replaceDesglose(presupuestoId: Long, items: List<PresupuestoMetodo>) {
        dao.clearDesglosePresupuesto(presupuestoId)
        items.forEach { dao.insertPresupuestoMetodo(it) }
    }

    suspend fun insertPresupuestoMetodo(item: PresupuestoMetodo) = dao.insertPresupuestoMetodo(item)

    suspend fun insertMovimiento(movimiento: Movimiento): Long = dao.insertMovimiento(movimiento)

    suspend fun deleteMovimiento(id: Long) = dao.deleteMovimiento(id)

    suspend fun insertRecordatorio(recordatorio: Recordatorio): Long = dao.insertRecordatorio(recordatorio)

    suspend fun updateRecordatorio(recordatorio: Recordatorio) = dao.updateRecordatorio(recordatorio)

    suspend fun deleteRecordatorio(id: Long) = dao.deleteRecordatorio(id)
}
