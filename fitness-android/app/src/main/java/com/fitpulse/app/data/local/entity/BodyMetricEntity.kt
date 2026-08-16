package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_metric")
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Double? = null,     // A维度重点
    val bodyFatPct: Double? = null,   // A维度重点
    val muscleKg: Double? = null,
    val recordedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
