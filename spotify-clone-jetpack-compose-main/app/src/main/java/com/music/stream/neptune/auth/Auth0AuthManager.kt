package com.music.stream.neptune.auth

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.music.stream.neptune.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class Auth0AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val account = Auth0(
        BuildConfig.AUTH0_CLIENT_ID,
        BuildConfig.AUTH0_DOMAIN
    )

    private val apiClient = AuthenticationAPIClient(account)
    private val credentialsManager = CredentialsManager(apiClient, SharedPreferencesStorage(context))

    fun isBypassEnabled(): Boolean = BuildConfig.AUTH_BYPASS_ENABLED

    fun getBypassSession(): AuthUserSession {
        val userId = BuildConfig.AUTH_BYPASS_USER_ID.ifBlank { "test-user" }
        val email = BuildConfig.AUTH_BYPASS_USER_EMAIL.ifBlank { "$userId@test.local" }
        return AuthUserSession(
            userId = userId,
            email = email,
            name = BuildConfig.AUTH_BYPASS_USER_NAME.ifBlank { "Test Listener" },
            pictureUrl = ""
        )
    }

    fun isConfigured(): Boolean {
        return isBypassEnabled() ||
            (BuildConfig.AUTH0_CLIENT_ID.isNotBlank() && BuildConfig.AUTH0_DOMAIN.isNotBlank())
    }

    suspend fun hasValidSession(): Boolean {
        if (isBypassEnabled()) return true
        if (!isConfigured()) return false
        return credentialsManager.hasValidCredentials()
    }

    suspend fun login(activity: Activity): AuthUserSession {
        if (isBypassEnabled()) {
            return getBypassSession()
        }
        if (!isConfigured()) {
            throw IllegalStateException("Auth0 is not configured. Set AUTH0_DOMAIN and AUTH0_CLIENT_ID.")
        }
        val credentials = suspendCancellableCoroutine<Credentials> { cont ->
            WebAuthProvider.login(account)
                .withScheme(BuildConfig.AUTH0_SCHEME)
                .withScope("openid profile email offline_access")
                .start(activity, object : Callback<Credentials, com.auth0.android.authentication.AuthenticationException> {
                    override fun onFailure(error: com.auth0.android.authentication.AuthenticationException) {
                        if (cont.isActive) cont.resumeWithException(error)
                    }

                    override fun onSuccess(result: Credentials) {
                        if (cont.isActive) cont.resume(result)
                    }
                })
        }
        credentialsManager.saveCredentials(credentials)
        return getSessionFromCredentials(credentials)
    }

    suspend fun getCurrentSession(): AuthUserSession? {
        if (isBypassEnabled()) return getBypassSession()
        if (!isConfigured()) return null
        if (!credentialsManager.hasValidCredentials()) return null
        val credentials = suspendCancellableCoroutine<Credentials> { cont ->
            credentialsManager.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
                override fun onFailure(error: CredentialsManagerException) {
                    if (cont.isActive) cont.resumeWithException(error)
                }

                override fun onSuccess(result: Credentials) {
                    if (cont.isActive) cont.resume(result)
                }
            })
        }
        return getSessionFromCredentials(credentials)
    }

    suspend fun logout(activity: Activity) {
        if (isBypassEnabled()) return
        if (!isConfigured()) return
        suspendCancellableCoroutine<Unit> { cont ->
            WebAuthProvider.logout(account)
                .withScheme(BuildConfig.AUTH0_SCHEME)
                .start(activity, object : Callback<Void?, com.auth0.android.authentication.AuthenticationException> {
                    override fun onFailure(error: com.auth0.android.authentication.AuthenticationException) {
                        credentialsManager.clearCredentials()
                        if (cont.isActive) cont.resume(Unit)
                    }

                    override fun onSuccess(result: Void?) {
                        credentialsManager.clearCredentials()
                        if (cont.isActive) cont.resume(Unit)
                    }
                })
        }
    }

    private suspend fun getSessionFromCredentials(credentials: Credentials): AuthUserSession {
        val profile = suspendCancellableCoroutine<UserProfile> { cont ->
            apiClient.userInfo(credentials.accessToken)
                .start(object : Callback<UserProfile, com.auth0.android.authentication.AuthenticationException> {
                    override fun onFailure(error: com.auth0.android.authentication.AuthenticationException) {
                        if (cont.isActive) cont.resumeWithException(error)
                    }

                    override fun onSuccess(result: UserProfile) {
                        if (cont.isActive) cont.resume(result)
                    }
                })
        }

        return AuthUserSession(
            userId = profile.getId().orEmpty(),
            email = profile.email.orEmpty(),
            name = profile.name.orEmpty(),
            pictureUrl = profile.pictureURL.orEmpty()
        )
    }
}
