package com.anix.vidflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anix.vidflow.data.local.dao.VideoDao
import com.anix.vidflow.data.local.entity.VideoEntity

@Database(entities = [VideoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
