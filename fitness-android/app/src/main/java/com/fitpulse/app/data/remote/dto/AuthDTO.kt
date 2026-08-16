package com.fitpulse.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?,
    @SerializedName("timestamp") val timestamp: Long
)

object AuthDTO {
    data class LoginReq(
        val username: String,
        val password: String
    )

    data class RegisterReq(
        val username: String,
        val email: String? = null,
        val password: String
    )

    data class LoginResp(
        @SerializedName("accessToken") val accessToken: String,
        @SerializedName("refreshToken") val refreshToken: String,
        @SerializedName("userId") val userId: Long,
        @SerializedName("username") val username: String
    )
}
