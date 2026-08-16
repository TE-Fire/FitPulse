package com.fitpulse.app.data.remote.api

import com.fitpulse.app.data.remote.dto.ApiResponse
import com.fitpulse.app.data.remote.dto.DashboardDTO
import retrofit2.http.GET

interface DashboardApi {

    @GET("admin/dashboard/training")
    suspend fun getTrainingOverview(): ApiResponse<DashboardDTO.TrainingOverview>

    @GET("admin/dashboard/health")
    suspend fun getHealthOverview(): ApiResponse<DashboardDTO.HealthOverview>
}
