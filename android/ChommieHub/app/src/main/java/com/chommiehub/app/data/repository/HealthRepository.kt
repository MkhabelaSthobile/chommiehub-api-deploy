package com.chommiehub.app.data.repository

import com.chommiehub.app.data.remote.ChommieHubApi

class HealthRepository(
    private val api: ChommieHubApi
) {

    suspend fun getHealth() = api.getHealth()
}