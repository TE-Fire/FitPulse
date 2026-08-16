package com.fitpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitpulse.app.data.local.entity.BodyMetricEntity
import com.fitpulse.app.data.local.entity.WorkoutRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Workout records - B/C维度查询
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(entity: WorkoutRecordEntity)

    @Query("SELECT * FROM workout_record WHERE recordedAt >= :from ORDER BY recordedAt DESC")
    fun observeWorkoutsFrom(from: Long): Flow<List<WorkoutRecordEntity>>

    // Body metrics - A维度查询
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMetric(entity: BodyMetricEntity)

    @Query("SELECT * FROM body_metric ORDER BY recordedAt DESC LIMIT 30")
    fun observeBodyMetrics30d(): Flow<List<BodyMetricEntity>>
}
