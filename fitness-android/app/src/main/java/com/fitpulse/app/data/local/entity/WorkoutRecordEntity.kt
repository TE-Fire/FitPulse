package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_record")
data class WorkoutRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long? = null,
    val exerciseId: Long,
    val exerciseName: String,
    val sets: Int = 0,
    val totalReps: Int = 0,
    val totalVolume: Double = 0.0,   // 缓存的训练容量 B维度
    val durationSec: Int = 0,
    val recordedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
