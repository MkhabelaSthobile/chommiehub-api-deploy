package com.chommiehub.app.data.remote

import com.chommiehub.app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Creates and exposes the shared Retrofit client used by the Android
 * application to communicate with the ChommieHub ASP.NET Core REST API.
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