package com.music.stream.neptune.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.stream.neptune.auth.Auth0AuthManager
import com.music.stream.neptune.auth.AuthUserSession
import com.music.stream.neptune.auth.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = true,
    val authenticated: Boolean = false,
    val user: AuthUserSession? = null,
    val errorMessage: String? = null,
    val configured: Boolean = true
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: Auth0AuthManager,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        refreshSession()
    }

    fun refreshSession() = viewModelScope.launch {
        if (!authManager.isConfigured()) {
            userSessionManager.setSession(null)
            _uiState.value = AuthUiState(
                loading = false,
                authenticated = false,
                configured = false,
                errorMessage = "Auth0 is not configured"
            )
            return@launch
        }

        runCatching {
            if (authManager.hasValidSession()) authManager.getCurrentSession() else null
        }.onSuccess { session ->
            userSessionManager.setSession(session)
            _uiState.value = AuthUiState(
                loading = false,
                authenticated = session != null,
                user = session,
                configured = true
            )
        }.onFailure { error ->
            userSessionManager.setSession(null)
            _uiState.value = AuthUiState(
                loading = false,
                authenticated = false,
                configured = true,
                errorMessage = error.message
            )
        }
    }

    fun login(activity: Activity) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        runCatching { authManager.login(activity) }
            .onSuccess { session ->
                userSessionManager.setSession(session)
                _uiState.value = AuthUiState(
                    loading = false,
                    authenticated = true,
                    user = session,
                    configured = true
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    authenticated = false,
                    errorMessage = error.message ?: "Authentication failed"
                )
            }
    }

    fun logout(activity: Activity) = viewModelScope.launch {
        runCatching { authManager.logout(activity) }
        userSessionManager.setSession(null)
        _uiState.value = AuthUiState(
            loading = false,
            authenticated = false,
            configured = authManager.isConfigured()
        )
    }
}
