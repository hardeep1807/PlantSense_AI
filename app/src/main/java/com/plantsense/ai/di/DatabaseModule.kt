package com.plantsense.ai.di

import android.content.Context
import androidx.room.Room
import com.plantsense.ai.data.local.PlantDatabase
import com.plantsense.ai.data.local.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlantDatabase {
        return Room.databaseBuilder(
            context,
            PlantDatabase::class.java,
            "plantsense_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideScanHistoryDao(database: PlantDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }
}
