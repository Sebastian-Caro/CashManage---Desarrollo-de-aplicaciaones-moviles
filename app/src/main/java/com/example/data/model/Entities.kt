package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Metodo_pago")
data class MetodoPago(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_metodo")
    val pkMetodo: Long = 0,
    val nombre: String
)

@Entity(tableName = "Presupuesto")
data class Presupuesto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_presupuesto")
    val pkPresupuesto: Long = 0,
    @ColumnInfo(name = "tipo_periodo")
    val tipoPeriodo: String,
    @ColumnInfo(name = "fecha_inicio")
    val fechaInicio: String,
    @ColumnInfo(name = "fecha_fin")
    val fechaFin: String,
    @ColumnInfo(name = "monto_limite")
    val montoLimite: Double
)

@Entity(
    tableName = "Usuario",
    foreignKeys = [
        ForeignKey(
            entity = Presupuesto::class,
            parentColumns = ["pk_presupuesto"],
            childColumns = ["fk_presupuesto"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["correo"], unique = true), Index("fk_presupuesto")]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_usuario")
    val pkUsuario: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: String,
    @ColumnInfo(name = "fk_presupuesto")
    val fkPresupuesto: Long? = null
)

@Entity(
    tableName = "Presupuesto_metodo",
    primaryKeys = ["fk_presupuesto", "fk_metodo"],
    foreignKeys = [
        ForeignKey(
            entity = Presupuesto::class,
            parentColumns = ["pk_presupuesto"],
            childColumns = ["fk_presupuesto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MetodoPago::class,
            parentColumns = ["pk_metodo"],
            childColumns = ["fk_metodo"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fk_presupuesto"), Index("fk_metodo")]
)
data class PresupuestoMetodo(
    @ColumnInfo(name = "monto_asignado")
    val montoAsignado: Double,
    @ColumnInfo(name = "fk_presupuesto")
    val fkPresupuesto: Long,
    @ColumnInfo(name = "fk_metodo")
    val fkMetodo: Long
)

@Entity(
    tableName = "Movimiento",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["pk_usuario"],
            childColumns = ["fk_usuario"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MetodoPago::class,
            parentColumns = ["pk_metodo"],
            childColumns = ["fk_metodo"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("fk_usuario"), Index("fk_metodo")]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_movimiento")
    val pkMovimiento: Long = 0,
    val tipo: String,
    val monto: Double,
    val fecha: String,
    val hora: String,
    val nota: String? = null,
    @ColumnInfo(name = "fk_usuario")
    val fkUsuario: Long,
    @ColumnInfo(name = "fk_metodo")
    val fkMetodo: Long
)

@Entity(
    tableName = "Recordatorio",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["pk_usuario"],
            childColumns = ["fk_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fk_usuario")]
)
data class Recordatorio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_recordatorio")
    val pkRecordatorio: Long = 0,
    val titulo: String,
    val tipo: String,
    @ColumnInfo(name = "fecha_limite")
    val fechaLimite: String,
    val estado: String = "PENDIENTE",
    @ColumnInfo(name = "fk_usuario")
    val fkUsuario: Long
)

data class MovimientoConMetodo(
    @Embedded val movimiento: Movimiento,
    @ColumnInfo(name = "metodo_nombre")
    val metodoNombre: String
)

data class PresupuestoMetodoDetalle(
    @ColumnInfo(name = "monto_asignado")
    val montoAsignado: Double,
    @ColumnInfo(name = "fk_presupuesto")
    val fkPresupuesto: Long,
    @ColumnInfo(name = "fk_metodo")
    val fkMetodo: Long,
    @ColumnInfo(name = "metodo_nombre")
    val metodoNombre: String
)

data class DesgloseCuenta(
    val fkMetodo: Long,
    val nombre: String,
    val montoAsignado: Double,
    val gastado: Double,
    val ingresado: Double,
    val disponible: Double
)
