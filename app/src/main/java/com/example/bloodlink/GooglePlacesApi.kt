package com.example.bloodlink
import com.example.bloodlink.NearbyPlacesResponse
import com.example.bloodlink.model.Place
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    @GET("nearbysearch/json")
    fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String
    ): Call<NearbyPlacesResponse>
}