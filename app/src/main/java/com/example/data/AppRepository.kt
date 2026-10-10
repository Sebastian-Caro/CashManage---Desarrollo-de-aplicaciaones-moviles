package com.example.data

import com.example.data.dao.AppDao
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class AppRepository(private val dao: AppDao) {

    val activeUsuario: Flow<Usuario?> = dao.getActiveUsuario()

    suspend fun getUsuarioByCorreo(correo: String): Usuario? = dao.getUsuarioByCorreo(correo)

    suspend fun insertUsuario(usuario: Usuario): Long = dao.insertUsuario(usuario)

    suspend fun updateUsuario(usuario: Usuario) = dao.updateUsuario(usuario)

    suspend fun logout() = dao.logoutAllUsers()

    suspend fun loginUser(userId: Long) {
        dao.logoutAllUsers()
        dao.setLoggedInUser(userId)
    }

    suspend fun ensureUserDefaults(usuarioId: Long) {
        if (dao.getCategoriaCount(usuarioId) == 0) {
            listOf(
                Categoria(nombre = "Alimentación", icono = "restaurant", colorHex = "#1E5C42", usuarioId = usuarioId),
                Categoria(nombre = "Transporte", icono = "directions_bus", colorHex = "#2E9B66", usuarioId = usuarioId),
                Categoria(nombre = "Academia", icono = "menu_book", colorHex = "#006D42", usuarioId = usuarioId),
                Categoria(nombre = "Ocio", icono = "sports_esports", colorHex = "#45554D", usuarioId = usuarioId),
                Categoria(nombre = "Otros", icono = "more_horiz", colorHex = "#707973", usuarioId = usuarioId)
            ).forEach { dao.insertCategoria(it.copy(usuarioId = usuarioId)) }
        }
        if (dao.getMetodoPagoCount(usuarioId) == 0) {
            listOf(
                MetodoPago(nombre = "Efectivo", icono = "payments", tipo = "EFECTIVO", usuarioId = usuarioId),
                MetodoPago(nombre = "Digital", icono = "account_balance_wallet", tipo = "DIGITAL", usuarioId = usuarioId),
                MetodoPago(nombre = "Débito", icono = "credit_card", tipo = "DEBITO", usuarioId = usuarioId)
            ).forEach { dao.insertMetodoPago(it.copy(usuarioId = usuarioId)) }
        }
        if (dao.getActivePresupuestoCount(usuarioId) == 0) {
            dao.insertPresupuesto(Presupuesto(usuarioId = usuarioId))
        }
    }

    suspend fun initializeBudget(
        usuarioId: Long,
        balances: Map<Long, Double>,
        total: Double,
        fechaInicioMillis: Long
    ): Boolean = dao.initializeBudget(usuarioId, balances, total, fechaInicioMillis)

    val allCategorias: Flow<List<Categoria>> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getAllCategorias(it.id) } ?: flowOf(emptyList())
    }

    suspend fun insertCategoria(categoria: Categoria, usuarioId: Long) =
        dao.insertCategoria(categoria.copy(usuarioId = usuarioId))

    suspend fun deleteCategoria(id: Long, usuarioId: Long) = dao.deleteCategoria(id, usuarioId)

    val allMetodosPago: Flow<List<MetodoPago>> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getAllMetodosPago(it.id) } ?: flowOf(emptyList())
    }

    suspend fun insertMetodoPago(metodoPago: MetodoPago, usuarioId: Long) =
        dao.insertMetodoPago(metodoPago.copy(usuarioId = usuarioId))

    suspend fun deleteMetodoPago(id: Long, usuarioId: Long) =
        dao.deletePaymentMethodIfUnused(id, usuarioId)

    val activePresupuesto: Flow<Presupuesto?> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getActivePresupuesto(it.id) } ?: flowOf(null)
    }

    suspend fun savePresupuesto(presupuesto: Presupuesto, usuarioId: Long) {
        val ownedBudget = presupuesto.copy(usuarioId = usuarioId)
        if (ownedBudget.id > 0) {
            dao.updatePresupuesto(
                id = ownedBudget.id,
                usuarioId = usuarioId,
                periodo = ownedBudget.periodo,
                metaTotal = ownedBudget.metaTotal,
                alertaPorcentaje = ownedBudget.alertaPorcentaje,
                activo = ownedBudget.activo,
                fechaInicioMillis = ownedBudget.fechaInicioMillis,
                configurado = ownedBudget.configurado
            )
        } else {
            dao.insertPresupuesto(ownedBudget)
        }
    }

    val allMovimientos: Flow<List<Movimiento>> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getAllMovimientos(it.id) } ?: flowOf(emptyList())
    }
    val recentMovimientos: Flow<List<Movimiento>> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getRecentMovimientos(it.id) } ?: flowOf(emptyList())
    }

    suspend fun registerExpense(movimiento: Movimiento, usuarioId: Long) =
        dao.registerExpense(movimiento.copy(usuarioId = usuarioId))

    suspend fun insertMovimiento(movimiento: Movimiento, usuarioId: Long): Long =
        dao.insertMovimiento(movimiento.copy(usuarioId = usuarioId))

    suspend fun deleteMovimiento(id: Long, usuarioId: Long) =
        dao.deleteMovimientoAndRestoreBalance(id, usuarioId)

    val allRecordatorios: Flow<List<Recordatorio>> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getAllRecordatorios(it.id) } ?: flowOf(emptyList())
    }

    fun getRecordatoriosByDia(dia: String): Flow<List<Recordatorio>> =
        activeUsuario.flatMapLatest { user ->
            user?.let { dao.getRecordatoriosByDia(dia, it.id) } ?: flowOf(emptyList())
        }

    suspend fun insertRecordatorio(recordatorio: Recordatorio, usuarioId: Long): Long =
        dao.insertRecordatorio(recordatorio.copy(usuarioId = usuarioId))

    suspend fun updateRecordatorio(recordatorio: Recordatorio, usuarioId: Long) {
        dao.updateRecordatorio(
            id = recordatorio.id,
            usuarioId = usuarioId,
            titulo = recordatorio.titulo,
            tipo = recordatorio.tipo,
            diaSemana = recordatorio.diaSemana,
            diaNumero = recordatorio.diaNumero,
            horaInicio = recordatorio.horaInicio,
            horaFin = recordatorio.horaFin,
            aula = recordatorio.aula,
            grupoOProfesor = recordatorio.grupoOProfesor,
            facultad = recordatorio.facultad,
            modalidad = recordatorio.modalidad,
            notificarMinutosAntes = recordatorio.notificarMinutosAntes,
            piso = recordatorio.piso,
            activo = recordatorio.activo
        )
    }

    suspend fun deleteRecordatorio(id: Long, usuarioId: Long) =
        dao.deleteRecordatorio(id, usuarioId)

    val allNotificaciones: Flow<List<Notificacion>> = activeUsuario.flatMapLatest { user ->
        user?.let { dao.getAllNotificaciones(it.id) } ?: flowOf(emptyList())
    }

    suspend fun insertNotificacion(notificacion: Notificacion, usuarioId: Long): Long =
        dao.insertNotificacion(notificacion.copy(usuarioId = usuarioId))

    suspend fun markNotificacionAsRead(id: Long, usuarioId: Long) =
        dao.markNotificacionAsRead(id, usuarioId)

    suspend fun deleteNotificacion(id: Long, usuarioId: Long) =
        dao.deleteNotificacion(id, usuarioId)

    suspend fun clearAllNotificaciones(usuarioId: Long) =
        dao.clearAllNotificaciones(usuarioId)
}
