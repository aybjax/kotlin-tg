package dataclasses.geocoding.geocoding_response

import com.google.gson.annotations.SerializedName
import dataclasses.geocoding.ReverseGeocoder

data class Property(
    @SerializedName("city")
    val city: String?,// "Dusseldorf",
    @SerializedName("state")
    val state: String?,// "North Rhine-Westphalia",
    @SerializedName("country")
    val country: String,//"Germany",
    @SerializedName("country_code")
    val country_code: String,// "de",
): ReverseGeocoder {
    override val location: String
        get() = buildString {
                this@Property.city?.let {
                    append(this@Property.city)
                }

                this@Property.state?.let {
                    append(" ")
                    append(this@Property.state)
                }

                append(" ")
                append(this@Property.country)

                append(" ")
                append("(${this@Property.country_code})")
            }
}
