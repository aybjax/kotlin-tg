package dataclasses.geocoding.geocoding_response

import com.google.gson.annotations.SerializedName
import dataclasses.geocoding.geocoding_response.feature.Geometry

data class Feature(
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("properties")
    val property: Property,
)
