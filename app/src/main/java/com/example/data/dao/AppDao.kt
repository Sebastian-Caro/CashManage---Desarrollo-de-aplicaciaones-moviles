package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.MovimientoConMetodo
import com.example.data.model.Presupuesto
import com.example.data.model.PresupuestoMetodo
import com.example.data.model.PresupuestoMetodoDetalle
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM Usuario WHERE pk_usuario = :id LIMIT 1")
    fun getUsuarioById(id: Long): Flow<Usuario?>

    @Query("SELECT * FROM Usuario WHERE correo = :correo LIMIT 1")
    suspend fun getUsuarioByCorreo(correo: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM Metodo_pago ORDER BY pk_metodo ASC")
    fun getAllMetodosPago(): Flow<List<MetodoPago>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetodoPago(metodoPago: MetodoPago): Long

    @Query("DELETE FROM Metodo_pago WHERE pk_metodo = :id")
    suspend fun deleteMetodoPago(id: Long)

    @Query("SELECT * FROM Presupuesto WHERE pk_presupuesto = :id LIMIT 1")
    fun getPresupuestoById(id: Long): Flow<Presupuesto?>

    @Query("SELECT * FROM Presupuesto WHERE pk_presupuesto = :id LIMIT 1")
    suspend fun getPresupuestoByIdOnce(id: Long): Presupuesto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuesto(presupuesto: Presupuesto): Long

    @Update
    suspend fun updatePresupuesto(presupuesto: Presupuesto)

    @Query(
        """
        SELECT pm.monto_asignado AS monto_asignado,
               pm.fk_presupuesto AS fk_presupuesto,
               pm.fk_metodo AS fk_metodo,
               m.nombre AS metodo_nombre
        FROM Presupuesto_metodo pm
        INNER JOIN Metodo_pago m ON m.pk_metodo = pm.fk_metodo
        WHERE pm.fk_presupuesto = :presupuestoId
        ORDER BY pm.fk_metodo ASC
        """
    )
    fun getDesglosePresupuesto(presupuestoId: Long): Flow<List<PresupuestoMetodoDetalle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuestoMetodo(item: PresupuestoMetodo)

    @Query("DELETE FROM Presupuesto_metodo WHERE fk_presupuesto = :presupuestoId")
    suspend fun clearDesglosePresupuesto(presupuestoId: Long)

    @Query(
        """
        SELECT Movimiento.*, Metodo_pago.nombre AS metodo_nombre
        FROM Movimiento
        INNER JOIN Metodo_pago ON Movimiento.fk_metodo = Metodo_pago.pk_metodo
        WHERE Movimiento.fk_usuario = :userId
        ORDER BY Movimiento.fecha DESC, Movimiento.hora DESC
        """
    )
    fun getMovimientosByUsuario(userId: Long): Flow<List<MovimientoConMetodo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovimiento(movimiento: Movimiento): Long

    @Query("DELETE FROM Movimiento WHERE pk_movimiento = :id")
    suspend fun deleteMovimiento(id: Long)

    @Query(
        """
        SELECT * FROM Recordatorio
        WHERE fk_usuario = :userId
        ORDER BY fecha_limite ASC
        """
    )
    fun getRecordatoriosByUsuario(userId: Long): Flow<List<Recordatorio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordatorio(recordatorio: Recordatorio): Long

    @Update
    suspend fun updateRecordatorio(recordatorio: Recordatorio)

    @Query("DELETE FROM Recordatorio WHERE pk_recordatorio = :id")
    suspend fun deleteRecordatorio(id: Long)
}
