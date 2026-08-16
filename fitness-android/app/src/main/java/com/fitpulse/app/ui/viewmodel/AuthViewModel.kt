package com.fitpulse.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请输入用户名和密码")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            repository.login(username, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(loading = false, loginSuccess = true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "登录失败")
                }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }
}

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)
