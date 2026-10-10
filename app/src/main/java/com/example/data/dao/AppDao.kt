package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.Flow

enum class ExpenseRegistrationResult {
    SUCCESS,
    BUDGET_NOT_CONFIGURED,
    PAYMENT_METHOD_NOT_FOUND,
    INSUFFICIENT_METHOD_BALANCE,
    BUDGET_LIMIT_EXCEEDED,
    INVALID_AMOUNT
}

@Dao
interface AppDao {

    // --- Usuario ---
    @Query("SELECT * FROM usuario WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUsuario(): Flow<Usuario?>

    @Query("SELECT * FROM usuario WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUsuarioOnce(): Usuario?

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun getUsuarioByCorreo(correo: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("UPDATE usuario SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE usuario SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setLoggedInUser(userId: Long)

    // --- Categorias ---
    @Query("SELECT * FROM categorias WHERE usuarioId = :usuarioId ORDER BY id ASC")
    fun getAllCategorias(usuarioId: Long): Flow<List<Categoria>>

    @Query("SELECT COUNT(*) FROM categorias WHERE usuarioId = :usuarioId")
    suspend fun getCategoriaCount(usuarioId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: Categoria): Long

    @Query("DELETE FROM categorias WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun deleteCategoria(id: Long, usuarioId: Long)

    // --- Métodos de Pago ---
    @Query("SELECT * FROM metodos_pago WHERE usuarioId = :usuarioId ORDER BY id ASC")
    fun getAllMetodosPago(usuarioId: Long): Flow<List<MetodoPago>>

    @Query("SELECT COUNT(*) FROM metodos_pago WHERE usuarioId = :usuarioId")
    suspend fun getMetodoPagoCount(usuarioId: Long): Int

    @Query("SELECT * FROM metodos_pago WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    suspend fun getMetodoPago(id: Long, usuarioId: Long): MetodoPago?

    @Query("SELECT * FROM metodos_pago WHERE usuarioId = :usuarioId ORDER BY id ASC")
    suspend fun getMetodosPago(usuarioId: Long): List<MetodoPago>

    @Query("UPDATE metodos_pago SET saldoActual = :saldo WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun setMetodoPagoSaldo(id: Long, usuarioId: Long, saldo: Double): Int

    @Query("UPDATE metodos_pago SET saldoActual = saldoActual - :monto WHERE id = :id AND usuarioId = :usuarioId AND saldoActual >= :monto")
    suspend fun descontarSaldoMetodoPago(id: Long, usuarioId: Long, monto: Double): Int

    @Query("UPDATE metodos_pago SET saldoActual = saldoActual + :monto WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun reponerSaldoMetodoPago(id: Long, usuarioId: Long, monto: Double): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetodoPago(metodoPago: MetodoPago): Long

    @Query("DELETE FROM metodos_pago WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun deleteMetodoPago(id: Long, usuarioId: Long)

    @Query("SELECT COUNT(*) FROM movimientos WHERE metodoPagoId = :metodoPagoId AND usuarioId = :usuarioId")
    suspend fun getMovimientoCountForPaymentMethod(metodoPagoId: Long, usuarioId: Long): Int

    @Transaction
    suspend fun deletePaymentMethodIfUnused(id: Long, usuarioId: Long): Boolean {
        if (getMovimientoCountForPaymentMethod(id, usuarioId) > 0) return false
        deleteMetodoPago(id, usuarioId)
        return true
    }

    // --- Presupuesto ---
    @Query("SELECT * FROM presupuesto WHERE activo = 1 AND usuarioId = :usuarioId ORDER BY id DESC LIMIT 1")
    fun getActivePresupuesto(usuarioId: Long): Flow<Presupuesto?>

    @Query("SELECT COUNT(*) FROM presupuesto WHERE activo = 1 AND usuarioId = :usuarioId")
    suspend fun getActivePresupuestoCount(usuarioId: Long): Int

    @Query("SELECT * FROM presupuesto WHERE activo = 1 AND usuarioId = :usuarioId ORDER BY id DESC LIMIT 1")
    suspend fun getActivePresupuestoOnce(usuarioId: Long): Presupuesto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuesto(presupuesto: Presupuesto): Long

    @Query("UPDATE presupuesto SET periodo = :periodo, metaTotal = :metaTotal, alertaPorcentaje = :alertaPorcentaje, activo = :activo, fechaInicioMillis = :fechaInicioMillis, configurado = :configurado WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun updatePresupuesto(
        id: Long,
        usuarioId: Long,
        periodo: String,
        metaTotal: Double,
        alertaPorcentaje: Int,
        activo: Boolean,
        fechaInicioMillis: Long,
        configurado: Boolean
    )

    @Transaction
    suspend fun initializeBudget(
        usuarioId: Long,
        balances: Map<Long, Double>,
        total: Double,
        fechaInicioMillis: Long
    ): Boolean {
        if (
            !total.isFinite() ||
            total <= 0.0 ||
            balances.values.any { !it.isFinite() || it < 0.0 } ||
            kotlin.math.abs(balances.values.sum() - total) > 0.001
        ) return false
        val metodos = getMetodosPago(usuarioId)
        if (metodos.isEmpty() || metodos.map { it.id }.toSet() != balances.keys) return false
        metodos.forEach { metodo ->
            val saldo = balances[metodo.id] ?: return false
            if (saldo < 0.0 || setMetodoPagoSaldo(metodo.id, usuarioId, saldo) != 1) return false
        }

        val current = getActivePresupuestoOnce(usuarioId)
        if (current == null) {
            insertPresupuesto(
                Presupuesto(
                    metaTotal = total,
                    fechaInicioMillis = fechaInicioMillis,
                    configurado = true,
                    usuarioId = usuarioId
                )
            )
        } else {
            updatePresupuesto(
                id = current.id,
                usuarioId = usuarioId,
                periodo = current.periodo,
                metaTotal = total,
                alertaPorcentaje = current.alertaPorcentaje,
                activo = current.activo,
                fechaInicioMillis = fechaInicioMillis,
                configurado = true
            )
        }
        return true
    }

    // --- Movimientos ---
    @Query("SELECT * FROM movimientos WHERE usuarioId = :usuarioId ORDER BY fechaTimestamp DESC")
    fun getAllMovimientos(usuarioId: Long): Flow<List<Movimiento>>

    @Query("SELECT * FROM movimientos WHERE usuarioId = :usuarioId ORDER BY fechaTimestamp DESC LIMIT 3")
    fun getRecentMovimientos(usuarioId: Long): Flow<List<Movimiento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovimiento(movimiento: Movimiento): Long

    @Query("SELECT COALESCE(SUM(monto), 0) FROM movimientos WHERE usuarioId = :usuarioId AND tipo = 'GASTO' AND fechaTimestamp >= :desde")
    suspend fun getGastosDesde(usuarioId: Long, desde: Long): Double

    @Transaction
    suspend fun registerExpense(movimiento: Movimiento): ExpenseRegistrationResult {
        if (movimiento.tipo != "GASTO" || !movimiento.monto.isFinite() || movimiento.monto <= 0.0) {
            return ExpenseRegistrationResult.INVALID_AMOUNT
        }
        val usuarioId = movimiento.usuarioId
        val presupuesto = getActivePresupuestoOnce(usuarioId)
            ?.takeIf { it.configurado }
            ?: return ExpenseRegistrationResult.BUDGET_NOT_CONFIGURED
        val metodoPagoId = movimiento.metodoPagoId
            ?: return ExpenseRegistrationResult.PAYMENT_METHOD_NOT_FOUND
        val metodo = getMetodoPago(metodoPagoId, usuarioId)
            ?: return ExpenseRegistrationResult.PAYMENT_METHOD_NOT_FOUND
        if (metodo.saldoActual < movimiento.monto) {
            return ExpenseRegistrationResult.INSUFFICIENT_METHOD_BALANCE
        }
        val gastos = getGastosDesde(usuarioId, presupuesto.fechaInicioMillis)
        if (gastos + movimiento.monto > presupuesto.metaTotal * 0.8) {
            return ExpenseRegistrationResult.BUDGET_LIMIT_EXCEEDED
        }
        if (descontarSaldoMetodoPago(metodoPagoId, usuarioId, movimiento.monto) != 1) {
            return ExpenseRegistrationResult.INSUFFICIENT_METHOD_BALANCE
        }
        insertMovimiento(movimiento)
        return ExpenseRegistrationResult.SUCCESS
    }

    @Query("SELECT * FROM movimientos WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    suspend fun getMovimiento(id: Long, usuarioId: Long): Movimiento?

    @Query("DELETE FROM movimientos WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun deleteMovimientoRaw(id: Long, usuarioId: Long): Int

    @Transaction
    suspend fun deleteMovimientoAndRestoreBalance(id: Long, usuarioId: Long): Boolean {
        val movimiento = getMovimiento(id, usuarioId) ?: return false
        if (deleteMovimientoRaw(id, usuarioId) != 1) return false

        val presupuesto = getActivePresupuestoOnce(usuarioId)
        if (
            movimiento.tipo == "GASTO" &&
            presupuesto?.configurado == true &&
            movimiento.fechaTimestamp >= presupuesto.fechaInicioMillis
        ) {
            movimiento.metodoPagoId?.let { metodoPagoId ->
                reponerSaldoMetodoPago(metodoPagoId, usuarioId, movimiento.monto)
            }
        }
        return true
    }

    // --- Recordatorios / Clases ---
    @Query("SELECT * FROM recordatorio WHERE usuarioId = :usuarioId ORDER BY id ASC")
    fun getAllRecordatorios(usuarioId: Long): Flow<List<Recordatorio>>

    @Query("SELECT * FROM recordatorio WHERE usuarioId = :usuarioId AND activo = 1 ORDER BY id ASC")
    suspend fun getActiveRecordatoriosOnce(usuarioId: Long): List<Recordatorio>

    @Query("SELECT * FROM recordatorio WHERE diaSemana = :diaSemana AND usuarioId = :usuarioId ORDER BY id ASC")
    fun getRecordatoriosByDia(diaSemana: String, usuarioId: Long): Flow<List<Recordatorio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordatorio(recordatorio: Recordatorio): Long

    @Query("UPDATE recordatorio SET titulo = :titulo, tipo = :tipo, diaSemana = :diaSemana, diaNumero = :diaNumero, horaInicio = :horaInicio, horaFin = :horaFin, aula = :aula, grupoOProfesor = :grupoOProfesor, facultad = :facultad, modalidad = :modalidad, notificarMinutosAntes = :notificarMinutosAntes, piso = :piso, activo = :activo WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun updateRecordatorio(
        id: Long,
        usuarioId: Long,
        titulo: String,
        tipo: String,
        diaSemana: String,
        diaNumero: Int,
        horaInicio: String,
        horaFin: String,
        aula: String,
        grupoOProfesor: String,
        facultad: String,
        modalidad: String,
        notificarMinutosAntes: Int,
        piso: String,
        activo: Boolean
    )

    @Query("DELETE FROM recordatorio WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun deleteRecordatorio(id: Long, usuarioId: Long)

    // --- Notificaciones ---
    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId ORDER BY fechaTimestamp DESC")
    fun getAllNotificaciones(usuarioId: Long): Flow<List<Notificacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacion(notificacion: Notificacion): Long

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun markNotificacionAsRead(id: Long, usuarioId: Long)

    @Query("DELETE FROM notificaciones WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun deleteNotificacion(id: Long, usuarioId: Long)

    @Query("DELETE FROM notificaciones WHERE usuarioId = :usuarioId")
    suspend fun clearAllNotificaciones(usuarioId: Long)
}
