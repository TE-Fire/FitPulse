package com.fitpulse.app.data.repository

import com.fitpulse.app.data.remote.api.DashboardApi
import com.fitpulse.app.data.remote.dto.DashboardDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val dashboardApi: DashboardApi
) {
    suspend fun getTrainingOverview(): Result<DashboardDTO.TrainingOverview> {
        return try {
            val resp = dashboardApi.getTrainingOverview()
            if (resp.code == 0 && resp.data != null) Result.success(resp.data)
            else Result.failure(Exception(resp.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHealthOverview(): Result<DashboardDTO.HealthOverview> {
        return try {
            val resp = dashboardApi.getHealthOverview()
            if (resp.code == 0 && resp.data != null) Result.success(resp.data)
            else Result.failure(Exception(resp.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
