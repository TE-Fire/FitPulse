package com.fitpulse.app.data.repository

import com.fitpulse.app.common.PreferencesManager
import com.fitpulse.app.data.remote.api.AuthApi
import com.fitpulse.app.data.remote.dto.AuthDTO
import com.fitpulse.app.data.remote.dto.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val prefs: PreferencesManager
) {
    suspend fun login(username: String, password: String): Result<AuthDTO.LoginResp> {
        return try {
            val resp: ApiResponse<AuthDTO.LoginResp> = authApi.login(AuthDTO.LoginReq(username, password))
            if (resp.code == 0 && resp.data != null) {
                prefs.saveTokens(resp.data.accessToken, resp.data.refreshToken, resp.data.userId.toString())
                Result.success(resp.data)
            } else {
                Result.failure(Exception(resp.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        runCatching { authApi.logout() }
        prefs.clear()
    }
}
