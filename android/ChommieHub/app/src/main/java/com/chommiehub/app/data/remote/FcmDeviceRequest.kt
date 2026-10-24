package com.chommiehub.app.data.remote

data class FcmDeviceRequest(
    val fcmToken: String,
    val platform: String = "android"
)