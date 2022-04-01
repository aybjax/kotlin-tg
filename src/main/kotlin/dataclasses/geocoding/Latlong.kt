package dataclasses.geocoding

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Latlong(
    val latitude: Double,
    val longitude: Double,
)
