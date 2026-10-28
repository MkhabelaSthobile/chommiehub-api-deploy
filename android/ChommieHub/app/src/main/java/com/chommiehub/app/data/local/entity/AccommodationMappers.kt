package com.chommiehub.app.data.local.entity

import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type

fun AccommodationResponse.toEntity(
    cachedAtMillis: Long = System.currentTimeMillis()
): CachedAccommodationEntity {
    return CachedAccommodationEntity(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        description = description,
        phoneNumber = phoneNumber,
        amenitiesJson = gson.toJson(amenities),
        institutionArea = institutionArea,
        isActive = isActive,
        cachedAtMillis = cachedAtMillis
    )
}

fun CachedAccommodationEntity.toResponse(): AccommodationResponse {
    val amenityList: List<String> = try {
        gson.fromJson(amenitiesJson, stringListType) ?: emptyList()
    } catch (exception: Exception) {
        emptyList()
    }

    return AccommodationResponse(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        description = description,
        phoneNumber = phoneNumber,
        amenities = amenityList,
        institutionArea = institutionArea,
        isActive = isActive
    )
}