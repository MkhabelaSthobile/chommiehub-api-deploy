package com.chommiehub.app.data.remote

import android.util.Log
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

/**
 * Converts technical networking exceptions into clear user-facing messages.
 *
 * ViewModels should continue logging the original exception for debugging while
 * presenting only these safe messages to the user interface.
 */
object ApiErrorMapper {

    private const val TAG = "ApiErrorMapper"

    fun message(exception: Throwable, fallback: String = "Something went wrong. Please try again."): String {
        Log.e(TAG, "API operation failed.", exception)

        return when (exception) {
            is SocketTimeoutException -> "The server took too long to respond. Please try again."
            is IOException -> "Unable to connect to ChommieHub. Check your internet connection and try again."
            is HttpException -> messageForHttpCode(exception.code(), fallback)
            else -> exception.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    private fun messageForHttpCode(code: Int, fallback: String): String {
        return when (code) {
            400 -> "The information supplied was not valid. Please check your entries."
            401 -> "Your session is no longer authorised. Please sign in again."
            403 -> "You do not have permission to perform this action."
            404 -> "The requested information could not be found."
            409 -> "This action could not be completed because the information has changed. Refresh and try again."
            429 -> "Too many requests were sent. Please wait a moment and try again."
            500 -> "The ChommieHub server encountered an error. Please try again."
            502, 503, 504 -> "The ChommieHub service is temporarily unavailable. Please try again shortly."
            else -> fallback
        }
    }
}