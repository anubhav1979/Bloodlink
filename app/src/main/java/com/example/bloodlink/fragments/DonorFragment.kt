package com.example.bloodlink.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloodlink.databinding.FragmentDonorBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonorFragment : Fragment() {

    private var _binding: FragmentDonorBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            getLastKnownLocation()
        } else {
            Log.w("DonorFragment", "Location permission not granted.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastKnownLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = "${location.latitude},${location.longitude}"
                    fetchNearbyHospitalsAndBloodBanks(latLng)
                } else {
                    Log.w("DonorFragment", "Location is null")
                }
            }
            .addOnFailureListener {
                Log.e("DonorFragment", "Failed to get location", it)
            }
    }

    private fun fetchNearbyHospitalsAndBloodBanks(latLng: String) {
        val apiKey = "AIzaSyAH-MQFrfBQFbOK_k6c3aj1MAwhuIBagi0"

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(GooglePlacesApi::class.java)

        val call = api.getNearbyPlaces(
            location = latLng,
            radius = 5000,
            type = "hospital",
            apiKey = apiKey
        )

        call.enqueue(object : Callback<NearbyPlacesResponse> {
            override fun onResponse(
                call: Call<NearbyPlacesResponse>,
                response: Response<NearbyPlacesResponse>
            ) {
                if (response.isSuccessful) {
                    val places = response.body()?.results ?: emptyList()
                    displayNearbyPlaces(places)
                } else {
                    Log.e("DonorFragment", "API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NearbyPlacesResponse>, t: Throwable) {
                Log.e("DonorFragment", "API call failed", t)
            }
        })
    }

    private fun displayNearbyPlaces(places: List<PlaceResult>) {
        // TODO: Update your RecyclerView adapter with the list of places
        Log.d("DonorFragment", "Found ${places.size} places")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Retrofit API Interface
interface GooglePlacesApi {
    @GET("maps/api/place/nearbysearch/json")
    fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 3000,
        @Query("type") type: String = "hospital",
        @Query("key") apiKey: String
    ): Call<NearbyPlacesResponse>
}

// Data classes
data class NearbyPlacesResponse(val results: List<PlaceResult>)
data class PlaceResult(val name: String, val geometry: Geometry)
data class Geometry(val location: LatLng)
data class LatLng(val lat: Double, val lng: Double)
