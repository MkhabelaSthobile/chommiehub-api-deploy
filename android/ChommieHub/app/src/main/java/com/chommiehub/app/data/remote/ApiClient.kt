
package com.chommiehub.app.data.remote

import com.chommiehub.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provides the shared Retrofit client used throughout ChommieHub.
 *
 * Automatically attaches a Firebase Authentication ID token
 * when a user is signed in.
 *
 * Existing explicit Authorization headers are preserved.
 */
object ApiClient {

    private val authInterceptor = Interceptor { chain ->

        val originalRequest = chain.request()

        // Preserve manually supplied Authorization headers,
        // including the existing FCM device endpoints.
        if (originalRequest.header("Authorization") != null) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val idToken = try {
            runBlocking {
                withTimeoutOrNull(10_000L) {
                    user.getIdToken(false).await().token
                }
            }
        } catch (exception: Exception) {
            null
        }

        if (idToken.isNullOrBlank()) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // Do not attach a token obtained for an account that
        // has since signed out or changed.
        if (FirebaseAuth.getInstance().currentUser?.uid != user.uid) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $idToken")
            .build()

        chain.proceed(authenticatedRequest)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ChommieHubApi by lazy {
        retrofit.create(ChommieHubApi::class.java)
    }
}
