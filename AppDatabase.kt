package com.intermedio.nomasxt.datos.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.intermedio.nomasxt.datos.dao.NumerosDao
import com.intermedio.nomasxt.datos.dao.ReportesDao
import com.intermedio.nomasxt.datos.entity.NumerosEntity
import com.intermedio.nomasxt.datos.entity.ReportesEntity

@Database(
    entities = [NumerosEntity::class, ReportesEntity::class],
    version = 2, // Incrementamos la versión de la base de datos
    exportSchema = true // Siempre es buena práctica mantener esto en true para generar el esquema
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun numerosDao(): NumerosDao
    abstract fun reportesDao(): ReportesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nomasxt_database"
                )
                .addMigrations(MIGRATION_1_2) // Agregamos nuestra migración aquí
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Definición de la migración de la versión 1 a la versión 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Prepend '+52' a los números que no tienen un prefijo de país
                database.execSQL("UPDATE numeros SET numero = '+52' || numero WHERE numero NOT LIKE '+%'")
                database.execSQL("UPDATE reportes SET numeroReportado = '+52' || numeroReportado WHERE numeroReportado NOT LIKE '+%'")
            }
        }
    }
}