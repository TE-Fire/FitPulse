package com.fitpulse.app.data.remote.api

import com.fitpulse.app.data.remote.dto.ApiResponse
import com.fitpulse.app.data.remote.dto.AuthDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body req: AuthDTO.RegisterReq): ApiResponse<Void>

    @POST("auth/login")
    suspend fun login(@Body req: AuthDTO.LoginReq): ApiResponse<AuthDTO.LoginResp>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Void>
}
