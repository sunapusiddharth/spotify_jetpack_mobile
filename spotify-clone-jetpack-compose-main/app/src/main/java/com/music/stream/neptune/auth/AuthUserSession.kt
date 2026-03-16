package com.music.stream.neptune.auth

data class AuthUserSession(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val pictureUrl: String = ""
) {
    val idOrEmail: String
        get() = userId.ifBlank { email }
}
