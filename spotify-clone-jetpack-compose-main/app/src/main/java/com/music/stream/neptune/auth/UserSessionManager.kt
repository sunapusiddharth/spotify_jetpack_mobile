package com.music.stream.neptune.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor() {
    private val _session = MutableStateFlow<AuthUserSession?>(null)
    val session: StateFlow<AuthUserSession?> = _session.asStateFlow()

    fun setSession(session: AuthUserSession?) {
        _session.value = session
    }

    fun userIdOrEmail(): String {
        return _session.value?.idOrEmail.orEmpty()
    }
}
