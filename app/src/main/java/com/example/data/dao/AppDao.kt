package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Categoria
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Notificacion
import com.example.data.model.Presupuesto
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Usuario ---
    @Query("SELECT * FROM usuario WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUsuario(): Flow<Usuario?>

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
    @Query("SELECT * FROM categorias ORDER BY id ASC")
    fun getAllCategorias(): Flow<List<Categoria>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: Categoria): Long

    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun deleteCategoria(id: Long)

    // --- Métodos de Pago ---
    @Query("SELECT * FROM metodos_pago ORDER BY id ASC")
    fun getAllMetodosPago(): Flow<List<MetodoPago>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetodoPago(metodoPago: MetodoPago): Long

    @Query("DELETE FROM metodos_pago WHERE id = :id")
    suspend fun deleteMetodoPago(id: Long)

    // --- Presupuesto ---
    @Query("SELECT * FROM presupuesto WHERE activo = 1 ORDER BY id DESC LIMIT 1")
    fun getActivePresupuesto(): Flow<Presupuesto?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuesto(presupuesto: Presupuesto): Long

    @Update
    suspend fun updatePresupuesto(presupuesto: Presupuesto)

    // --- Movimientos ---
    @Query("SELECT * FROM movimientos ORDER BY fechaTimestamp DESC")
    fun getAllMovimientos(): Flow<List<Movimiento>>

    @Query("SELECT * FROM movimientos ORDER BY fechaTimestamp DESC LIMIT 3")
    fun getRecentMovimientos(): Flow<List<Movimiento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovimiento(movimiento: Movimiento): Long

    @Query("DELETE FROM movimientos WHERE id = :id")
    suspend fun deleteMovimiento(id: Long)

    // --- Recordatorios / Clases ---
    @Query("SELECT * FROM recordatorio ORDER BY id ASC")
    fun getAllRecordatorios(): Flow<List<Recordatorio>>

    @Query("SELECT * FROM recordatorio WHERE diaSemana = :diaSemana ORDER BY id ASC")
    fun getRecordatoriosByDia(diaSemana: String): Flow<List<Recordatorio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordatorio(recordatorio: Recordatorio): Long

    @Update
    suspend fun updateRecordatorio(recordatorio: Recordatorio)

    @Query("DELETE FROM recordatorio WHERE id = :id")
    suspend fun deleteRecordatorio(id: Long)

    // --- Notificaciones ---
    @Query("SELECT * FROM notificaciones ORDER BY fechaTimestamp DESC")
    fun getAllNotificaciones(): Flow<List<Notificacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacion(notificacion: Notificacion): Long

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun markNotificacionAsRead(id: Long)

    @Query("DELETE FROM notificaciones WHERE id = :id")
    suspend fun deleteNotificacion(id: Long)

    @Query("DELETE FROM notificaciones")
    suspend fun clearAllNotificaciones()
}
