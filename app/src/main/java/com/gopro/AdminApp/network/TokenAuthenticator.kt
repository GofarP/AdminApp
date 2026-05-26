package com.gopro.AdminApp.network

import android.content.Context
import com.gopro.AdminApp.BuildConfig
import com.gopro.AdminApp.data.local.TokenManager
import com.gopro.AdminApp.data.local.UserSessionManager
import com.gopro.AdminApp.model.dto.request.refreshtoken.RefreshTokenRequest
import com.gopro.AdminApp.network.tokenrefresh.TokenRefreshApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val sessionManager: UserSessionManager
) : Authenticator {

    private val refreshApi: TokenRefreshApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TokenRefreshApi::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()

        if (accessToken == null || refreshToken == null) {
            return null
        }

        if (response.responseCount >= 2) {
            forceLogout()
            return null
        }

        val requestDTO = RefreshTokenRequest(accessToken, refreshToken)

        // Pengecekan try-catch untuk menghindari crash jika server mati saat refresh
        return try {
            val refreshResponse = refreshApi.refreshToken(requestDTO).execute()

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newAuthData = refreshResponse.body()!!

                // Null safety diterapkan di sini
                tokenManager.saveTokens(
                    token = newAuthData.token,
                    refreshToken = newAuthData.refreshToken ?: ""
                )

                runBlocking {
                    sessionManager.saveSession(
                        email = newAuthData.email,
                        name = newAuthData.fullName,
                        roles = newAuthData.roles ?: emptyList(),
                        perms = newAuthData.permissions ?: emptyList(),
                        photoUrl=newAuthData.photoUrl
                    )
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newAuthData.token}")
                    .build()

            } else {
                forceLogout()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            forceLogout()
            null
        }
    }

    private fun forceLogout() {
        tokenManager.clear()
        runBlocking { sessionManager.clear() }
    }

    private val Response.responseCount: Int
        get() {
            var result = 1
            var priorResponse = priorResponse
            while (priorResponse != null) {
                result++
                priorResponse = priorResponse.priorResponse
            }
            return result
        }
}