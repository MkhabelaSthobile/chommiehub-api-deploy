package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.HealthResponse
import com.chommiehub.app.data.remote.ChommieHubApi

/**
 * Provides a repository abstraction between Android UI logic and
 * the remote health endpoint.
 */
class HealthRepository(
    private val api: ChommieHubApi
) {

    /**
     * Requests the current backend health state.
     */
    suspend fun getHealth(): HealthResponse {
        return api.getHealth()
    }
}