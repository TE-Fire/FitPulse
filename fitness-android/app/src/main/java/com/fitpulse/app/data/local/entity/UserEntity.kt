package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val height: Double? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
