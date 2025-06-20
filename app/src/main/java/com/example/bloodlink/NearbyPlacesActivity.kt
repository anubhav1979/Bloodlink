package com.example.bloodlink

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloodlink.adapter.PlaceAdapter
import com.example.bloodlink.databinding.ActivityNearbyPlacesBinding
import com.example.bloodlink.model.Place
import com.google.android.gms.location.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import com.example.bloodlink.NearbyPlacesResponse
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class NearbyPlacesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNearbyPlacesBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var adapter: PlaceAdapter
    private val placesList = mutableListOf<Place>()
    private lateinit var userLocation: Location

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        const val API_KEY = "AIzaSyAH-MQFrfBQFbOK_k6c3aj1MAwhuIBagi0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_LONG).show()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        adapter = PlaceAdapter(placesList)
        binding.recyclerViewPlaces.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPlaces.adapter = adapter

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = location
                val latLng = "${location.latitude},${location.longitude}"
                fetchNearbyPlaces(latLng)
            } else {
                requestNewLocationData()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        userLocation = location
                    }
                    val latLng = "${location?.latitude},${location?.longitude}"
                    fetchNearbyPlaces(latLng)
                }
            }, Looper.getMainLooper()
        )
    }


    private fun fetchNearbyPlaces(latLng: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(GooglePlacesApi::class.java)

        val call = api.getNearbyPlaces(
            location = latLng,
            radius = 5000,
            type = "hospital",
            key = "YOUR_API_KEY"
        )

        call.enqueue(object : Callback<NearbyPlacesResponse>  {
            @SuppressLint("DefaultLocale", "NotifyDataSetChanged")
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onResponse(
                call: Call<NearbyPlacesResponse>,
                response: Response<NearbyPlacesResponse>
            ) {
                if (response.isSuccessful) {
                    val places = response.body()?.results ?: emptyList()

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            placesList.clear()

                            for (place in places) {
                                val placeLoc = Location("").apply {
                                    latitude = place.geometry.location.lat
                                    longitude = place.geometry.location.lng
                                }

                                val distance = location.distanceTo(placeLoc) // ✅ Now safe

                                place.distanceFromUser = String.format("%.1f km", distance / 1000)
                                placesList.add(place)
                            }

                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(applicationContext, "User location not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<NearbyPlacesResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "API Failed: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

