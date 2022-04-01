package geocoding

import dataclasses.geocoding.GeocodingResponse
import dataclasses.geocoding.Latlong
import geocoding.services.GeocodeApiService
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import variables.GeocodingApiVars

object Geoapify {
    private val geocodeRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.geoapify.com/v1/geocode/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val geocodeApiService = geocodeRetrofit.create(
        GeocodeApiService::class.java
    )

    suspend fun getLocation(latlong: Latlong): String? {
        val response: Response<GeocodingResponse>
        try {
            response = geocodeApiService.reverseGeocode(latitude = latlong.latitude,
                                                        longitude = latlong.longitude,
                                                        apiKey = GeocodingApiVars.GEOCODING_API_KEY)
        }
        catch (ex: Exception) {
            return null
        }

        val body = response.body() ?: return null

        return body.location
    }

    suspend fun getCoordinate(location: String): Latlong? {
        val response: Response<GeocodingResponse>

        try {
            response = geocodeApiService.geocode(location, GeocodingApiVars.GEOCODING_API_KEY)
        }
        catch (ex: Exception) {
            return null
        }

        val body = response.body() ?: return null

        return body.latlong
    }
}