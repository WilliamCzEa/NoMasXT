package com.intermedio.nomasxt.datos.di

import android.content.Context
import androidx.room.Room
import com.intermedio.nomasxt.datos.dao.NumerosDao
import com.intermedio.nomasxt.datos.dao.CatalogoEdadesDao
import com.intermedio.nomasxt.datos.dao.CatalogoIncidentesDao
import com.intermedio.nomasxt.datos.dao.CatalogoFAQDao
import com.intermedio.nomasxt.datos.dao.CatalogoEstadosDao
import com.intermedio.nomasxt.datos.dao.PerfilDao
import com.intermedio.nomasxt.datos.dao.ReportesDao
import com.intermedio.nomasxt.datos.db.BaseDeDatos
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BaseDeDatosModule {

    @Provides
    @Singleton
    fun provideBaseDeDatos(@ApplicationContext appContext: Context): BaseDeDatos {
        return Room.databaseBuilder(appContext, BaseDeDatos::class.java, "BaseDeDatos").build()
    }

    //DAOs para proveer
    @Provides
    @Singleton
    fun provideNumerosDao(baseDeDatos: BaseDeDatos): NumerosDao {
        return baseDeDatos.numerosDao()
    }

    @Provides
    @Singleton
    fun provideCatalogoEdadesDao(baseDeDatos: BaseDeDatos): CatalogoEdadesDao {
        return baseDeDatos.catalogoEdadesDao()
    }

    @Provides
    @Singleton
    fun provideCatalogoIncidentesDao(baseDeDatos: BaseDeDatos): CatalogoIncidentesDao {
        return baseDeDatos.catalogoIncidentesDao()
    }

    @Provides
    @Singleton
    fun provideCatalogoFAQDao(baseDeDatos: BaseDeDatos): CatalogoFAQDao {
        return baseDeDatos.catalogoFAQDao()
    }
    
    @Provides
    @Singleton
    fun provideCatalogoEstadosDao(baseDeDatos: BaseDeDatos): CatalogoEstadosDao {
        return baseDeDatos.catalogoEstadosDao()
    }

    @Provides
    @Singleton
    fun providePerfilDao(baseDeDatos: BaseDeDatos): PerfilDao {
        return baseDeDatos.perfilDao()
    }

    @Provides
    @Singleton
    fun provideReportesDao(baseDeDatos: BaseDeDatos): ReportesDao {
        return baseDeDatos.reportesDao()
    }
}
