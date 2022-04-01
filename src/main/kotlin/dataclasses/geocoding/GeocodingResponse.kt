package dataclasses.geocoding

import com.google.gson.annotations.SerializedName
import dataclasses.geocoding.geocoding_response.Feature

data class GeocodingResponse(
    @SerializedName("features")
    val features: List<Feature>
): Geocoder, ReverseGeocoder {
    override val latlong: Latlong?
        get() {
            val lat = features.getOrNull(0)
                ?.geometry?.latLong?.getOrNull(0) ?: return null
            val long = features.getOrNull(0)
                ?.geometry?.latLong?.getOrNull(1) ?: return null

            return Latlong(
                lat,
                long,
            )
        }
    override val location: String?
        get() {
            val property = features.getOrNull(0)
                ?.property ?: return null

            return "${property.city} ${property.state} ${property.country} (${property.country_code})"
        }

}
