package dataclasses.geocoding.geocoding_response.feature

import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("coordinates")
    val latLong: Array<Double>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Geometry

        if (!latLong.contentEquals(other.latLong)) return false

        return true
    }

    override fun hashCode(): Int {
        return latLong.contentHashCode()
    }
}
