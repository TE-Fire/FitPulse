package com.fitpulse.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fitpulse.app.data.local.dao.AppDao
import com.fitpulse.app.data.local.entity.BodyMetricEntity
import com.fitpulse.app.data.local.entity.UserEntity
import com.fitpulse.app.data.local.entity.WorkoutRecordEntity

@Database(
    entities = [
        UserEntity::class,
        WorkoutRecordEntity::class,
        BodyMetricEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
