package com.chommiehub.app.data.remote

import com.chommiehub.app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides the shared Retrofit client used throughout ChommieHub.
 *
 * The base URL is supplied through BuildConfig so the application can use the
 * shared Render API while still allowing individual developers to override the
 * address locally without changing source code.
 */
object ApiClient {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ChommieHubApi by lazy {
        retrofit.create(ChommieHubApi::class.java)
    }
}