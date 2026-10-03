package com.example.data

import com.example.data.dao.AppDao
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {

    // Usuario
    val activeUsuario: Flow<Usuario?> = dao.getActiveUsuario()

    suspend fun getUsuarioByCorreo(correo: String): Usuario? = dao.getUsuarioByCorreo(correo)

    suspend fun insertUsuario(usuario: Usuario): Long = dao.insertUsuario(usuario)

    suspend fun updateUsuario(usuario: Usuario) = dao.updateUsuario(usuario)

    suspend fun logout() = dao.logoutAllUsers()

    suspend fun loginUser(userId: Long) {
        dao.logoutAllUsers()
        dao.setLoggedInUser(userId)
    }

    // Categorias
    val allCategorias: Flow<List<Categoria>> = dao.getAllCategorias()

    suspend fun insertCategoria(categoria: Categoria) = dao.insertCategoria(categoria)

    suspend fun deleteCategoria(id: Long) = dao.deleteCategoria(id)

    // Metodos de Pago
    val allMetodosPago: Flow<List<MetodoPago>> = dao.getAllMetodosPago()

    suspend fun insertMetodoPago(metodoPago: MetodoPago) = dao.insertMetodoPago(metodoPago)

    suspend fun deleteMetodoPago(id: Long) = dao.deleteMetodoPago(id)

    // Presupuesto
    val activePresupuesto: Flow<Presupuesto?> = dao.getActivePresupuesto()

    suspend fun savePresupuesto(presupuesto: Presupuesto) {
        if (presupuesto.id > 0) {
            dao.updatePresupuesto(presupuesto)
        } else {
            dao.insertPresupuesto(presupuesto)
        }
    }

    // Movimientos
    val allMovimientos: Flow<List<Movimiento>> = dao.getAllMovimientos()
    val recentMovimientos: Flow<List<Movimiento>> = dao.getRecentMovimientos()

    suspend fun insertMovimiento(movimiento: Movimiento): Long = dao.insertMovimiento(movimiento)

    suspend fun deleteMovimiento(id: Long) = dao.deleteMovimiento(id)

    // Recordatorios / Clases
    val allRecordatorios: Flow<List<Recordatorio>> = dao.getAllRecordatorios()

    fun getRecordatoriosByDia(dia: String): Flow<List<Recordatorio>> = dao.getRecordatoriosByDia(dia)

    suspend fun insertRecordatorio(recordatorio: Recordatorio): Long = dao.insertRecordatorio(recordatorio)

    suspend fun updateRecordatorio(recordatorio: Recordatorio) = dao.updateRecordatorio(recordatorio)

    suspend fun deleteRecordatorio(id: Long) = dao.deleteRecordatorio(id)

    // Notificaciones
    val allNotificaciones: Flow<List<Notificacion>> = dao.getAllNotificaciones()

    suspend fun insertNotificacion(notificacion: Notificacion) = dao.insertNotificacion(notificacion)

    suspend fun markNotificacionAsRead(id: Long) = dao.markNotificacionAsRead(id)

    suspend fun deleteNotificacion(id: Long) = dao.deleteNotificacion(id)

    suspend fun clearAllNotificaciones() = dao.clearAllNotificaciones()
}
