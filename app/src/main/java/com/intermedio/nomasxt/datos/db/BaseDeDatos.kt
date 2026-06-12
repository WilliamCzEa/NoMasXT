package com.intermedio.nomasxt.datos.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.intermedio.nomasxt.datos.dao.CatalogoEdadesDao
import com.intermedio.nomasxt.datos.dao.CatalogoEstadosDao
import com.intermedio.nomasxt.datos.dao.CatalogoFAQDao
import com.intermedio.nomasxt.datos.dao.CatalogoIncidentesDao
import com.intermedio.nomasxt.datos.dao.NumerosDao
import com.intermedio.nomasxt.datos.dao.PerfilDao
import com.intermedio.nomasxt.datos.dao.ReportesDao
import com.intermedio.nomasxt.datos.entity.CatalogoEdadesEntity
import com.intermedio.nomasxt.datos.entity.CatalogoEstadosEntity
import com.intermedio.nomasxt.datos.entity.CatalogoFAQEntity
import com.intermedio.nomasxt.datos.entity.CatalogoIncidentesEntity
import com.intermedio.nomasxt.datos.entity.NumerosEntity
import com.intermedio.nomasxt.datos.entity.PerfilEntity
import com.intermedio.nomasxt.datos.entity.ReportesEntity

@Database(
    entities = [
        NumerosEntity::class,
        CatalogoEdadesEntity::class,
        CatalogoFAQEntity::class,
        CatalogoIncidentesEntity::class,
        CatalogoEstadosEntity::class,
        PerfilEntity::class,
        ReportesEntity::class
    ],
    version = 1
)
abstract class BaseDeDatos: RoomDatabase() {
    //DAO
    abstract fun numerosDao(): NumerosDao
    abstract fun catalogoEdadesDao(): CatalogoEdadesDao
    abstract fun catalogoFAQDao(): CatalogoFAQDao
    abstract fun catalogoIncidentesDao(): CatalogoIncidentesDao
    abstract fun catalogoEstadosDao(): CatalogoEstadosDao
    abstract fun perfilDao(): PerfilDao
    abstract fun reportesDao(): ReportesDao
}
