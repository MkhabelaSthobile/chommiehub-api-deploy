package com.chommiehub.app.data.remote

import com.chommiehub.app.data.model.HealthResponse
import retrofit2.http.GET

/**
 * Defines HTTP operations exposed by the ChommieHub REST API.
 *
 * Additional accommodation and residence-manager endpoints will be
 * added here as their Android features are implemented.
 */
interface ChommieHubApi {

    /**
     * Confirms that the custom ASP.NET Core backend is reachable.
     */
    @GET("api/health")
    suspend fun getHealth(): HealthResponse
}