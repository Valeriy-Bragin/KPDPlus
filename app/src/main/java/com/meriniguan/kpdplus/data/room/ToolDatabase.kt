package com.meriniguan.kpdplus.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meriniguan.kpdplus.data.room.Tool
import com.meriniguan.kpdplus.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Tool::class], version = 1)
abstract class ToolDatabase : RoomDatabase() {

    abstract fun getToolDao(): ToolDao

}