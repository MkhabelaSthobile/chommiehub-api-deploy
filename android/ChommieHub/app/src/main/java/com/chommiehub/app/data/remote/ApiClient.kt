package com.chommiehub.app.data.remote

import com.chommiehub.app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Shared Retrofit client used by the ChommieHub Android application
 * to communicate with the ASP.NET Core REST API.
 */
object ApiClient {

    private val retrofitClient: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ChommieHubApi by lazy {
        retrofitClient.create(ChommieHubApi::class.java)
    }
}