package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.model.MetodoPago
import com.example.data.model.Movimiento
import com.example.data.model.Presupuesto
import com.example.data.model.PresupuestoMetodo
import com.example.data.model.Recordatorio
import com.example.data.model.Usuario
import com.example.security.PasswordSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MetodoPago::class,
        Presupuesto::class,
        Usuario::class,
        PresupuestoMetodo::class,
        Movimiento::class,
        Recordatorio::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_manager_db_v2"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            val efectivoId = dao.insertMetodoPago(MetodoPago(nombre = "Efectivo"))
            val nequiId = dao.insertMetodoPago(MetodoPago(nombre = "Nequi"))
            val bancoNuId = dao.insertMetodoPago(MetodoPago(nombre = "Banco Nu"))

            val presupuestoId = dao.insertPresupuesto(
                Presupuesto(
                    tipoPeriodo = "MENSUAL",
                    fechaInicio = DateFormats.monthStart(),
                    fechaFin = DateFormats.monthEnd(),
                    montoLimite = 100000.0
                )
            )

            dao.insertPresupuestoMetodo(
                PresupuestoMetodo(montoAsignado = 50000.0, fkPresupuesto = presupuestoId, fkMetodo = nequiId)
            )
            dao.insertPresupuestoMetodo(
                PresupuestoMetodo(montoAsignado = 40000.0, fkPresupuesto = presupuestoId, fkMetodo = efectivoId)
            )
            dao.insertPresupuestoMetodo(
                PresupuestoMetodo(montoAsignado = 10000.0, fkPresupuesto = presupuestoId, fkMetodo = bancoNuId)
            )

            val userId = dao.insertUsuario(
                Usuario(
                    nombre = "Alejandro Morales",
                    correo = "estudiante@universidad.edu",
                    contrasena = PasswordSecurity.hash("123456"),
                    fechaRegistro = DateFormats.today(),
                    fkPresupuesto = presupuestoId
                )
            )

            dao.insertMovimiento(
                Movimiento(
                    tipo = "GASTO",
                    monto = 14500.0,
                    fecha = DateFormats.today(),
                    hora = "13:30:00",
                    nota = "Almuerzo cafetería central",
                    fkUsuario = userId,
                    fkMetodo = nequiId
                )
            )
            dao.insertMovimiento(
                Movimiento(
                    tipo = "GASTO",
                    monto = 8000.0,
                    fecha = DateFormats.today(),
                    hora = "08:15:00",
                    nota = "Recarga de transporte",
                    fkUsuario = userId,
                    fkMetodo = efectivoId
                )
            )
            dao.insertMovimiento(
                Movimiento(
                    tipo = "INGRESO",
                    monto = 20000.0,
                    fecha = DateFormats.today(),
                    hora = "09:00:00",
                    nota = "Mesada semanal",
                    fkUsuario = userId,
                    fkMetodo = bancoNuId
                )
            )

            dao.insertRecordatorio(
                Recordatorio(
                    titulo = "Entrega de Economía Aplicada",
                    tipo = "ENTREGA",
                    fechaLimite = "${DateFormats.today()} 18:00:00",
                    estado = "PENDIENTE",
                    fkUsuario = userId
                )
            )
            dao.insertRecordatorio(
                Recordatorio(
                    titulo = "Parcial de Cálculo Multivariable",
                    tipo = "PARCIAL",
                    fechaLimite = "${DateFormats.today()} 10:00:00",
                    estado = "PENDIENTE",
                    fkUsuario = userId
                )
            )
        }
    }
}
