package dataclasses.geocoding.geocoding_response

import com.google.gson.annotations.SerializedName

data class Property(
    @SerializedName("city")
    val city: String,// "Dusseldorf",
    @SerializedName("state")
    val state: String,// "North Rhine-Westphalia",
    @SerializedName("country")
    val country: String,//"Germany",
    @SerializedName("country_code")
    val country_code: String,// "de",
)
