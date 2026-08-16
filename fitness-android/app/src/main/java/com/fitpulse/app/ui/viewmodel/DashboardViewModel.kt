package com.fitpulse.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.data.remote.dto.DashboardDTO
import com.fitpulse.app.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _training = MutableStateFlow<TrainingUiState>(TrainingUiState.Loading)
    val training = _training.asStateFlow()

    private val _health = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val health = _health.asStateFlow()

    fun loadTraining() {
        viewModelScope.launch {
            _training.value = TrainingUiState.Loading
            repository.getTrainingOverview()
                .onSuccess { _training.value = TrainingUiState.Success(it) }
                .onFailure { _training.value = TrainingUiState.Error(it.message ?: "加载失败") }
        }
    }

    fun loadHealth() {
        viewModelScope.launch {
            _health.value = HealthUiState.Loading
            repository.getHealthOverview()
                .onSuccess { _health.value = HealthUiState.Success(it) }
                .onFailure { _health.value = HealthUiState.Error(it.message ?: "加载失败") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // logout via AuthRepo
        }
    }
}

sealed interface TrainingUiState {
    object Loading : TrainingUiState
    data class Success(val data: DashboardDTO.TrainingOverview) : TrainingUiState
    data class Error(val msg: String) : TrainingUiState
}

sealed interface HealthUiState {
    object Loading : HealthUiState
    data class Success(val data: DashboardDTO.HealthOverview) : HealthUiState
    data class Error(val msg: String) : HealthUiState
}
