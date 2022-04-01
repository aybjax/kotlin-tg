package geocoding.services

import dataclasses.geocoding.GeocodingResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodeApiService {
//    @GET("search")
//    fun geocode(
//        @Query("text")
//        location: String,
//        @Query("apiKey")
//        apiKey: String,
//    ): Call<GeocodingResponse>

//    @GET("search")
//    suspend fun geocode(
//        @Query("text")
//        location: String,
//        @Query("apiKey")
//        apiKey: String,
//    ): GeocodingResponse

    @GET("search")
    suspend fun geocode(
        @Query("text")
        location: String,
        @Query("apiKey")
        apiKey: String,
    ): Response<GeocodingResponse>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat")
        latitude: Double,
        @Query("lon")
        longitude: Double,
        @Query("apiKey")
        apiKey: String,
    ): Response<GeocodingResponse>
}