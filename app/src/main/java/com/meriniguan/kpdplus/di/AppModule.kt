package com.meriniguan.kpdplus.di

import android.app.Application
import androidx.room.Room
import com.meriniguan.kpdplus.data.room.ToolDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application
    ) = Room.databaseBuilder(app, ToolDatabase::class.java, "tool_database")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideTaskDao(
        taskDatabase: ToolDatabase
    ) = taskDatabase.getToolDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope